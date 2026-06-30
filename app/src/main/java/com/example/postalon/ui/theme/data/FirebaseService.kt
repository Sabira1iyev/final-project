package com.example.postalon.data

import android.util.Log
import com.example.postalon.data.model.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class FirebaseService {
    private val auth = FirebaseAuth.getInstance()
    

    private val DATABASE_URL = "https://postalon-51db0-default-rtdb.europe-west1.firebasedatabase.app/"
    
    private val dbRef by lazy {
        Log.d("FirebaseService", "Connection attempt: $DATABASE_URL")
        try {

            FirebaseDatabase.getInstance(DATABASE_URL).apply {
                setPersistenceEnabled(true)
            }.reference
        } catch (e: Exception) {
            Log.e("FirebaseService", "Failed to connect via URL, trying default", e)
            FirebaseDatabase.getInstance().reference
        }
    }

    suspend fun signInAnonymouslyIfNeeded(): FirebaseUser? {
        return try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            Log.d("FirebaseService", "Login successful: ${auth.currentUser?.uid}")
            auth.currentUser
        } catch (e: Exception) {
            Log.e("FirebaseService", "Auth error", e)
            null
        }
    }

    suspend fun createPost(imageLink: String): Boolean {
        val user = auth.currentUser ?: return false
        
        val data = mapOf(
            "uid" to user.uid,
            "image_link" to imageLink,
            "created_at" to ServerValue.TIMESTAMP,
            "like_count" to 0,
            "dislike_count" to 0
        )

        return try {
            withTimeoutOrNull(6000) {
                dbRef.child("posts").push().setValue(data).await()
                Log.d("FirebaseService", "Write successful")
                true
            } ?: run {
                Log.e("FirebaseService", "Write timeout")
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Write error", e)
            false
        }
    }

    fun getLivePostsFlow(limit: Int): Flow<List<PostModel>> = callbackFlow {
        Log.d("FirebaseService", "Data stream started")
        val query: Query = dbRef.child("posts").limitToLast(limit)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseService", "Data updated: ${snapshot.childrenCount}")
                val currentUid = auth.currentUser?.uid
                val list = snapshot.children.mapNotNull { child ->
                    val map = child.value as? Map<*, *> ?: return@mapNotNull null
                    val stringMap = map.entries.associate { it.key.toString() to it.value }
                    PostModel.fromSnapshot(child.key ?: "", stringMap, currentUid)
                }.sortedByDescending { it.id }

                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseService", "Firebase cancel: ${error.message}")
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun likeQuestions(postId: String, selected: Int?): Boolean {
        val user = auth.currentUser ?: return false
        val postRef = dbRef.child("posts").child(postId)

        return suspendCancellableCoroutine { cont ->
            postRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val map = (currentData.value as? Map<*, *>)?.toMutableMap() ?: return Transaction.abort()
                    
                    var likeCount = (map["like_count"] as? Number)?.toLong() ?: 0L
                    var dislikeCount = (map["dislike_count"] as? Number)?.toLong() ?: 0L
                    val likes = (map["likes"] as? Map<*, *>)?.toMutableMap() ?: mutableMapOf()
                    
                    val currentLike = (likes[user.uid] as? Number)?.toInt()

                    if (selected == null) {
                        if (currentLike == null) return Transaction.abort()
                        when (currentLike) {
                            1 -> if (likeCount > 0) likeCount -= 1
                            2 -> if (dislikeCount > 0) dislikeCount -= 1
                        }
                        likes.remove(user.uid)
                    } else {
                        if (currentLike == selected) return Transaction.abort()
                        when (currentLike) {
                            1 -> if (likeCount > 0) likeCount -= 1
                            2 -> if (dislikeCount > 0) dislikeCount -= 1
                        }
                        if (selected == 1) likeCount += 1 else dislikeCount += 1
                        likes[user.uid] = selected.toLong()
                    }

                    map["like_count"] = likeCount
                    map["dislike_count"] = dislikeCount
                    map["likes"] = likes

                    currentData.value = map
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (cont.isActive) cont.resume(error == null && committed)
                }
            })
        }
    }
}
