package com.layzen.bdotools.feature.uieditor.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.uieditor.domain.arrangement.SnapGuides
import com.layzen.bdotools.feature.uieditor.domain.game.LoadedGame
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout

/**
 * Состояние страницы раскладки слотов.
 *
 * @property loaded прочитанный `gamevariable.xml`; `null`, пока файл не прочитан.
 * @property layout раскладка в редакторе — её и запишет «Применить в игру».
 * @property history предыдущие раскладки для «Отменить», последняя — в конце.
 * @property selectedChats выбранные отдельные окна чата (`WindowIndex`): двигаются вместе с выбранными элементами.
 * @property future отменённые раскладки для «Повторить», ближайшая — в конце; новая правка их забывает.
 * @property activePreset пресет, из которого взята раскладка; `null` после правки руками.
 * @property snapToGrid привязка к узлам сетки с шагом [snapStep], отсчитанным от прицела; по умолчанию выключена.
 * @property snapToSlots прилипание двигаемого к краям и серединам других элементов и к осям прицела.
 * @property snapDistance с какого расстояния, UI-px, срабатывает прилипание к элементам.
 * @property distributeGapX промежуток между рамками, UI-px, с которым «Распределить по горизонтали» ставит элементы друг за другом.
 * @property distributeGapY то же для «Распределить по вертикали»: у каждого направления свой промежуток.
 * @property snapGuides линии, к которым двигаемый элемент прилип сейчас; пусты вне перетаскивания.
 * @property backgroundShown видна ли подложка [background] на холсте: её можно спрятать, не убирая картинку.
 * @property backgroundDim затенение подложки в процентах, общее для редактора и превью; 0 — без затенения.
 */
data class SlotsState(
    val access: FolderAccess? = null,
    val loaded: LoadedGame? = null,
    val layout: SlotLayout = SlotLayout(),
    val history: List<SlotLayout> = emptyList(),
    val future: List<SlotLayout> = emptyList(),
    val selection: Set<SlotId> = emptySet(),
    val selectedChats: Set<Int> = emptySet(),
    val presets: List<NamedLayout> = emptyList(),
    val activePreset: PresetRef? = PresetRef.Game,
    val grid: GridStep = GridStep.Game,
    val snapToGrid: Boolean = false,
    val snapStep: Int = DEFAULT_SNAP_STEP,
    val snapToSlots: Boolean = false,
    val snapDistance: Int = DEFAULT_SNAP_DISTANCE,
    val distributeGapX: Int = DEFAULT_DISTRIBUTE_GAP,
    val distributeGapY: Int = DEFAULT_DISTRIBUTE_GAP,
    val snapGuides: SnapGuides = SnapGuides.None,
    val background: ImageBitmap? = null,
    val backgroundShown: Boolean = true,
    val backgroundDim: Int = 50,
    val dialog: SlotsDialog? = null,
    val busy: Boolean = false,
    val notice: SlotsNotice? = null,
) {
    /** Есть ли в редакторе изменения, которых ещё нет в файле игры. */
    val hasChanges: Boolean
        get() = loaded?.game?.let {
            it.changedEntries(layout).isNotEmpty() || it.quickSlotsChanged(layout) || it.splitHpChanged(layout) ||
                it.changedChats(layout).isNotEmpty()
        } == true

    /**
     * Элементы, которые есть в файле игры и на экране при текущих «Настройке быстрых слотов» и «Разделении HP»:
     * их показывает редактор, скрытые — приглушёнными.
     */
    val slots: List<SlotId> get() = loaded?.game?.slots.orEmpty().filter { layout.isActive(it) }

    /** Сколько всего выбрано: элементов и окон чата. */
    val selectedCount: Int get() = selection.size + selectedChats.size

    /** Шаг привязки к сетке, который действует сейчас: 0, пока привязка выключена. */
    val gridSnapStep: Int get() = if (snapToGrid) snapStep else 0

    /** Промежуток «Распределить» по горизонтали ([horizontal]) или по вертикали. */
    fun distributeGap(horizontal: Boolean): Int = if (horizontal) distributeGapX else distributeGapY

    companion object {
        /** Шаг привязки по умолчанию, UI-px. */
        const val DEFAULT_SNAP_STEP = 5

        /** Расстояние прилипания к элементам по умолчанию, UI-px. */
        const val DEFAULT_SNAP_DISTANCE = 10

        /** Допустимый шаг привязки к сетке, UI-px; без привязки — выключатель [snapToGrid]. */
        val SNAP_STEP_RANGE = 1..25

        /** Промежуток между рамками при распределении по умолчанию, UI-px: вплотную, как слоты в игре. */
        const val DEFAULT_DISTRIBUTE_GAP = 0

        /** Допустимый промежуток при распределении, UI-px. */
        val DISTRIBUTE_GAP_RANGE = 0..100

        /** Допустимое затенение подложки, %: 0 — картинка как есть, 100 — совсем тёмная. */
        val BACKGROUND_DIM_RANGE = 0..100

        /** Допустимое расстояние прилипания к элементам, UI-px. */
        val SNAP_DISTANCE_RANGE = 2..25
    }
}
