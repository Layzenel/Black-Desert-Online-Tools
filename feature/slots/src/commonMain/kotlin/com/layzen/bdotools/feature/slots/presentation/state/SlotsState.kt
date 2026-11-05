package com.layzen.bdotools.feature.slots.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.slots.data.LoadedGame
import com.layzen.bdotools.feature.slots.domain.NamedLayout
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.domain.SlotLayout
import com.layzen.bdotools.feature.slots.domain.SnapGuides

/**
 * Состояние страницы раскладки слотов.
 *
 * @property loaded прочитанный `gamevariable.xml`; `null`, пока файл не прочитан.
 * @property layout раскладка в редакторе — её и запишет «Применить в игру».
 * @property history предыдущие раскладки для «Отменить», последняя — в конце.
 * @property future отменённые раскладки для «Повторить», ближайшая — в конце; новая правка их забывает.
 * @property activePreset пресет, из которого взята раскладка; `null` после правки руками.
 * @property snapToSlots прилипание двигаемого к краям и серединам других элементов и к осям прицела.
 * @property snapDistance с какого расстояния, UI-px, срабатывает прилипание к элементам.
 * @property snapGuides линии, к которым двигаемый элемент прилип сейчас; пусты вне перетаскивания.
 * @property backgroundShown видна ли подложка [background] на холсте: её можно спрятать, не убирая картинку.
 */
data class SlotsState(
    val access: FolderAccess? = null,
    val loaded: LoadedGame? = null,
    val layout: SlotLayout = SlotLayout(),
    val history: List<SlotLayout> = emptyList(),
    val future: List<SlotLayout> = emptyList(),
    val selection: Set<SlotId> = emptySet(),
    val presets: List<NamedLayout> = emptyList(),
    val activePreset: PresetRef? = PresetRef.Game,
    val grid: GridStep = GridStep.Game,
    val snapStep: Int = DEFAULT_SNAP_STEP,
    val snapToSlots: Boolean = false,
    val snapDistance: Int = DEFAULT_SNAP_DISTANCE,
    val snapGuides: SnapGuides = SnapGuides.None,
    val background: ImageBitmap? = null,
    val backgroundShown: Boolean = true,
    val dialog: SlotsDialog? = null,
    val busy: Boolean = false,
    val notice: SlotsNotice? = null,
) {
    /** Есть ли в редакторе изменения, которых ещё нет в файле игры. */
    val hasChanges: Boolean get() = loaded?.game?.changedEntries(layout)?.isNotEmpty() == true

    /** Элементы, которые есть в файле игры: их показывает редактор, скрытые — приглушёнными. */
    val slots: List<SlotId> get() = loaded?.game?.slots.orEmpty()

    companion object {
        /** Шаг привязки по умолчанию, UI-px. */
        const val DEFAULT_SNAP_STEP = 5

        /** Расстояние прилипания к элементам по умолчанию, UI-px. */
        const val DEFAULT_SNAP_DISTANCE = 10

        /** Допустимый шаг привязки к сетке, UI-px; 0 — без привязки. */
        val SNAP_STEP_RANGE = 0..20

        /** Допустимое расстояние прилипания к элементам, UI-px. */
        val SNAP_DISTANCE_RANGE = 2..30
    }
}
