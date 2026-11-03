package com.layzen.bdotools.feature.portraits.presentation.state

/** Окно выбора картинки: файл с устройства или ссылка. */
data class SourceDialogState(
    val target: ReplaceTarget,
    val url: String = "",
    val isLoading: Boolean = false,
    val error: Notice? = null,
)
