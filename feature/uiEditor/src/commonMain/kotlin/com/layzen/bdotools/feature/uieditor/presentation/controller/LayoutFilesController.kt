package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.domain.file.AnchoredLayout
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutCodec
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutFilesInteractor
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutFormatException
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenArea
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
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
    private val interactor: LayoutFilesInteractor,
) {

    /** Открывает диалог импорта с пустым текстом. */
    fun openImport() = context.update { it.copy(dialog = SlotsDialog.Import("")) }

    /** Запоминает текст раскладки, введённый в диалоге импорта. */
    fun changeImportText(text: String) = context.update { it.copy(dialog = SlotsDialog.Import(text)) }

    /** Даёт выбрать файл раскладки и открывает его превью. */
    fun importFromFile() = context.launch {
        val text = interactor.pickLayoutFile() ?: return@launch
        openImportPreview(text)
    }

    /** Открывает превью раскладки из текста диалога импорта. */
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
                preview.layout.layout.isActive(id) && area.touches(id, anchored.placeOn(screen), screen)
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

    /** «Разделение HP» раскладки в превью: на мониторе остаются здоровье, PVP и гнев или нет, с ним раскладка и уйдёт. */
    fun setPreviewSplitHp(enabled: Boolean) = updatePreview {
        it.copy(layout = it.layout.copy(layout = it.layout.layout.withSplitHp(enabled)))
    }

    /** Отменяет последнюю правку раскладки в превью. */
    fun undoPreview() = updatePreview { it.undone() }

    /** Повторяет отменённую правку раскладки в превью. */
    fun redoPreview() = updatePreview { it.redone() }

    /** Подтверждает превью: берёт отмеченные элементы и либо импортирует их в редактор, либо скачивает файл экспорта. */
    fun confirmPreview() {
        val preview = context.current.dialog as? SlotsDialog.Preview ?: return
        val layout = preview.layout.copy(layout = preview.layout.layout.only(preview.included, preview.includedChats))
        when (preview.mode) {
            PreviewMode.Import -> import(layout)
            PreviewMode.Export -> context.launch {
                interactor.export(layout)
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
        // Файл без настроек быстрых слотов и разделения HP их не меняет: превью показывает те, что сейчас в редакторе
        val quickSlots = imported.layout.customQuickSlots ?: context.current.layout.customQuickSlots
        val splitHp = imported.layout.splitHp ?: context.current.layout.splitHp
        // Только окна, которые у игрока есть отдельными: вкладки главного окна и чужие окна не показываем и не импортируем
        val ownWindows = context.current.layout.chats.filterValues { !it.combined }.keys
        val ownChats = imported.layout.chats.filter { (window, chat) -> window in ownWindows && !chat.combined }
        openPreview(PreviewMode.Import, imported.copy(layout = imported.layout.copy(customQuickSlots = quickSlots, splitHp = splitHp, chats = ownChats)), screen)
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
