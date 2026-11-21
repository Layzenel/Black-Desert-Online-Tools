package com.layzen.bdotools.feature.comboassist.presentation.ui.skill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import com.layzen.bdotools.composable.skill.SkillTooltipContent
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.combo_tooltip_cell
import com.layzen.bdotools.feature.comboassist.resources.combo_tooltip_cell_chain
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.TooltipPanel
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Подсказка с карточкой умения [item] вокруг [content] — тот же тултип, что в редакторе интерфейса
 * ([SkillTooltipContent]): описание, требования, команда и эффекты с цветами справочника. Подробности грузятся
 * при первом наведении, и тултип показывается, когда они пришли: растущая карточка у нижнего края окна
 * перепрыгивала из-под иконки наверх. [beside] — справа от элемента (строки списка в меню). [cell] — что это за ячейка в комбо.
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
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    // null — ещё не грузили; загружено, но без текста — подробностей нет
    var details by remember(item.skillNo) { mutableStateOf<Result<SkillDetails?>?>(null) }
    LaunchedEffect(hovered, item.skillNo) { if (hovered && details == null) details = Result.success(loadDetails(item.skillNo)) }
    Box(Modifier.hoverable(interaction)) {
        HoverTooltip(
            tooltip = { TooltipPanel { SkillTooltipCard(item, details?.getOrNull(), cell) } },
            enabled = details != null, beside = beside,
        ) { content() }
    }
}

/** Карточка умения [item] с загруженными подробностями [details] (`null` — подробностей нет). */
@Composable
internal fun SkillTooltipCard(item: SkillItem, details: SkillDetails?, cell: CellNote?) {
    val language = stringResource(Res.string.combo_names_language)
    val text = details?.text(language)
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SkillTooltipContent(item.skill.name(language), item.icon, text, KeyBindings.Default, failed = text == null)
        cell?.let { CellLine(it) }
    }
}

/** Место умения в комбо: подпись ячейки и связка. */
@Composable
private fun CellLine(cell: CellNote) {
    val parts = listOfNotNull(
        stringResource(Res.string.combo_tooltip_cell, cell.label),
        stringResource(Res.string.combo_tooltip_cell_chain).takeIf { cell.chain },
    )
    Text(parts.joinToString(" · "), style = AppTheme.typography.bodyBold, color = AppTheme.colors.accentGold)
}

/** Перезарядка в секундах для подписи: «8» или «1.5». */
internal fun cooldownSeconds(ms: Long): String =
    if (ms % MS_PER_SECOND == 0L) (ms / MS_PER_SECOND).toString() else "${ms / MS_PER_SECOND}.${ms % MS_PER_SECOND / TENTH_MS}"

private const val MS_PER_SECOND = 1000L
private const val TENTH_MS = 100L
