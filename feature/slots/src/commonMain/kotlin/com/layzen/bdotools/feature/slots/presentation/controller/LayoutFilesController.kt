package com.layzen.bdotools.feature.slots.presentation.controller

import com.layzen.bdotools.feature.slots.data.DeviceFiles
import com.layzen.bdotools.feature.slots.data.SlotsRepository
import com.layzen.bdotools.feature.slots.domain.AnchoredLayout
import com.layzen.bdotools.feature.slots.domain.LayoutCodec
import com.layzen.bdotools.feature.slots.domain.LayoutFormatException
import com.layzen.bdotools.feature.slots.domain.NamedLayout
import com.layzen.bdotools.feature.slots.domain.ScreenArea
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.presentation.SlotsContext
import com.layzen.bdotools.feature.slots.presentation.state.PresetRef
import com.layzen.bdotools.feature.slots.presentation.state.PreviewMode
import com.layzen.bdotools.feature.slots.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.slots.presentation.state.SlotsNotice

/** Имя файла экспорта, если раскладка не из сохранённого пресета. */
private const val DEFAULT_EXPORT_NAME = "slot-layout"

/**
 * Импорт и экспорт файла раскладки. Перед тем как загрузить раскладку в редактор или сохранить её в файл,
 * открывается превью: в нём видно, как она ляжет на экран игры, и выбираются элементы.
 */
internal class LayoutFilesController(
    private val context: SlotsContext,
    private val repository: SlotsRepository,
    private val device: DeviceFiles,
) {

    fun openImport() = context.update { it.copy(dialog = SlotsDialog.Import("")) }

    fun changeImportText(text: String) = context.update { it.copy(dialog = SlotsDialog.Import(text)) }

    fun importFromFile() = context.launch {
        val text = device.pickLayoutFile() ?: return@launch
        openImportPreview(text)
    }

    fun confirmImport() {
        val dialog = context.current.dialog as? SlotsDialog.Import ?: return
        openImportPreview(dialog.text)
    }

    /** Экспорт начинается с превью текущей раскладки редактора. */
    fun export() {
        val screen = screen() ?: return
        val layout = NamedLayout(activeUserPreset() ?: DEFAULT_EXPORT_NAME, AnchoredLayout.of(context.current.layout, screen))
        openPreview(PreviewMode.Export, layout, screen)
    }

    /** Отметить или снять элементы [ids] в превью — по одному, группой или все сразу. */
    fun include(ids: Set<SlotId>, include: Boolean) = updatePreview {
        it.withIncluded(if (include) it.included + ids else it.included - ids)
    }

    /**
     * Обводка рамкой [area] на мониторе превью: задетые элементы отмечаются, а если все они уже отмечены — снимаются,
     * как у галочки группы.
     */
    fun includeArea(area: ScreenArea) = updatePreview { preview ->
        val screen = preview.screen
        val touched = preview.layout.layout.positions
            .filter { (id, anchored) -> area.touches(id, anchored.placeOn(screen), screen) }
            .keys
        if (touched.isEmpty()) preview
        else preview.withIncluded(if (preview.included.containsAll(touched)) preview.included - touched else preview.included + touched)
    }

    /** Монитор превью экспорта. Только для просмотра: в файл раскладка попадёт с якорями и у получателя встанет по его экрану. */
    fun setPreviewScreen(screen: ScreenSpace) = updatePreview { it.copy(screen = screen) }

    fun undoPreview() = updatePreview { it.undone() }

    fun redoPreview() = updatePreview { it.redone() }

    fun confirmPreview() {
        val preview = context.current.dialog as? SlotsDialog.Preview ?: return
        val layout = preview.layout.copy(layout = preview.layout.layout.only(preview.included))
        when (preview.mode) {
            PreviewMode.Import -> import(layout)
            PreviewMode.Export -> context.launch {
                device.download(repository.fileName(layout.name), LayoutCodec.encode(layout))
                context.update { it.copy(dialog = null) }
            }
        }
    }

    private fun openImportPreview(text: String) {
        val screen = screen() ?: return
        val imported = try {
            LayoutCodec.decode(text)
        } catch (e: LayoutFormatException) {
            context.update { it.copy(dialog = SlotsDialog.Import(text, e.error)) }
            return
        }
        openPreview(PreviewMode.Import, imported, screen)
    }

    private fun openPreview(mode: PreviewMode, layout: NamedLayout, screen: ScreenSpace) = context.update {
        it.copy(dialog = SlotsDialog.Preview(mode, layout, screen, layout.layout.positions.keys))
    }

    /**
     * Выбранная часть раскладки ставится на экран игры от своих якорей и ложится поверх текущей: элементы,
     * которых нет в ней, не меняются. Если что-то не влезло, сообщение подскажет масштаб интерфейса.
     */
    private fun import(imported: NamedLayout) {
        val screen = screen() ?: return
        context.editLayout { it.overlaidWith(imported.layout.placeOn(screen)) }
        val offScreen = imported.layout.offScreenCount(screen)
        val notice = SlotsNotice.Imported(imported.name, offScreen, imported.layout.suggestedUiScale(screen))
        context.update { it.copy(dialog = null, notice = notice) }
    }

    private fun updatePreview(change: (SlotsDialog.Preview) -> SlotsDialog.Preview) = context.update {
        val preview = it.dialog as? SlotsDialog.Preview ?: return@update it
        it.copy(dialog = change(preview))
    }

    private fun screen(): ScreenSpace? = context.current.loaded?.game?.screen

    private fun activeUserPreset(): String? = (context.current.activePreset as? PresetRef.User)?.name
}
