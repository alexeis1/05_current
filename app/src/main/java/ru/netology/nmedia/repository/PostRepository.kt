package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun saveAsync      (post: Post, callback: GetResultCallback<Post>)
    fun removeByIdAsync(id: Long, callback: GetResultCallback<Boolean>)
    fun likeByIdAsync  (id: Long, callback: GetResultCallback<Post>)
    fun getAllAsync    (          callback: GetResultCallback<List<Post>>)

    interface GetResultCallback<T> {
        fun onSuccess(value: T) {}
        fun onError(e: Exception) {}
    }
}
