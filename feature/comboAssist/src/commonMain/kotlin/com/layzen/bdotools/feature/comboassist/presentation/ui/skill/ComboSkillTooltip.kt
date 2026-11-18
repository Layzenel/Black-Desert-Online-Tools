package com.layzen.bdotools.feature.comboassist.presentation.ui.skill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.layzen.bdotools.composable.skill.SkillTooltipContent
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.combo_tooltip_cell
import com.layzen.bdotools.feature.comboassist.resources.combo_tooltip_cell_chain
import com.layzen.bdotools.feature.comboassist.resources.combo_tooltip_cell_optional
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.TooltipPanel
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Подсказка с карточкой умения [item] вокруг [content] — тот же тултип, что в редакторе интерфейса
 * ([SkillTooltipContent]): описание, требования, команда и эффекты с цветами справочника. Подробности грузятся
 * при наведении. [beside] — справа от элемента (строки списка в меню). [cell] — что это за ячейка в комбо.
 */
@Composable
internal fun SkillHover(
    item: SkillItem?,
    loadDetails: suspend (Int) -> SkillDetails?,
    beside: Boolean = false,
    cell: CellNote? = null,
    content: @Composable () -> Unit,
) {
    if (item == null) return content()
    HoverTooltip(tooltip = { TooltipPanel { SkillTooltipCard(item, loadDetails, cell) } }, beside = beside) { content() }
}

@Composable
internal fun SkillTooltipCard(item: SkillItem, loadDetails: suspend (Int) -> SkillDetails?, cell: CellNote?) {
    val language = stringResource(Res.string.combo_names_language)
    // null — ещё грузится; загружено, но без текста — подробностей нет
    val details by produceState<Result<SkillDetails?>?>(null, item.skillNo) { value = Result.success(loadDetails(item.skillNo)) }
    val text = details?.getOrNull()?.text(language)
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SkillTooltipContent(item.skill.name(language), item.icon, text, KeyBindings.Default, failed = details != null && text == null)
        cell?.let { CellLine(it) }
    }
}

/** Место умения в комбо: подпись ячейки, связка, опциональность. */
@Composable
private fun CellLine(cell: CellNote) {
    val parts = listOfNotNull(
        stringResource(Res.string.combo_tooltip_cell, cell.label),
        stringResource(Res.string.combo_tooltip_cell_chain).takeIf { cell.chain },
        stringResource(Res.string.combo_tooltip_cell_optional).takeIf { cell.optional },
    )
    Text(parts.joinToString(" · "), style = AppTheme.typography.bodyBold, color = AppTheme.colors.accentGold)
}

/** Перезарядка в секундах для подписи: «8» или «1.5». */
internal fun cooldownSeconds(ms: Long): String =
    if (ms % MS_PER_SECOND == 0L) (ms / MS_PER_SECOND).toString() else "${ms / MS_PER_SECOND}.${ms % MS_PER_SECOND / TENTH_MS}"

private const val MS_PER_SECOND = 1000L
private const val TENTH_MS = 100L
