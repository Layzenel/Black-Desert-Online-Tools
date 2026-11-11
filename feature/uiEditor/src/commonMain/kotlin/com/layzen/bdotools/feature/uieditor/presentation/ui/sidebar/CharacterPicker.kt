package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.CharacterPortrait
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.characterTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_character
import com.layzen.bdotools.feature.uieditor.resources.slots_character_description
import com.layzen.bdotools.feature.uieditor.resources.slots_character_none
import com.layzen.bdotools.feature.uieditor.resources.slots_skills_unavailable
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Пункт «Не выбран» в списке персонажей. */
private const val NONE = -1

/**
 * Выбор персонажа, чьи навыки и надписи в слотах перезарядки: портрет из `FaceTexture` и подпись
 * ([characterTitle]). Навыки выбранного персонажа попадают в редактор, «Применить» записывает их ему.
 */
@Composable
internal fun CharacterPicker(skills: SkillsState, onEvent: (SlotsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SectionHeader(
            stringResource(Res.string.slots_character),
            description = stringResource(Res.string.slots_character_description),
        )
        DropdownSelect(
            selected = skills.characters.indexOfFirst { it.characterNo == skills.selected },
            options = listOf(NONE) + skills.characters.indices,
            onSelect = { index -> onEvent(SlotsEvent.SelectCharacter(skills.characters.getOrNull(index)?.characterNo)) },
            label = { index ->
                skills.characters.getOrNull(index)?.let { characterTitle(skills, it) } ?: stringResource(Res.string.slots_character_none)
            },
            icon = { index -> skills.characters.getOrNull(index)?.let { CharacterPortrait(skills.portraits[it.characterNo]) } },
        )
        if (skills.catalogFailed) WarningText(stringResource(Res.string.slots_skills_unavailable))
    }
}
