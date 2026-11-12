package com.layzen.bdotools.feature.portraitconstructor.presentation.controller

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.DecorationStyle
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitWrite
import com.layzen.bdotools.feature.portraitconstructor.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.toBmp
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.BusyKind
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.DecorationColorsState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.Notice
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.NoticeKind

/**
 * Цвет заметок и значков сразу у всех портретов. Украшение рисуется поверх чистой основы, поэтому портреты
 * перерисовываются с теми же заметками и значками, а записываются только те, у которых они есть.
 */
internal class DecorationColorsController(private val context: PortraitsContext) {

    /**
     * Открывает окно со стилем первого портрета с заметкой или значком (обычно он у всех одинаковый) и в фоне
     * читает его чистую основу для превью. Не прочиталась — окно работает и без превью.
     */
    fun open() {
        val first = context.current.portraits.map { it.characterId }.firstOrNull { !context.current.decorationOf(it).isEmpty }
        val decoration = first?.let(context.current::decorationOf)
        context.update { it.copy(decorationColors = DecorationColorsState(decoration?.style ?: DecorationStyle(), decoration)) }
        if (first == null) return
        context.launch(onError = {}) {
            val base = context.images.decode(context.portraits.readCleanBase(first))
            updateDialog { it.copy(previewBase = base) }
        }
    }

    /** Меняет цвета и затенение для всех портретов. */
    fun changeStyle(style: DecorationStyle) = updateDialog { it.copy(style = style) }

    /** Закрывает окно без записи. */
    fun close() = context.update { it.copy(decorationColors = null) }

    /** Перерисовывает украшенные портреты в выбранных цветах, показывает их в сетке и записывает одной копией. */
    fun apply() {
        val dialog = context.current.decorationColors ?: return
        close()
        val targets = context.current.portraits.map { it.characterId }
            .map { it to context.current.decorationOf(it) }
            .filter { (_, decoration) -> !decoration.isEmpty }
            .map { (id, decoration) -> id to decoration.copy(style = dialog.style) }
            .filter { (id, decoration) -> decoration != context.current.decorationOf(id) }
        if (targets.isEmpty()) return
        context.launch {
            val faces = mutableMapOf<String, ImageBitmap>()
            val writes = context.withBusy(BusyKind.Rendering) {
                targets.mapIndexed { done, (id, decoration) ->
                    context.setProgress(done, targets.size)
                    val clean = context.portraits.readCleanBase(id)
                    val face = context.images.decorate(context.images.decode(clean), decoration)
                    faces[id] = face
                    PortraitWrite(id, face.toBmp(), clean, decoration)
                }
            }
            val backup = context.save(writes)
            context.update { it.withPortraitImages(faces).copy(notice = Notice(NoticeKind.DecorationSaved, backup)) }
        }
    }

    private fun updateDialog(transform: (DecorationColorsState) -> DecorationColorsState) =
        context.update { it.copy(decorationColors = it.decorationColors?.let(transform)) }
}
