package com.layzen.bdotools.feature.portraits.presentation.controller

import com.layzen.bdotools.feature.portraits.data.PortraitWrite
import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.domain.BadgeSlot
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraits.presentation.render.toBmp
import com.layzen.bdotools.feature.portraits.presentation.state.BusyKind
import com.layzen.bdotools.feature.portraits.presentation.state.DecorationDialogState
import com.layzen.bdotools.feature.portraits.presentation.state.Notice
import com.layzen.bdotools.feature.portraits.presentation.state.NoticeKind
import com.layzen.bdotools.feature.portraits.presentation.toNotice

/**
 * Надпись и значки одного портрета. Рисуются всегда поверх чистой основы, поэтому их можно менять
 * сколько угодно; пустое украшение возвращает в игру чистый портрет.
 */
internal class DecorationController(private val context: PortraitsContext) {

    /** Байты чистого портрета открытого окна: сохраняются как основа без перекодирования. */
    private var baseBytes: ByteArray? = null

    fun open(characterId: String) {
        val draft = context.current.decorationOf(characterId)
        context.update { it.copy(decorationDialog = DecorationDialogState(characterId, draft)) }
        context.launch(onError = { e -> close().also { context.showNotice(e.toNotice()) } }) {
            val bytes = context.repository.readCleanBase(characterId)
            context.breathe()
            val base = context.images.decode(bytes)
            baseBytes = bytes
            updateDialog { if (it.characterId == characterId) it.copy(base = base) else it }
        }
    }

    fun changeCaption(text: String) = updateDraft { it.withCaption(text) }

    fun changeBadge(slot: BadgeSlot, icon: BadgeIcon?) = updateDraft { it.withBadge(slot, icon) }

    fun close() {
        baseBytes = null
        context.update { it.copy(decorationDialog = null) }
    }

    fun apply() {
        val dialog = context.current.decorationDialog ?: return
        val base = dialog.base ?: return
        val bytes = baseBytes ?: return
        val decoration = dialog.draft.withCaption(dialog.draft.caption.trim())
        context.launch {
            close()
            val face = context.withBusy(BusyKind.Rendering) { context.images.decorate(base, decoration) }
            val faceBytes = if (decoration.isEmpty) bytes else face.toBmp()
            val backup = context.save(listOf(PortraitWrite(dialog.characterId, faceBytes, bytes, decoration)))
            context.update {
                it.withPortraitImages(mapOf(dialog.characterId to face)).copy(notice = Notice(NoticeKind.DecorationSaved, backup))
            }
        }
    }

    private fun updateDialog(transform: (DecorationDialogState) -> DecorationDialogState) =
        context.update { it.copy(decorationDialog = it.decorationDialog?.let(transform)) }

    private fun updateDraft(transform: (PortraitDecoration) -> PortraitDecoration) =
        updateDialog { it.copy(draft = transform(it.draft)) }
}
