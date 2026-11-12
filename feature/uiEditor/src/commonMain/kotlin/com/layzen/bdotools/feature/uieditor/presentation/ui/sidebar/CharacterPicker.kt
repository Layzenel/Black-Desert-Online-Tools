package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.resources.slots_character_hint
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.slots_frame_transparency
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.CharacterPortrait
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.characterTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_character_none
import com.layzen.bdotools.feature.uieditor.resources.slots_skills_unavailable
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Пункт «Выбрать персонажа» в списке: персонаж не выбран. */
private const val NONE = -1

/**
 * Выбор персонажа, чьи навыки и надписи в слотах перезарядки: портрет из `FaceTexture` и подпись
 * ([characterTitle]). Навыки выбранного персонажа попадают в редактор, «Применить» записывает их ему.
 * При наведении на список — подсказка, что умения и заметки у каждого персонажа свои.
 * Под списком у выбранного персонажа — прозрачность слотов перезарядки с навыком [frameTransparency], %: 100 — видны только иконки, как в игре.
 */
@Composable
internal fun CharacterPicker(skills: SkillsState, frameTransparency: Int, onEvent: (SlotsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        HoverTooltip(stringResource(Res.string.slots_character_hint), Modifier.fillMaxWidth()) {
            DropdownSelect(
                selected = skills.characters.indexOfFirst { it.characterNo == skills.selected },
                options = listOf(NONE) + skills.characters.indices,
                onSelect = { index -> onEvent(SlotsEvent.SelectCharacter(skills.characters.getOrNull(index)?.characterNo)) },
                label = { index ->
                    skills.characters.getOrNull(index)?.let { characterTitle(skills, it) } ?: stringResource(Res.string.slots_character_none)
                },
                modifier = Modifier.fillMaxWidth(),
                icon = { index -> skills.characters.getOrNull(index)?.let { CharacterPortrait(skills.portraits[it.characterNo]) } },
                minHeight = SlotsDimens.sidebarSelectHeight,
            )
        }
        if (skills.catalogFailed) WarningText(stringResource(Res.string.slots_skills_unavailable))
        if (skills.selected != null) {
            DescriptionText(stringResource(Res.string.slots_frame_transparency))
            ValueSlider(frameTransparency, { onEvent(SlotsEvent.SetSkillFrameTransparency(it)) }, range = SlotsState.SKILL_FRAME_TRANSPARENCY_RANGE)
        }
    }
}
