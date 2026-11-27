package com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillHover
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.ExpandableHeader
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Свёрнутый список умений класса галочками (филлеры, пул): клик по заголовку [title] раскрывает его, с пояснением
 * [description] и поиском [searchPlaceholder]. Сверху — отмеченные [checked] (в порядке отметки), ниже — остальные
 * умения [skills], подходящие под [candidate], по алфавиту. Снятая галочка возвращает умение на его место.
 * [onToggle] получает умение и новое состояние галочки.
 */
@Composable
internal fun SkillChecklistSection(
    title: String,
    description: String,
    searchPlaceholder: String,
    checked: List<Int>,
    skills: Map<Int, SkillItem>,
    candidate: (SkillItem) -> Boolean,
    onToggle: (Int, Boolean) -> Unit,
    loadDetails: suspend (Int) -> SkillDetails?,
) {
    val language = stringResource(Res.string.combo_names_language)
    var query by remember { mutableStateOf("") }
    var open by remember { mutableStateOf(false) }
    ExpandableHeader(title, open, { open = !open }, description)
    if (!open) return
    InputField(
        query, { query = it }, Modifier.fillMaxWidth(), placeholder = searchPlaceholder,
    )
    val matches = rememberSkillSearch(query)
    val chosen = checked.mapNotNull { skills[it] }.filter(matches)
    val others = skills.values
        .filter { it.skillNo !in checked && candidate(it) && matches(it) }
        .sortedBy { it.skill.name(language).lowercase() }
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxWidth().heightIn(max = ComboDimens.paletteHeight).verticalScrollbar(scroll).verticalScroll(scroll)) {
        (chosen + others).forEach { item ->
            val on = item.skillNo in checked
            val toggle = { onToggle(item.skillNo, !on) }
            SkillHover(item, loadDetails, beside = true) {
                MenuItem(
                    item.skill.name(language), toggle, Modifier.fillMaxWidth(),
                    icon = {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalAlignment = Alignment.CenterVertically) {
                            CheckBox(on, { toggle() })
                            SkillIconView(item.icon, ComboDimens.paletteIcon)
                        }
                    },
                    selected = on,
                )
            }
        }
    }
}
