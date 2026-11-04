package com.layzen.bdotools.feature.slots.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.slots.data.FolderAccess
import com.layzen.bdotools.feature.slots.data.LoadedGame
import com.layzen.bdotools.feature.slots.domain.NamedLayout
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.domain.SlotLayout

/**
 * Состояние страницы раскладки слотов.
 *
 * @property loaded прочитанный `gamevariable.xml`; `null`, пока файл не прочитан.
 * @property layout раскладка в редакторе — её и запишет «Применить в игру».
 * @property history предыдущие раскладки для «Отменить», последняя — в конце.
 * @property activePreset пресет, из которого взята раскладка; `null` после правки руками.
 */
data class SlotsState(
    val access: FolderAccess? = null,
    val accounts: List<String> = emptyList(),
    val loaded: LoadedGame? = null,
    val layout: SlotLayout = SlotLayout(),
    val history: List<SlotLayout> = emptyList(),
    val selection: Set<SlotId> = emptySet(),
    val presets: List<NamedLayout> = emptyList(),
    val activePreset: PresetRef? = PresetRef.Game,
    val grid: GridStep = GridStep.Game,
    val snapStep: Int = DEFAULT_SNAP_STEP,
    val background: ImageBitmap? = null,
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
        const val DEFAULT_SNAP_STEP = 10
    }
}
