package com.layzen.bdotools.feature.portraitconstructor.presentation.controller

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.FavoriteSet
import com.layzen.bdotools.feature.portraitconstructor.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.BusyKind
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.FavoritesDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.Notice
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.NoticeKind
import kotlinx.coroutines.Job

/**
 * Окно избранного: наборы всех портретов в `Favorite FaceTexture`. Сохранить можно только все портреты
 * сразу, и применяется набор целиком — к тем персонажам из него, которые есть сейчас.
 * Перед записью старые портреты уходят в резервную копию.
 */
internal class FavoritesController(private val context: PortraitsContext) {

    private var previewJob: Job? = null

    fun open() {
        context.update { it.copy(favoritesDialog = FavoritesDialogState()) }
        reload(select = null)
    }

    fun select(name: String) {
        val set = context.current.favoritesDialog?.sets?.firstOrNull { it.name == name } ?: return
        updateDialog { it.copy(selected = name, previews = emptyMap(), titleDraft = set.title.orEmpty()) }
        previewJob?.cancel()
        previewJob = context.launch {
            orderedIds(set.characterIds).forEach { id ->
                context.breathe()
                val image = context.images.render(context.repository.readFavoritePortrait(name, id))
                updateDialog { if (it.selected == name) it.copy(previews = it.previews + (id to image)) else it }
            }
        }
    }

    fun changeTitle(text: String) = updateDialog { it.copy(titleDraft = text.take(FavoriteSet.TITLE_MAX_LENGTH)) }

    /** Сохраняет название выбранного набора; пустое возвращает подпись датой. */
    fun rename() {
        val dialog = context.current.favoritesDialog ?: return
        val set = dialog.selectedSet ?: return
        val title = FavoriteSet.normalizeTitle(dialog.titleDraft)
        context.launch {
            context.repository.renameFavoriteSet(set.name, title.orEmpty())
            updateDialog { state ->
                state.copy(
                    sets = state.sets?.map { if (it.name == set.name) it.copy(title = title) else it },
                    titleDraft = title.orEmpty(),
                )
            }
        }
    }

    fun toggleMark(name: String) =
        updateDialog { it.copy(marked = if (name in it.marked) it.marked - name else it.marked + name) }

    fun requestDelete() = updateDialog { if (it.marked.isEmpty()) it else it.copy(confirmDelete = true) }

    fun cancelDelete() = updateDialog { it.copy(confirmDelete = false) }

    /** Удаляет отмеченные наборы; окно остаётся открытым со свежим списком. */
    fun deleteMarked() {
        val names = context.current.favoritesDialog?.marked?.toList().orEmpty()
        if (names.isEmpty()) return
        cancelDelete()
        context.launch {
            val left = context.withBusy(BusyKind.Deleting) {
                context.setProgress(0, names.size)
                context.repository.deleteFavoriteSets(names) { done, total -> context.setProgress(done, total) }
            }
            val selected = context.current.favoritesDialog?.selected?.takeIf { name -> left.any { it.name == name } }
            updateDialog { it.copy(sets = left, marked = emptySet()) }
            context.showNotice(Notice(NoticeKind.FavoritesDeleted, names.size.toString()))
            if (selected == null) {
                previewJob?.cancel()
                updateDialog { it.copy(selected = null, previews = emptyMap(), titleDraft = "") }
                left.firstOrNull()?.let { select(it.name) }
            }
        }
    }

    fun requestReplace() = updateDialog { if (it.selectedSet == null) it else it.copy(confirmReplace = true) }

    fun cancelReplace() = updateDialog { it.copy(confirmReplace = false) }

    /** Заменяет портреты выбранного набора текущими; название набора остаётся. */
    fun replaceSelected() {
        val set = context.current.favoritesDialog?.selectedSet ?: return
        cancelReplace()
        context.launch {
            val replacement = context.withBusy(BusyKind.SavingFavorites) {
                context.repository.replaceFavoriteSet(set.name, currentIds()) { done, total ->
                    context.setProgress(done, total)
                }
            }
            context.showNotice(Notice(NoticeKind.FavoriteReplaced, set.label))
            reload(select = replacement)
        }
    }

    /** Сохраняет все текущие портреты новым набором и показывает его. */
    fun saveAll() {
        context.launch {
            val set = context.withBusy(BusyKind.SavingFavorites) {
                context.repository.saveFavoriteSet(currentIds()) { done, total ->
                    context.setProgress(done, total)
                }
            }
            context.showNotice(Notice(NoticeKind.FavoriteAdded, set))
            reload(select = set)
        }
    }

    /** Портреты на странице — только персонажи выбранного аккаунта. */
    private fun currentIds() = context.current.portraits.map { it.characterId }

    fun apply() {
        val set = context.current.favoritesDialog?.selectedSet ?: return
        val ids = orderedIds(set.characterIds).filter { id -> context.current.portraits.any { it.characterId == id } }
        if (ids.isEmpty()) return
        context.launch {
            close()
            val faces = mutableMapOf<String, ImageBitmap>()
            val writes = context.withBusy(BusyKind.ReadingFavorites) {
                ids.mapIndexed { index, id ->
                    context.setProgress(index, ids.size)
                    val (face, write) = context.compose(id, context.repository.readFavoritePortrait(set.name, id))
                    faces[id] = face
                    write
                }
            }
            val backup = context.save(writes)
            context.update { it.withPortraitImages(faces).copy(notice = Notice(NoticeKind.FavoriteApplied, backup)) }
        }
    }

    fun close() {
        previewJob?.cancel()
        context.update { it.copy(favoritesDialog = null) }
    }

    /** Перечитывает список наборов и открывает [select] или самый новый. */
    private fun reload(select: String?) {
        context.launch {
            val sets = context.repository.favoriteSets()
            updateDialog { it.copy(sets = sets) }
            (select ?: sets.firstOrNull()?.name)?.let(::select)
        }
    }

    /** Сначала персонажи в порядке страницы, затем те, кого сейчас нет. */
    private fun orderedIds(ids: Set<String>): List<String> {
        val current = context.current.portraits.map { it.characterId }
        return current.filter { it in ids } + (ids - current.toSet()).sorted()
    }

    private fun updateDialog(transform: (FavoritesDialogState) -> FavoritesDialogState) =
        context.update { it.copy(favoritesDialog = it.favoritesDialog?.let(transform)) }
}
