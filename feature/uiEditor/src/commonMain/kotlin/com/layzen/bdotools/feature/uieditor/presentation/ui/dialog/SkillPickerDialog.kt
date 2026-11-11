package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.graphics.ColorFilter
import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillStance
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.character.className
import com.layzen.bdotools.feature.uieditor.presentation.ui.skill.SkillTooltip
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_names_language
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_nothing_found
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_hide_absolute
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_picker_title
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_picker_tooltip_hint
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_search
import com.layzen.bdotools.feature.uieditor.resources.slots_stance_awakening
import com.layzen.bdotools.feature.uieditor.resources.slots_stance_succession
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.TooltipPanel
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.icons.icon
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * «Добавить умение»: навыки последнего ранга класса и стойки персонажа в редакторе из справочника, с поиском по
 * названию на языке интерфейса (без учёта регистра). Клик по навыку ставит его в слот; наведение на иконку или Alt + наведение — его тултип слева от окна.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SkillPickerDialog(dialog: SlotsDialog.PickSkill, skills: SkillsState, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    val language = stringResource(Res.string.slots_skill_names_language)
    val choices = remember(skills.catalog, dialog.classKey, dialog.stance, dialog.hideAbsolute, language) {
        skills.catalog?.choicesFor(dialog.classKey, dialog.stance, dialog.hideAbsolute).orEmpty()
            .entries.sortedBy { it.value.name(language).lowercase() }
    }
    val found = choices.filter { it.value.name(language).contains(dialog.query.trim(), ignoreCase = true) }
    // Навык, чей тултип слева от окна; только вид, поэтому не в состоянии экрана
    var tooltipSkill by remember { mutableStateOf<Int?>(null) }
    ModalWindow(
        title = stringResource(Res.string.slots_skill_picker_title),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = { ActionButton(stringResource(Res.string.slots_cancel), onClick = close) },
        aside = tooltipSkill?.let { skillNo -> { TooltipPanel { SkillTooltip(skillNo, skills, onEvent) } } },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            val hasAbsoluteWithPrime = remember(skills.catalog, dialog.classKey) {
                skills.catalog?.choicesFor(dialog.classKey, SkillStance.Succession, hideAbsoluteWithPrime = false)
                    .orEmpty().values.any { it.hasPrimeVariant }
            }
            PickerFilters(dialog, showHideAbsolute = hasAbsoluteWithPrime, onEvent)
            InputField(
                value = dialog.query,
                onValueChange = { onEvent(SlotsEvent.SkillQueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(Res.string.slots_skill_search),
            )
            DescriptionText(stringResource(Res.string.slots_skill_picker_tooltip_hint))
            val altPressed = LocalWindowInfo.current.keyboardModifiers.isAltPressed
            val scroll = rememberScrollState()
            Column(Modifier.heightIn(max = SlotsDimens.skillListHeight).verticalScrollbar(scroll).verticalScroll(scroll)) {
                found.forEach { (skillNo, skill) ->
                    key(skillNo) {
                        SkillChoice(
                            name = skill.name(language),
                            icon = skills.icons[skill.icon],
                            altPressed = altPressed,
                            onPick = { onEvent(SlotsEvent.PickSkill(skillNo)) },
                            onTooltip = { show ->
                                if (show) tooltipSkill = skillNo else if (tooltipSkill == skillNo) tooltipSkill = null
                            },
                        )
                    }
                }
            }
            if (found.isEmpty()) DescriptionText(stringResource(Res.string.slots_skill_nothing_found))
        }
    }
}

/**
 * Шапка выбора: иконка и название класса персонажа, стойка списком и у наследия — «Скрыть умения «Абсолют»»,
 * если у класса есть «Абсолют» с вариантом «Мощь» ([showHideAbsolute]). У пробуждения «Мощи» нет и так:
 * её прячет сама стойка.
 */
@Composable
private fun PickerFilters(dialog: SlotsDialog.PickSkill, showHideAbsolute: Boolean, onEvent: (SlotsEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        CharacterClass.fromKey(dialog.classKey)?.let {
            Image(
                painterResource(it.icon),
                contentDescription = null,
                modifier = Modifier.size(SlotsDimens.classIcon),
                colorFilter = ColorFilter.tint(AppTheme.colors.textBeige),
            )
        }
        Text(className(dialog.classKey), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        DropdownSelect(
            selected = dialog.stance,
            options = SkillStance.entries,
            onSelect = { onEvent(SlotsEvent.SkillStanceChanged(it)) },
            label = { stanceTitle(it) },
            modifier = Modifier.width(SlotsDimens.stanceSelectWidth),
        )
    }
    if (dialog.stance == SkillStance.Succession && showHideAbsolute) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            CheckBox(dialog.hideAbsolute, onCheckedChange = { onEvent(SlotsEvent.SkillHideAbsoluteChanged(it)) })
            Text(stringResource(Res.string.slots_skill_hide_absolute), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        }
    }
}

@Composable
private fun stanceTitle(stance: SkillStance): String = stringResource(
    when (stance) {
        SkillStance.Succession -> Res.string.slots_stance_succession
        SkillStance.Awakening -> Res.string.slots_stance_awakening
    },
)
