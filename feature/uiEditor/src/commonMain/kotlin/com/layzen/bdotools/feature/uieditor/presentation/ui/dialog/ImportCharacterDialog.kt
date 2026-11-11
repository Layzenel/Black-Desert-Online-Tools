package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.CharacterPortrait
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.characterTitle
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.className
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.feature.uieditor.resources.slots_import_character_description
import com.layzen.bdotools.feature.uieditor.resources.slots_import_character_none
import com.layzen.bdotools.feature.uieditor.resources.slots_import_character_title
import com.layzen.bdotools.feature.uieditor.resources.slots_import_characters_confirm
import com.layzen.bdotools.feature.uieditor.resources.slots_import_without_skills
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Импорт с навыками: каким персонажам их поставить. В списке — персонажи класса навыков импорта
 * ([SlotsDialog.ImportCharacter.classKey]) и персонажи с пустыми слотами; класс не определить — все. Отмечают
 * нескольких: первый попадает в редактор, «Применить» запишет навыки всем. «Без умений» импортирует только расположение.
 */
@Composable
internal fun ImportCharacterDialog(dialog: SlotsDialog.ImportCharacter, skills: SkillsState, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    val fitting = skills.characters.filter { skills.fitsClass(it, dialog.classKey) }
    ModalWindow(
        title = stringResource(Res.string.slots_import_character_title),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.slots_cancel), onClick = close)
            ActionButton(stringResource(Res.string.slots_import_without_skills), onClick = { onEvent(SlotsEvent.ImportWithoutSkills) })
            if (dialog.chosen.isNotEmpty()) {
                ActionButton(stringResource(Res.string.slots_import_characters_confirm), onClick = { onEvent(SlotsEvent.ConfirmImportCharacters) })
            }
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            DescriptionText(stringResource(Res.string.slots_import_character_description, className(dialog.classKey)))
            if (fitting.isEmpty()) WarningText(stringResource(Res.string.slots_import_character_none))
            val scroll = rememberScrollState()
            Column(Modifier.heightIn(max = SlotsDimens.skillListHeight).verticalScrollbar(scroll).verticalScroll(scroll)) {
                fitting.forEach { character ->
                    val chosen = character.characterNo in dialog.chosen
                    val toggle = { onEvent(SlotsEvent.ToggleImportCharacter(character.characterNo)) }
                    MenuItem(
                        characterTitle(skills, character),
                        onClick = toggle,
                        icon = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                                CheckBox(chosen, onCheckedChange = { toggle() })
                                CharacterPortrait(skills.portraits[character.characterNo])
                            }
                        },
                        selected = chosen,
                    )
                }
            }
        }
    }
}
