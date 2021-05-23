package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
) {
    fun toDto() = Post(
        id = id,
        author = author,
        authorAvatar = "",
        content = content,
        published = published.toLong(),
        likedByMe = likedByMe,
        likes = likes
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                author = dto.author,
                content = dto.content,
                published = dto.published.toString(),
                likedByMe = dto.likedByMe,
                likes = dto.likes
            )

    }
}

