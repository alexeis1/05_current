package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = 0L
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    private val _error       = SingleLiveEvent<String>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    val error : LiveData<String>
        get() = _error

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.GetResultCallback<List<Post>> {
            override fun onSuccess(value: List<Post>) {
                _data.postValue(FeedModel(posts = value, empty = value.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
                _error.postValue(e.message)
            }
        })
    }

    fun save() {
        edited.value?.let { _post ->
            val insertAfterSuccess = _post.id == 0L
            repository.saveAsync(_post, object : PostRepository.GetResultCallback<Post> {
                override fun onSuccess(value: Post) {
                    if (insertAfterSuccess) {
                        _data.postValue(_data.value?.copy(
                            posts = listOf(value) + _data.value?.posts.orEmpty()))
                    } else {
                        _data.postValue(_data.value?.copy(posts = _data.value?.posts.orEmpty()
                            .map { if (it.id != value.id) it else value }
                        ))
                    }
                    _postCreated.postValue(Unit)
                    edited.postValue(empty)
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                    _error.postValue(e.message)
                }
            })
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        val old = _data.value?.posts.orEmpty()

        _data.postValue(_data.value?.copy(posts = _data.value?.posts.orEmpty()
            .map { if (it.id != id) it else
                it.copy(likedByMe = !it.likedByMe,
                    likes     = if (!it.likedByMe) it.likes + 1 else it.likes - 1) }
        ))

        repository.likeByIdAsync(id, object : PostRepository.GetResultCallback<Post> {
            override fun onSuccess(value: Post) {
                _data.postValue(_data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .map { if (it.id != value.id) it else value }
                ))
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
                _error.postValue(e.message)
            }
        })
    }

    fun removeById(id: Long) {
        // Оптимистичная модель
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )
        repository.removeByIdAsync(id, object : PostRepository.GetResultCallback<Boolean> {
            override fun onSuccess(value: Boolean) {
                if (!value){//восстанавливаем состояние
                    _data.postValue(_data.value?.copy(posts = old))
                    val context = getApplication<Application>().applicationContext
                    _error.postValue(context.getString(R.string.deleting_error))
                }
            }
            override fun onError(e: Exception) {//восстанавливаем состояние
                _data.postValue(_data.value?.copy(posts = old))
                _error.postValue(e.message)
            }
        })
    }
}
