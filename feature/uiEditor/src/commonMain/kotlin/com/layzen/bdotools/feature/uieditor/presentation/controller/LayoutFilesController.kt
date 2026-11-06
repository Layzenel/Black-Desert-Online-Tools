package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.data.DeviceFiles
import com.layzen.bdotools.feature.uieditor.data.SlotsRepository
import com.layzen.bdotools.feature.uieditor.domain.AnchoredLayout
import com.layzen.bdotools.feature.uieditor.domain.LayoutCodec
import com.layzen.bdotools.feature.uieditor.domain.LayoutFormatException
import com.layzen.bdotools.feature.uieditor.domain.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.ScreenArea
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.SlotId
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext
import com.layzen.bdotools.feature.uieditor.presentation.state.PresetRef
import com.layzen.bdotools.feature.uieditor.presentation.state.PreviewMode
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsNotice

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
        // Вкладки главного окна чата — не окна: в превью и в файл попадают только главное и отдельные окна
        val current = context.current.layout.let { it.copy(chats = it.chats.filterValues { chat -> !chat.combined }) }
        val layout = NamedLayout(activeUserPreset() ?: DEFAULT_EXPORT_NAME, AnchoredLayout.of(current, screen))
        openPreview(PreviewMode.Export, layout, screen)
    }

    /** Отметить или снять элементы [ids] и окна чата [chats] в превью — по одному, группой или все сразу. */
    fun include(ids: Set<SlotId>, chats: Set<Int>, include: Boolean) = updatePreview {
        if (include) it.withIncluded(it.included + ids, it.includedChats + chats)
        else it.withIncluded(it.included - ids, it.includedChats - chats)
    }

    /**
     * Обводка рамкой [area] на мониторе превью: задетые элементы отмечаются, а если все они уже отмечены — снимаются,
     * как у галочки группы.
     */
    fun includeArea(area: ScreenArea) = updatePreview { preview ->
        val screen = preview.screen
        val touched = preview.layout.layout.positions
            .filter { (id, anchored) ->
                id.isActiveWith(preview.layout.layout.customQuickSlots) && area.touches(id, anchored.placeOn(screen), screen)
            }
            .keys
        if (touched.isEmpty()) preview
        else preview.withIncluded(if (preview.included.containsAll(touched)) preview.included - touched else preview.included + touched)
    }

    /** Монитор превью экспорта. Только для просмотра: в файл раскладка попадёт с якорями и у получателя встанет по его экрану. */
    fun setPreviewScreen(screen: ScreenSpace) = updatePreview { it.copy(screen = screen) }

    /** «Настройка быстрых слотов» раскладки в превью: на мониторе остаются хотбар или «Быстрый слот», с ней раскладка и уйдёт. */
    fun setPreviewQuickSlots(enabled: Boolean) = updatePreview {
        it.copy(layout = it.layout.copy(layout = it.layout.layout.withCustomQuickSlots(enabled)))
    }

    fun undoPreview() = updatePreview { it.undone() }

    fun redoPreview() = updatePreview { it.redone() }

    fun confirmPreview() {
        val preview = context.current.dialog as? SlotsDialog.Preview ?: return
        val layout = preview.layout.copy(layout = preview.layout.layout.only(preview.included, preview.includedChats))
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
        // Файл без настройки быстрых слотов её не меняет: превью показывает ту, что сейчас в редакторе
        val quickSlots = imported.layout.customQuickSlots ?: context.current.layout.customQuickSlots
        // Только окна, которые у игрока есть отдельными: вкладки главного окна и чужие окна не показываем и не импортируем
        val ownWindows = context.current.layout.chats.filterValues { !it.combined }.keys
        val ownChats = imported.layout.chats.filter { (window, chat) -> window in ownWindows && !chat.combined }
        openPreview(PreviewMode.Import, imported.copy(layout = imported.layout.copy(customQuickSlots = quickSlots, chats = ownChats)), screen)
    }

    private fun openPreview(mode: PreviewMode, layout: NamedLayout, screen: ScreenSpace) = context.update {
        it.copy(dialog = SlotsDialog.Preview(mode, layout, screen, layout.layout.positions.keys, layout.layout.chats.keys))
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
