package com.layzen.bdotools.composable.skill

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.composable.resources.Res
import com.layzen.bdotools.composable.resources.skill_tooltip_failed
import com.layzen.bdotools.composable.resources.skill_tooltip_loading
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.core.skill.domain.SkillText
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Полный тултип навыка, как в окне умений игры: иконка [icon] в золотистой рамке и название [name] над разделителем,
 * затем блоки описания, требований, команды и эффектов ([SkillTooltipBody]) по подробностям [text] с клавишами [keys].
 * Пока [text] нет — «загрузка», а при [failed] — предупреждение. Загрузку подробностей делает вызывающий.
 * Рамку всего тултипа рисует `TooltipPanel`.
 */
@Composable
fun SkillTooltipContent(
    name: String,
    icon: ImageBitmap?,
    text: SkillText?,
    keys: KeyBindings,
    failed: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.width(SkillTooltipDimens.width).padding(vertical = AppTheme.dimens.spaceXs),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
            Box(Modifier.border(AppTheme.dimens.borderWidth, AppTheme.colors.accentGoldPale).padding(AppTheme.dimens.tooltipFrameGap)) {
                SkillIcon(icon, SkillTooltipDimens.icon)
            }
            Text(name, style = AppTheme.typography.windowTitle, color = AppTheme.colors.textPrimary)
        }
        SectionDivider()
        when {
            text != null -> SkillTooltipBody(text, keys)
            failed -> WarningText(stringResource(Res.string.skill_tooltip_failed))
            else -> DescriptionText(stringResource(Res.string.skill_tooltip_loading))
        }
    }
}
