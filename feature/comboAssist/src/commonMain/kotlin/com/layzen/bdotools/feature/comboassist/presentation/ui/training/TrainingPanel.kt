package com.layzen.bdotools.feature.comboassist.presentation.ui.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.tracker.TrackerEvent
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.state.LogLine
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.ComboOverlay
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.chainText
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.eventText
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.skillName
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_log_cooldown
import com.layzen.bdotools.feature.comboassist.resources.combo_log_event
import com.layzen.bdotools.feature.comboassist.resources.combo_log_filler
import com.layzen.bdotools.feature.comboassist.resources.combo_log_step
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_fix
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_hide
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_horizontal
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_vertical
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_style_next
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_style_schema
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_move
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_show
import com.layzen.bdotools.feature.comboassist.resources.combo_press_mistake
import com.layzen.bdotools.feature.comboassist.resources.combo_press_suggested
import com.layzen.bdotools.feature.comboassist.resources.combo_reset
import com.layzen.bdotools.feature.comboassist.resources.combo_training_hint
import com.layzen.bdotools.feature.comboassist.resources.combo_watch_description
import com.layzen.bdotools.feature.comboassist.resources.combo_watch_start
import com.layzen.bdotools.feature.comboassist.resources.combo_watch_stop
import com.layzen.bdotools.feature.comboassist.resources.combo_watch_web
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Тренировка: оверлей с кликабельными иконками, пульт, слежение за игрой (Desktop) и журнал. */
@Composable
internal fun TrainingPanel(state: ComboState, canWatchGame: Boolean, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val combo = state.combo ?: return
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        ComboOverlay(combo, state.overlay, state.skills, loadDetails, onPress = { onEvent(ComboEvent.Press(it)) })
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            OptionGroup(OverlayStyle.entries, combo.overlayStyle, { onEvent(ComboEvent.SetOverlayStyle(it)) }, {
                stringResource(if (it == OverlayStyle.Schema) Res.string.combo_overlay_style_schema else Res.string.combo_overlay_style_next)
            })
            ActionButton(stringResource(if (combo.vertical) Res.string.combo_overlay_horizontal else Res.string.combo_overlay_vertical), { onEvent(ComboEvent.ToggleVertical) })
            ActionButton(stringResource(Res.string.combo_reset), { onEvent(ComboEvent.ResetTraining) })
            ActionButton(stringResource(Res.string.combo_press_mistake), { onEvent(ComboEvent.PressMistake) })
            ActionButton(stringResource(Res.string.combo_press_suggested), { onEvent(ComboEvent.PressSuggested) })
        }
        DescriptionText(stringResource(Res.string.combo_training_hint))
        if (canWatchGame) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                ActionButton(stringResource(if (state.overlayShown) Res.string.combo_overlay_hide else Res.string.combo_overlay_show), { onEvent(ComboEvent.ToggleOverlay) })
                if (state.overlayShown) ActionButton(stringResource(if (state.overlayMovable) Res.string.combo_overlay_fix else Res.string.combo_overlay_move), { onEvent(ComboEvent.ToggleOverlayMove) })
                ActionButton(stringResource(if (state.watching) Res.string.combo_watch_stop else Res.string.combo_watch_start), { onEvent(ComboEvent.ToggleWatching) })
            }
            DescriptionText(stringResource(Res.string.combo_watch_description))
        } else {
            DescriptionText(stringResource(Res.string.combo_watch_web))
        }
        LazyColumn(Modifier.heightIn(max = ComboDimens.logHeight).widthIn(max = AppTheme.dimens.contentMaxWidth), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs)) {
            items(state.log, key = { it.id }) { LogRow(it, state) }
        }
    }
}

@Composable
private fun LogRow(line: LogLine, state: ComboState) {
    val skillNo = line.onCooldown ?: line.event?.skillNo
    val name = skillName(state.skills, skillNo)
    val event = line.event
    val text = when {
        line.onCooldown != null -> stringResource(Res.string.combo_log_cooldown, name)
        event == null -> return
        event is TrackerEvent.Filler -> stringResource(Res.string.combo_log_filler, name)
        else -> {
            val detail = chainText(event.chain, state.skills) ?: eventText(event, state.skills) ?: line.where.orEmpty()
            if (event is TrackerEvent.Step) stringResource(Res.string.combo_log_step, name, detail)
            else stringResource(Res.string.combo_log_event, name.ifEmpty { line.where.orEmpty() }, detail)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS), verticalAlignment = Alignment.CenterVertically) {
        SkillIconView(skillNo?.let { state.skills[it]?.icon }, ComboDimens.logIcon)
        Text(text, style = AppTheme.typography.body, color = AppTheme.colors.textBeige)
    }
}
