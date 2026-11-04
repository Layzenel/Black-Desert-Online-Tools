package com.layzen.bdotools.feature.slots.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.core.folder.FolderAccess
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
    val snap: Boolean = true,
    val background: ImageBitmap? = null,
    val dialog: SlotsDialog? = null,
    val busy: Boolean = false,
    val notice: SlotsNotice? = null,
) {
    /** Есть ли в редакторе изменения, которых ещё нет в файле игры. */
    val hasChanges: Boolean get() = loaded?.game?.changedEntries(layout)?.isNotEmpty() == true

    /** Слоты, которые игра показывает: только их видно в редакторе. */
    val visibleSlots: List<SlotId> get() = loaded?.game?.visibleSlots.orEmpty()
}
