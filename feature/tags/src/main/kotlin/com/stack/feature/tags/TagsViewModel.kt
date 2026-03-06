package com.stack.feature.tags

import androidx.lifecycle.viewModelScope
import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.Tag
import com.stack.domain.usecase.tag.CreateTagUseCase
import com.stack.domain.usecase.tag.DeleteTagUseCase
import com.stack.domain.usecase.tag.GetTagsUseCase
import com.stack.domain.usecase.tag.UpdateTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagsState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val showEditor: Boolean = false,
    val editingTag: Tag? = null
)

sealed interface TagsIntent {
    data object ShowCreateDialog : TagsIntent
    data class ShowEditDialog(val tag: Tag) : TagsIntent
    data object DismissDialog : TagsIntent
    data class CreateTag(val name: String, val color: Int) : TagsIntent
    data class UpdateTag(val tagId: Long, val name: String, val color: Int) : TagsIntent
    data class DeleteTag(val tagId: Long) : TagsIntent
}

sealed interface TagsEffect {
    data class ShowMessage(val message: String) : TagsEffect
}

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val getTagsUseCase: GetTagsUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase
) : BaseViewModel<TagsState, TagsIntent, TagsEffect>(TagsState()) {

    init {
        loadTags()
    }

    override fun dispatch(intent: TagsIntent) {
        when (intent) {
            TagsIntent.ShowCreateDialog -> updateState { copy(showEditor = true, editingTag = null) }
            is TagsIntent.ShowEditDialog -> updateState { copy(showEditor = true, editingTag = intent.tag) }
            TagsIntent.DismissDialog -> updateState { copy(showEditor = false, editingTag = null) }
            is TagsIntent.CreateTag -> createTag(intent.name, intent.color)
            is TagsIntent.UpdateTag -> updateTag(intent.tagId, intent.name, intent.color)
            is TagsIntent.DeleteTag -> deleteTag(intent.tagId)
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            getTagsUseCase().collectLatest { tags ->
                updateState { copy(tags = tags, isLoading = false) }
            }
        }
    }

    private fun createTag(name: String, color: Int) {
        viewModelScope.launch {
            createTagUseCase(name, color)
            updateState { copy(showEditor = false, editingTag = null) }
        }
    }

    private fun updateTag(tagId: Long, name: String, color: Int) {
        viewModelScope.launch {
            updateTagUseCase(tagId, name, color)
            updateState { copy(showEditor = false, editingTag = null) }
        }
    }

    private fun deleteTag(tagId: Long) {
        viewModelScope.launch {
            deleteTagUseCase(tagId)
        }
    }
}
