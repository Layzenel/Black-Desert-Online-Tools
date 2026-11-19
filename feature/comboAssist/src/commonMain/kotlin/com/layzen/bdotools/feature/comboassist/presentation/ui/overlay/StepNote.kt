package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/** Пометка игрока к шагу «что жать сейчас» ([com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell.note]); пусто — шаг без пометки. */
internal fun nextStepNote(combo: Combo, overlay: OverlayState): String =
    overlay.next?.node?.at?.let { combo[it]?.note }.orEmpty().trim()

/** Пометка шага под трекером: оранжевым, как заметка персонажа в игре, не шире [ComboDimens.noteMaxWidth]. */
@Composable
internal fun StepNote(note: String, modifier: Modifier = Modifier) {
    if (note.isEmpty()) return
    Text(
        note, modifier.widthIn(max = ComboDimens.noteMaxWidth),
        style = AppTheme.typography.caption, color = AppTheme.colors.textNote, maxLines = NOTE_LINES, overflow = TextOverflow.Ellipsis,
    )
}

private const val NOTE_LINES = 2
