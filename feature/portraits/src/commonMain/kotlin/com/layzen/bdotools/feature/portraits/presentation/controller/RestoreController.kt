package com.layzen.bdotools.feature.portraits.presentation.controller

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraits.data.PortraitWrite
import com.layzen.bdotools.feature.portraits.domain.BackupInfo
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.domain.containing
import com.layzen.bdotools.feature.portraits.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraits.presentation.state.BusyKind
import com.layzen.bdotools.feature.portraits.presentation.state.Notice
import com.layzen.bdotools.feature.portraits.presentation.state.NoticeKind
import com.layzen.bdotools.feature.portraits.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraits.presentation.state.RestoreDialogState
import com.layzen.bdotools.feature.portraits.presentation.state.previewKey
import kotlinx.coroutines.Job

/**
 * Окно резервных копий: превью, восстановление выбранных портретов и удаление копий.
 * Восстановленный портрет становится чистой основой, надпись и значки персонажа сбрасываются.
 */
internal class RestoreController(private val context: PortraitsContext) {

    private var previewJob: Job? = null

    fun open(target: ReplaceTarget) {
        val all = context.current.backups
        when (target) {
            ReplaceTarget.All -> {
                context.update { it.copy(restoreDialog = RestoreDialogState(target, all, selected = null)) }
                all.firstOrNull()?.let { select(it.name) }
            }
            is ReplaceTarget.One -> {
                val backups = all.containing(target.characterId)
                context.update { it.copy(restoreDialog = RestoreDialogState(target, backups, selected = backups.firstOrNull()?.name)) }
                loadPreviews(backups.map { it.name to target.characterId })
            }
        }
    }

    /** В режиме всех портретов отмечает портреты текущих персонажей из копии и грузит превью. */
    fun select(name: String) {
        val dialog = context.current.restoreDialog ?: return
        val backup = dialog.backups.firstOrNull { it.name == name } ?: return
        if (dialog.target is ReplaceTarget.One) return updateDialog { it.copy(selected = name) }
        val current = context.current.portraits.map { it.characterId }
        val ids = current.filter { it in backup.characterIds } + backup.characterIds.filterNot { it in current }.sorted()
        updateDialog { it.copy(selected = name, checked = backup.characterIds.intersect(current.toSet())) }
        loadPreviews(ids.map { name to it })
    }

    fun togglePortrait(characterId: String) = updateDialog { it.copy(checked = it.checked.toggled(characterId)) }

    fun checkAll(checked: Boolean) =
        updateDialog { it.copy(checked = if (checked) it.selectedBackup?.characterIds.orEmpty() else emptySet()) }

    fun toggleBackupMark(name: String) = updateDialog { it.copy(marked = it.marked.toggled(name)) }

    fun requestDelete() = updateDialog { if (it.marked.isEmpty()) it else it.copy(confirmDelete = true) }

    fun cancelDelete() = updateDialog { it.copy(confirmDelete = false) }

    fun close() {
        previewJob?.cancel()
        context.update { it.copy(restoreDialog = null) }
    }

    fun apply() {
        val dialog = context.current.restoreDialog ?: return
        val backup = dialog.selected ?: return
        val ids = when (val target = dialog.target) {
            ReplaceTarget.All -> dialog.checked.toList()
            is ReplaceTarget.One -> listOf(target.characterId)
        }
        if (ids.isEmpty()) return
        context.launch {
            close()
            val faces = mutableMapOf<String, ImageBitmap>()
            val writes = context.withBusy(BusyKind.Restoring) {
                ids.mapIndexed { index, id ->
                    context.setProgress(index, ids.size)
                    val bytes = context.repository.readBackupPortrait(backup, id)
                    faces[id] = context.images.decode(bytes)
                    PortraitWrite(id, face = bytes, cleanBase = bytes, decoration = PortraitDecoration())
                }
            }
            val copy = context.save(writes)
            context.update { it.withPortraitImages(faces).copy(notice = Notice(NoticeKind.Restored, copy)) }
        }
    }

    /** Удаляет отмеченные копии; окно остаётся открытым со свежим списком. */
    fun deleteMarked() {
        val dialog = context.current.restoreDialog ?: return
        val names = dialog.marked.toList()
        if (names.isEmpty()) return
        cancelDelete()
        context.launch {
            val left = context.withBusy(BusyKind.Deleting) {
                context.setProgress(0, names.size)
                context.repository.deleteBackups(names) { done, total -> context.setProgress(done, total) }
            }
            val selected = dialog.selected?.takeIf { name -> left.any { it.name == name } }
            context.update {
                it.copy(
                    backups = left,
                    restoreDialog = it.restoreDialog?.afterDeletion(left, names, selected),
                    notice = Notice(NoticeKind.BackupsDeleted, names.size.toString()),
                )
            }
            if (selected == null) left.firstOrNull()?.let { select(it.name) }
        }
    }

    /** Превью грузятся по одному в фоне: окно открывается сразу, портреты появляются по мере чтения. */
    private fun loadPreviews(items: List<Pair<String, String>>) {
        previewJob?.cancel()
        updateDialog { it.copy(isLoadingPreviews = true) }
        previewJob = context.launch {
            try {
                items.forEach { (backup, id) ->
                    val key = previewKey(backup, id)
                    if (context.current.restoreDialog?.previews?.containsKey(key) == true) return@forEach
                    val image = context.images.decode(context.repository.readBackupPortrait(backup, id))
                    updateDialog { it.copy(previews = it.previews + (key to image)) }
                    context.breathe()
                }
            } finally {
                updateDialog { it.copy(isLoadingPreviews = false) }
            }
        }
    }

    private fun RestoreDialogState.afterDeletion(left: List<BackupInfo>, deleted: List<String>, selected: String?) = copy(
        backups = left,
        marked = emptySet(),
        selected = selected,
        checked = if (selected == null) emptySet() else checked,
        previews = previews.filterKeys { key -> key.substringBefore('/') !in deleted },
    )

    private fun updateDialog(transform: (RestoreDialogState) -> RestoreDialogState) =
        context.update { it.copy(restoreDialog = it.restoreDialog?.let(transform)) }

    private fun Set<String>.toggled(value: String) = if (value in this) this - value else this + value
}
