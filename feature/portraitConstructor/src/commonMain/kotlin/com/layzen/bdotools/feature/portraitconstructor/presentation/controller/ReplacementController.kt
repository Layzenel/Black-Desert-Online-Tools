package com.layzen.bdotools.feature.portraitconstructor.presentation.controller

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.data.ImageSource
import com.layzen.bdotools.feature.portraitconstructor.data.PortraitWrite
import com.layzen.bdotools.feature.portraitconstructor.domain.ImagePlacement
import com.layzen.bdotools.feature.portraitconstructor.domain.PortraitGrid
import com.layzen.bdotools.feature.portraitconstructor.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitRenderer
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.toBmp
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.BusyKind
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.EditorState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.Notice
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.NoticeKind
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.SourceDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.toNotice

/**
 * Замена портретов: выбор картинки (файл или ссылка), подгонка под маску и запись.
 * Новые портреты становятся чистой основой, а надпись и значки персонажа рисуются поверх них заново.
 */
internal class ReplacementController(
    private val context: PortraitsContext,
    private val imageSource: ImageSource,
) {
    fun openSource(target: ReplaceTarget) = context.update { it.copy(sourceDialog = SourceDialogState(target)) }

    fun closeSource() = context.update { it.copy(sourceDialog = null) }

    fun changeUrl(url: String) = updateSource { it.copy(url = url, error = null) }

    fun pickFromDevice() = loadImage { imageSource.pickFromDevice() }

    fun loadFromUrl() {
        val url = context.current.sourceDialog?.url?.trim().orEmpty()
        if (url.isNotEmpty()) loadImage { imageSource.download(url) }
    }

    fun updatePlacement(transform: (ImagePlacement) -> ImagePlacement) =
        updateEditor { it.copy(placement = transform(it.placement)) }

    fun resetPlacement() = updateEditor { it.copy(placement = it.initialPlacement) }

    fun toggleCell(index: Int) = updateEditor { it.toggledCell(index) }

    fun closeEditor() = context.update { it.copy(editor = null) }

    fun apply() {
        val editor = context.current.editor ?: return
        val ids = when (editor.target) {
            ReplaceTarget.All -> context.current.portraits.map { it.characterId }
            is ReplaceTarget.One -> listOf(editor.target.characterId)
        }
        // номер ячейки маски совпадает с местом персонажа на странице
        val cells = ids.indices.filter { it !in editor.disabledCells }
        context.launch {
            closeEditor()
            if (cells.isEmpty()) return@launch
            val faces = mutableMapOf<String, ImageBitmap>()
            val writes = context.withBusy(BusyKind.Rendering) {
                cells.mapIndexed { done, cell ->
                    context.setProgress(done, cells.size)
                    val id = ids[cell]
                    renderPortrait(editor, cell, id).also { (face, _) -> faces[id] = face }.second
                }
            }
            val backup = context.save(writes)
            context.update { it.withPortraitImages(faces).copy(notice = Notice(NoticeKind.Saved, backup)) }
        }
    }

    /** Ячейка [index] маски → готовый портрет для страницы и запись для файлов. */
    private suspend fun renderPortrait(editor: EditorState, index: Int, characterId: String): Pair<ImageBitmap, PortraitWrite> {
        val decoration = context.current.decorationOf(characterId)
        val clean = PortraitRenderer.renderCell(editor.image, editor.placement, editor.grid.cell(index))
        val face = context.images.decorate(clean, decoration)
        return face to PortraitWrite(characterId, face = face.toBmp(), cleanBase = clean.toBmp(), decoration = decoration)
    }

    private fun loadImage(load: suspend () -> ByteArray?) {
        val dialog = context.current.sourceDialog ?: return
        updateSource { it.copy(isLoading = true, error = null) }
        context.launch(onError = { e -> updateSource { it.copy(isLoading = false, error = e.toNotice()) } }) {
            val bytes = load() ?: return@launch updateSource { it.copy(isLoading = false) }
            context.breathe()
            val image = context.images.decode(bytes)
            val grid = when (dialog.target) {
                ReplaceTarget.All -> PortraitGrid(count = context.current.portraits.size)
                is ReplaceTarget.One -> PortraitGrid.single()
            }
            val placement = ImagePlacement.cover(image.width, image.height, grid)
            context.update { it.copy(sourceDialog = null, editor = EditorState(dialog.target, image, grid, placement)) }
        }
    }

    private fun updateSource(transform: (SourceDialogState) -> SourceDialogState) =
        context.update { it.copy(sourceDialog = it.sourceDialog?.let(transform)) }

    private fun updateEditor(transform: (EditorState) -> EditorState) =
        context.update { it.copy(editor = it.editor?.let(transform)) }
}
