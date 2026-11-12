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
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.skill.CharacterSkills
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.CharacterPortrait
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.characterTitle
import com.layzen.bdotools.feature.uieditor.presentation.ui.gamePresetTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_archive_hint
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_changed_both
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_changed_memos
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_changed_skills
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_confirm
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_description
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_game_running
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_skills
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_target
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_title
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * «Применить в игру»: в какой набор позиций записать и предупреждение, что игра должна быть закрыта.
 * Ниже — персонажи, у которых навыки или надписи слотов перезарядки в файле не те, что в раскладке [layout]:
 * с портретом и подписью из [skills], что именно изменится, и отметкой — писать ли ему.
 */
@Composable
internal fun ApplyDialog(
    dialog: SlotsDialog.ConfirmApply,
    presets: List<GamePreset>,
    layout: SlotLayout,
    skills: SkillsState,
    onEvent: (SlotsEvent) -> Unit,
) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    val skillTargets = skills.skillTargets(layout)
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
                Column {
                    skillTargets.forEach { character -> ApplyCharacterRow(character, layout, skills, dialog.skipped, onEvent) }
                }
            }
            if (dialog.gameRunning) WarningText(stringResource(Res.string.slots_apply_game_running))
            WarningText(stringResource(Res.string.slots_apply_description))
            DescriptionText(stringResource(Res.string.slots_apply_archive_hint))
        }
    }
}

@Composable
private fun ApplyCharacterRow(
    character: CharacterSkills,
    layout: SlotLayout,
    skills: SkillsState,
    skipped: Set<String>,
    onEvent: (SlotsEvent) -> Unit,
) {
    val chosen = character.characterNo !in skipped
    val toggle = { onEvent(SlotsEvent.ToggleApplyCharacter(character.characterNo)) }
    MenuItem(
        characterTitle(skills, character),
        onClick = toggle,
        icon = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                CheckBox(chosen, onCheckedChange = { toggle() })
                CharacterPortrait(skills.portraits[character.characterNo])
            }
        },
        hotkey = changesText(layout, character),
        selected = chosen,
    )
}

/** Что изменится в файле персонажа: умения в слотах, заметки или и то и другое. */
@Composable
private fun changesText(layout: SlotLayout, character: CharacterSkills): String {
    val skillsChanged = layout.slotSkillsDifferFrom(character)
    val memosChanged = layout.memosDifferFrom(character)
    return stringResource(
        when {
            skillsChanged && memosChanged -> Res.string.slots_apply_changed_both
            skillsChanged -> Res.string.slots_apply_changed_skills
            else -> Res.string.slots_apply_changed_memos
        },
    )
}
