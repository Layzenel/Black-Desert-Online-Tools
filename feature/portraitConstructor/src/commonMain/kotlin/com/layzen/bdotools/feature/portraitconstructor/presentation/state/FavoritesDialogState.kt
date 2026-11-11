package com.layzen.bdotools.feature.portraitconstructor.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.favorite.FavoriteSet

/**
 * Окно избранного: наборы всех портретов в `Favorite FaceTexture`. [sets] — `null`, пока список читается;
 * [previews] — портреты выбранного набора по ID персонажа. [titleDraft] — название выбранного набора в поле
 * ввода, [marked] — наборы, отмеченные для удаления, [confirmDelete] и [confirmReplace] — открыто
 * подтверждение удаления отмеченных или замены выбранного набора текущими портретами.
 */
data class FavoritesDialogState(
    val sets: List<FavoriteSet>? = null,
    val selected: String? = null,
    val previews: Map<String, ImageBitmap> = emptyMap(),
    val titleDraft: String = "",
    val marked: Set<String> = emptySet(),
    val confirmDelete: Boolean = false,
    val confirmReplace: Boolean = false,
) {
    /** Выбранный набор; `null`, если ничего не выбрано или список ещё читается. */
    val selectedSet: FavoriteSet? get() = sets?.firstOrNull { it.name == selected }

    /** Название в поле отличается от сохранённого. */
    val titleChanged: Boolean get() = selectedSet?.let { FavoriteSet.normalizeTitle(titleDraft) != it.title } == true
}
