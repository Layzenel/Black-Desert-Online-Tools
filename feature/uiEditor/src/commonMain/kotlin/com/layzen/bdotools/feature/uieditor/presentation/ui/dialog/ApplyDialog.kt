package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.game.GamePreset
import com.layzen.bdotools.feature.uieditor.domain.skill.CharacterSkills
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.CharacterPortrait
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.characterTitle
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.title
import com.layzen.bdotools.feature.uieditor.presentation.ui.gamePresetTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_confirm
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_description
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_skills
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_target
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_title
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * «Применить в игру»: в какой набор позиций записать и предупреждение, что игра должна быть закрыта.
 * [skillTargets] — персонажи (с портретом и подписью из [skills]), которым запишутся навыки и надписи слотов перезарядки;
 * пусто — они не менялись.
 */
@Composable
internal fun ApplyDialog(
    dialog: SlotsDialog.ConfirmApply,
    presets: List<GamePreset>,
    skillTargets: List<CharacterSkills>,
    skills: SkillsState,
    onEvent: (SlotsEvent) -> Unit,
) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    ModalWindow(
        title = stringResource(Res.string.slots_apply_title),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.slots_cancel), onClick = close)
            ActionButton(stringResource(Res.string.slots_apply_confirm), onClick = { onEvent(SlotsEvent.ConfirmApply) })
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
            SectionHeader(stringResource(Res.string.slots_apply_target))
            OptionGroup(presets, dialog.target, { onEvent(SlotsEvent.SelectApplyTarget(it)) }, { gamePresetTitle(it) })
            if (skillTargets.isNotEmpty()) {
                WarningText(stringResource(Res.string.slots_apply_skills))
                skillTargets.forEach { character ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                        CharacterPortrait(skills.portraits[character.characterNo])
                        DescriptionText(characterTitle(skills, character))
                    }
                }
            }
            WarningText(stringResource(Res.string.slots_apply_description))
        }
    }
}
