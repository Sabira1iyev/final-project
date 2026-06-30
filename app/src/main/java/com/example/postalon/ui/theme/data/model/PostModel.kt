package com.example.postalon.data.model

data class PostModel(
    val id: String = "",
    val uid: String = "",
    val imageLink: String? = null,
    val createdAt: Long = 0L,
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val selected: Int? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromSnapshot(
            id: String,
            raw: Map<String, Any?>,
            currentUid: String?
        ): PostModel {
            val likes = raw["likes"] as? Map<String, Any?>
            val selected = if (currentUid != null) {
                (likes?.get(currentUid) as? Number)?.toInt()
            } else null

            return PostModel(
                id = id,
                uid = raw["uid"] as? String ?: "",
                imageLink = raw["image_link"] as? String,
                createdAt = (raw["created_at"] as? Number)?.toLong() ?: 0L,
                likeCount = (raw["like_count"] as? Number)?.toInt() ?: 0,
                dislikeCount = (raw["dislike_count"] as? Number)?.toInt() ?: 0,
                selected = selected
            )
        }
    }
}
