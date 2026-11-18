package com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.core.skill.domain.SkillStance
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_class_search
import com.layzen.bdotools.feature.comboassist.resources.combo_name_placeholder
import com.layzen.bdotools.feature.comboassist.resources.combo_new
import com.layzen.bdotools.feature.comboassist.resources.combo_none
import com.layzen.bdotools.feature.comboassist.resources.combo_section_combo
import com.layzen.bdotools.feature.comboassist.resources.combo_section_class
import com.layzen.bdotools.feature.comboassist.resources.combo_section_class_description
import com.layzen.bdotools.feature.comboassist.resources.combo_section_combo_description
import com.layzen.bdotools.feature.comboassist.resources.combo_stance_all
import com.layzen.bdotools.feature.comboassist.resources.combo_stance_awakening
import com.layzen.bdotools.feature.comboassist.resources.combo_stance_succession
import com.layzen.bdotools.feature.comboassist.resources.combo_untitled
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.icons.icon
import com.layzen.bdotools.uikit.icons.title
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Меню слева: комбо и его класс, список умений, филлеры. Комбо всегда повторяется с первой ячейки. */
@Composable
internal fun ComboSidebarContent(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val tint = ColorFilter.tint(AppTheme.colors.textBeige)
    SectionHeader(stringResource(Res.string.combo_section_class), description = stringResource(Res.string.combo_section_class_description))
    DropdownSelect(
        state.characterClass, CharacterClass.entries, { onEvent(ComboEvent.SelectClass(it)) }, { stringResource(it.title) }, Modifier.fillMaxWidth(),
        icon = { Image(painterResource(it.icon), null, Modifier.size(ComboDimens.paletteIcon), colorFilter = tint) },
        searchPlaceholder = stringResource(Res.string.combo_class_search),
    )
    // у классов Освобождения стоек нет: все навыки в одном наборе
    if (!state.characterClass.ascension) StanceSelect(state, onEvent)
    SectionHeader(stringResource(Res.string.combo_section_combo), description = stringResource(Res.string.combo_section_combo_description))
    SavedCombos(state, onEvent, tint)
    MenuAction(stringResource(Res.string.combo_new), { onEvent(ComboEvent.NewCombo) })
    state.combo?.let { combo ->
        InputField(combo.name, { onEvent(ComboEvent.Rename(it)) }, Modifier.fillMaxWidth(), placeholder = stringResource(Res.string.combo_name_placeholder))
    }
    SectionDivider()
    SkillPalette(state, onEvent, loadDetails)
    SectionDivider()
    FillersSection(state, onEvent, loadDetails)
}

/**
 * Сохранённые комбо всех классов: выбор открывает комбо и переключает класс и его умения. Открытое комбо ещё
 * не сохранено (новое) — в поле «Новое комбо».
 */
@Composable
private fun SavedCombos(state: ComboState, onEvent: (ComboEvent) -> Unit, tint: ColorFilter) {
    val untitled = stringResource(Res.string.combo_untitled)
    val classOf = { combo: Combo -> CharacterClass.entries.firstOrNull { it.key == combo.classKey } }
    val saved = state.combos.firstOrNull { it.id == state.combo?.id }
    DropdownSelect(
        saved, state.combos, { onEvent(ComboEvent.SelectCombo(it.id)) },
        { combo -> listOfNotNull(combo.name.ifBlank { untitled }, classOf(combo)?.let { stringResource(it.title) }).joinToString(" · ") },
        Modifier.fillMaxWidth(),
        fieldText = when {
            saved != null -> null
            state.combo != null -> stringResource(Res.string.combo_new)
            else -> stringResource(Res.string.combo_none)
        },
        icon = { combo -> classOf(combo)?.let { Image(painterResource(it.icon), null, Modifier.size(ComboDimens.paletteIcon), colorFilter = tint) } },
    )
}

/** Стойка, умения которой показывать: наследие, пробуждение или все. */
@Composable
private fun StanceSelect(state: ComboState, onEvent: (ComboEvent) -> Unit) {
    DropdownSelect(
        state.stance, listOf(SkillStance.Succession, SkillStance.Awakening, null), { onEvent(ComboEvent.SelectStance(it)) },
        { stance ->
            when (stance) {
                SkillStance.Succession -> stringResource(Res.string.combo_stance_succession)
                SkillStance.Awakening -> stringResource(Res.string.combo_stance_awakening)
                null -> stringResource(Res.string.combo_stance_all)
            }
        },
        Modifier.fillMaxWidth(),
    )
}
