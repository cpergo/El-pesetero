package com.pesetas.ui.categories.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.ui.components.CategoryColors
import com.pesetas.ui.navigation.Routes
import com.pesetas.ui.util.IconCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryEditorUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val name: String = "",
    val iconKey: String = IconCatalog.default,
    val colorArgb: Int = CategoryColors.first(),
    val type: CategoryType = CategoryType.EXPENSE,
) {
    val canSave: Boolean get() = name.isNotBlank()
}

@HiltViewModel
class CategoryEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>(Routes.ARG_CATEGORY_ID) ?: -1L
    private val initialType: CategoryType = runCatching {
        CategoryType.valueOf(savedStateHandle.get<String>(Routes.ARG_TYPE).orEmpty())
    }.getOrDefault(CategoryType.EXPENSE)

    private val _uiState = MutableStateFlow(
        CategoryEditorUiState(
            isLoading = categoryId > 0,
            isEditing = categoryId > 0,
            type = initialType,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _finished = MutableSharedFlow<Unit>()
    val finished = _finished.asSharedFlow()

    init {
        if (categoryId > 0) {
            viewModelScope.launch {
                categoryRepository.getCategory(categoryId)?.let { category ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            name = category.name,
                            iconKey = category.iconKey,
                            colorArgb = category.colorArgb,
                            type = category.type,
                        )
                    }
                } ?: _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(name = name) }

    fun setIcon(iconKey: String) = _uiState.update { it.copy(iconKey = iconKey) }

    fun setColor(colorArgb: Int) = _uiState.update { it.copy(colorArgb = colorArgb) }

    fun setType(type: CategoryType) = _uiState.update { it.copy(type = type) }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            categoryRepository.upsert(
                Category(
                    id = if (categoryId > 0) categoryId else 0,
                    name = state.name.trim(),
                    iconKey = state.iconKey,
                    colorArgb = state.colorArgb,
                    type = state.type,
                ),
            )
            _finished.emit(Unit)
        }
    }
}
