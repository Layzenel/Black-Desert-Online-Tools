package com.layzen.bdotools.feature.portraits.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraits.domain.BackupInfo

/**
 * Окно восстановления из резервной копии.
 * Для всех портретов: [selected] — выбранная копия, [checked] — какие портреты из неё вернуть.
 * Для одного портрета: [backups] — только копии с этим персонажем, [selected] — выбранная версия.
 * [previews] — уменьшенные портреты по ключу [previewKey].
 * [marked] — копии, отмеченные галочкой для удаления (только в режиме всех портретов).
 */
data class RestoreDialogState(
    val target: ReplaceTarget,
    val backups: List<BackupInfo>,
    val selected: String?,
    val checked: Set<String> = emptySet(),
    val previews: Map<String, ImageBitmap> = emptyMap(),
    val isLoadingPreviews: Boolean = false,
    /** Копии, отмеченные в списке для удаления. */
    val marked: Set<String> = emptySet(),
    val confirmDelete: Boolean = false,
) {
    val selectedBackup: BackupInfo? get() = backups.firstOrNull { it.name == selected }
}

/** Ключ превью в [RestoreDialogState.previews]: копия и персонаж. */
fun previewKey(backup: String, characterId: String) = "$backup/$characterId"
