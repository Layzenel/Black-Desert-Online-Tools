package com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.cells
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillHover
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_fillers_description
import com.layzen.bdotools.feature.comboassist.resources.combo_fillers_search
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.combo_section_fillers_count
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.CloseButton
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Филлеры комбо галочками: сверху — включённые (в порядке включения), ниже — остальные умения класса не из комбо
 * по алфавиту. Снятая галочка возвращает умение в общий список на его место по алфавиту.
 */
@Composable
internal fun FillersSection(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val combo = state.combo ?: return
    val language = stringResource(Res.string.combo_names_language)
    var query by remember { mutableStateOf("") }
    var open by remember { mutableStateOf(false) }
    // список свёрнут: раскрывается кликом по заголовку, в заголовке — сколько филлеров выбрано
    Row(
        Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand).clickable { open = !open },
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExpandArrow(open)
        SectionHeader(
            stringResource(Res.string.combo_section_fillers_count, combo.fillers.size),
            description = if (open) stringResource(Res.string.combo_fillers_description) else null,
        )
    }
    if (!open) return
    InputField(
        query, { query = it }, Modifier.fillMaxWidth(), placeholder = stringResource(Res.string.combo_fillers_search),
        trailing = { if (query.isNotEmpty()) CloseButton({ query = "" }) },
    )
    val inCombo = combo.cells().mapNotNull { it.second.skillNo }.toSet()
    val matches = rememberSkillSearch(query)
    val chosen = combo.fillers.mapNotNull { state.skills[it] }.filter(matches)
    val pool = state.skills.values
        .filter { it.skillNo !in inCombo && it.skillNo !in combo.fillers && !it.skill.onlyAfter && it.skill.cooldownMs > 0 && it.damage != false && matches(it) }
        .sortedBy { it.skill.name(language).lowercase() }
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxWidth().heightIn(max = ComboDimens.paletteHeight).verticalScrollbar(scroll).verticalScroll(scroll)) {
        (chosen + pool).forEach { item ->
            val checked = item.skillNo in combo.fillers
            val toggle = { onEvent(if (checked) ComboEvent.RemoveFiller(item.skillNo) else ComboEvent.AddFiller(item.skillNo)) }
            SkillHover(item, loadDetails, beside = true) {
                MenuItem(
                    item.skill.name(language), toggle, Modifier.fillMaxWidth(),
                    icon = {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalAlignment = Alignment.CenterVertically) {
                            CheckBox(checked, { toggle() })
                            SkillIconView(item.icon, ComboDimens.paletteIcon)
                        }
                    },
                    selected = checked,
                )
            }
        }
    }
}

/** Треугольник раскрытия: вправо — свёрнуто, вниз — раскрыто. Нарисован: символов ▸ ▾ в шрифте сайта нет. */
@Composable
private fun ExpandArrow(open: Boolean) {
    val color = AppTheme.colors.textBeige
    Canvas(Modifier.size(ComboDimens.expandArrow)) {
        val path = Path().apply {
            if (open) {
                moveTo(0f, size.height * ARROW_INSET); lineTo(size.width, size.height * ARROW_INSET); lineTo(size.width / 2, size.height * (1 - ARROW_INSET))
            } else {
                moveTo(size.width * ARROW_INSET, 0f); lineTo(size.width * (1 - ARROW_INSET), size.height / 2); lineTo(size.width * ARROW_INSET, size.height)
            }
            close()
        }
        drawPath(path, color)
    }
}

private const val ARROW_INSET = 0.2f
