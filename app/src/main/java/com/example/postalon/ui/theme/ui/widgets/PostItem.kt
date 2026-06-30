package com.example.postalon.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.postalon.data.model.PostModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement


@Composable
fun PostItem(
    post: PostModel,
    onLike: (Int?) -> Unit,
    onDislike: (Int?) -> Unit
) {
    var displayPost by remember(post.id) { mutableStateOf(post) }

    LaunchedEffect(post) {
        displayPost = post
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF7B9CFF))
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            val link = displayPost.imageLink
            if (link != null) {
                SubcomposeAsyncImage(
                    model = link,
                    contentDescription = "Post image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(350.dp),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF8EDBFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF8EDBFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BrokenImage,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF8EDBFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ImageNotSupported,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.padding(8.dp).
                fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement= Arrangement.Center
        ) {
            Text(displayPost.likeCount.toString())
            Box(modifier = Modifier.width(8.dp))

            IconButton(onClick = {
                val isLiked = displayPost.selected == 1

                displayPost = if (isLiked) {
                    displayPost.copy(selected = null, likeCount = displayPost.likeCount - 1)
                } else {
                    val newDislikeCount =
                        if (displayPost.selected == 2) displayPost.dislikeCount - 1
                        else displayPost.dislikeCount

                    displayPost.copy(
                        selected = 1,
                        likeCount = displayPost.likeCount + 1,
                        dislikeCount = newDislikeCount
                    )
                }

                onLike(if (isLiked) null else 1)
            }) {
                Icon(
                    imageVector = if (displayPost.selected == 1) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "Like",
                    tint = Color(0xFF4CAF50)
                )
            }

            Box(modifier = Modifier.width(8.dp))
            Text(displayPost.dislikeCount.toString())
            Box(modifier = Modifier.width(16.dp))

            IconButton(onClick = {
                val isDisliked = displayPost.selected == 2

                displayPost = if (isDisliked) {
                    displayPost.copy(selected = null, dislikeCount = displayPost.dislikeCount - 1)
                } else {
                    val newLikeCount =
                        if (displayPost.selected == 1) displayPost.likeCount - 1
                        else displayPost.likeCount

                    displayPost.copy(
                        selected = 2,
                        dislikeCount = displayPost.dislikeCount + 1,
                        likeCount = newLikeCount
                    )
                }

                onDislike(if (isDisliked) null else 2)
            }) {
                Icon(
                    imageVector = if (displayPost.selected == 2) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                    contentDescription = "Dislike",
                    tint = Color(0xFFF44336)
                )
            }
        }

        Text(
            text = formatTimestamp(displayPost.createdAt),
            modifier = Modifier.padding(8.dp)
        )
    }
}

private fun formatTimestamp(millis: Long): String {
    if (millis == 0L) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(millis))
}
