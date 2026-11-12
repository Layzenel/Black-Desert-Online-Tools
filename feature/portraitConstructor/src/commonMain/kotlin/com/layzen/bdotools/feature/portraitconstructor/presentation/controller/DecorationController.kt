package com.layzen.bdotools.feature.portraitconstructor.presentation.controller

import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.SlotIcon
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitWrite
import com.layzen.bdotools.feature.portraitconstructor.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.toBmp
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.DecorationDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.Notice
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.NoticeKind
import com.layzen.bdotools.feature.portraitconstructor.presentation.toNotice

/**
 * Надпись и значки одного портрета. Рисуются всегда поверх чистой основы, поэтому их можно менять
 * сколько угодно; пустое украшение возвращает в игру чистый портрет.
 */
internal class DecorationController(private val context: PortraitsContext) {

    /** Байты чистого портрета открытого окна: сохраняются как основа без перекодирования. */
    private var baseBytes: ByteArray? = null

    /** Открывает окно украшения портрета [characterId] с его текущим украшением как черновиком и в фоне читает чистую основу. */
    fun open(characterId: String) {
        val draft = context.current.decorationOf(characterId)
        context.update { it.copy(decorationDialog = DecorationDialogState(characterId, draft)) }
        context.launch(onError = { e -> close().also { context.showNotice(e.toNotice()) } }) {
            val bytes = context.portraits.readCleanBase(characterId)
            context.breathe()
            val base = context.images.decode(bytes)
            baseBytes = bytes
            updateDialog { if (it.characterId == characterId) it.copy(base = base) else it }
        }
    }

    /** Меняет надпись черновика. */
    fun changeCaption(text: String) = updateDraft { it.withCaption(text) }

    /** Меняет значок черновика; `null` убирает его. */
    fun changeBadge(icon: SlotIcon?) = updateDraft { it.withBadge(icon) }

    /** Меняет цвет надписи черновика; `null` — цвет по умолчанию. */
    fun changeCaptionColor(color: Int?) = updateDraft { it.copy(captionColor = color) }

    /** Меняет цвет значка черновика; `null` — цвет по умолчанию. */
    fun changeBadgeColor(color: Int?) = updateDraft { it.copy(badgeColor = color) }

    /** Закрывает окно без сохранения. */
    fun close() {
        baseBytes = null
        context.update { it.copy(decorationDialog = null) }
    }

    /** Рисует черновик поверх основы, показывает результат в сетке и записывает портрет. */
    fun apply() {
        val dialog = context.current.decorationDialog ?: return
        val base = dialog.base ?: return
        val bytes = baseBytes ?: return
        val decoration = dialog.draft.withCaption(dialog.draft.caption.trim())
        val id = dialog.characterId
        val previous = context.current.portraits.firstOrNull { it.characterId == id }?.image
        close()
        // один портрет рисуется и пишется быстро: без окна ожидания, новая картинка сразу встаёт в сетку
        context.launch(
            onError = { e ->
                if (previous != null) context.update { it.withPortraitImages(mapOf(id to previous)) }
                context.showNotice(e.toNotice())
            },
        ) {
            context.breathe()
            val face = context.images.decorate(base, decoration)
            context.update { it.withPortraitImages(mapOf(id to face)) }
            val faceBytes = if (decoration.isEmpty) bytes else face.toBmp()
            val backup = context.saveQuietly(listOf(PortraitWrite(id, faceBytes, bytes, decoration)))
            context.showNotice(Notice(NoticeKind.DecorationSaved, backup))
        }
    }

    private fun updateDialog(transform: (DecorationDialogState) -> DecorationDialogState) =
        context.update { it.copy(decorationDialog = it.decorationDialog?.let(transform)) }

    private fun updateDraft(transform: (PortraitDecoration) -> PortraitDecoration) =
        updateDialog { it.copy(draft = transform(it.draft)) }
}
