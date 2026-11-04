package com.layzen.bdotools.feature.portraits.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.portraits.domain.BackupInfo
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration

/** Всё, что показывает страница портретов: доступ к папке, портреты, открытые окна и ожидание. */
data class PortraitsState(
    /** `null`, пока проверяется доступ к папке. */
    val access: FolderAccess? = null,
    val portraits: List<PortraitItem> = emptyList(),
    /** Идёт чтение портретов; `null`, когда все прочитаны. */
    val loading: LoadProgress? = null,
    val decorations: Map<String, PortraitDecoration> = emptyMap(),
    val backups: List<BackupInfo> = emptyList(),
    val sourceDialog: SourceDialogState? = null,
    val editor: EditorState? = null,
    val decorationDialog: DecorationDialogState? = null,
    val restoreDialog: RestoreDialogState? = null,
    val favoritesDialog: FavoritesDialogState? = null,
    /** Режим изменения порядка на сетке; `null`, когда он выключен. */
    val reorder: ReorderState? = null,
    /** Идущая анимация обмена двух портретов. */
    val swapAnimation: SwapAnimation? = null,
    val busy: Busy? = null,
    val notice: Notice? = null,
) {
    fun hasBackups(characterId: String) = backups.any { characterId in it.characterIds }

    fun decorationOf(characterId: String) = decorations[characterId] ?: PortraitDecoration()

    /** Заменяет картинки у перечисленных персонажей, порядок сетки не меняется. */
    fun withPortraitImages(images: Map<String, ImageBitmap>) = copy(
        portraits = portraits.map { item -> images[item.characterId]?.let { PortraitItem(item.characterId, it) } ?: item },
    )
}
