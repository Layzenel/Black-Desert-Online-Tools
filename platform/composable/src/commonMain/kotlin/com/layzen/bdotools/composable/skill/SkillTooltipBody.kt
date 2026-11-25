package com.layzen.bdotools.composable.skill

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.core.skill.domain.SkillText
import com.layzen.bdotools.composable.resources.Res
import com.layzen.bdotools.composable.resources.skill_tooltip_command
import com.layzen.bdotools.composable.resources.skill_tooltip_cooldown
import com.layzen.bdotools.composable.resources.skill_tooltip_description
import com.layzen.bdotools.composable.resources.skill_tooltip_effects
import com.layzen.bdotools.composable.resources.skill_tooltip_level
import com.layzen.bdotools.composable.resources.skill_tooltip_requirements
import com.layzen.bdotools.composable.resources.skill_tooltip_resource
import com.layzen.bdotools.composable.resources.skill_tooltip_skill_points
import com.layzen.bdotools.composable.resources.skill_tooltip_stamina
import com.layzen.bdotools.uikit.components.TooltipSection
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Блоки тултипа навыка, как разделы тултипа в игре: описание, требования (без нулевых) вместе с перезарядкой,
 * команда с клавишами персонажа [keys] и эффекты. Пустые блоки не показываются.
 */
@Composable
fun SkillTooltipBody(text: SkillText, keys: KeyBindings) {
    Section(stringResource(Res.string.skill_tooltip_description), text.description.trim(), keys)
    val requirements = listOfNotNull(
        text.requiredLevel.takeIf { it > 0 }?.let { stringResource(Res.string.skill_tooltip_level, it) },
        text.requiredSkillPoints.takeIf { it > 0 }?.let { stringResource(Res.string.skill_tooltip_skill_points, it) },
        text.requiredMp.takeIf { it > 0 }?.let { stringResource(Res.string.skill_tooltip_resource, it) },
        text.requiredSp.takeIf { it > 0 }?.let { stringResource(Res.string.skill_tooltip_stamina, it) },
    )
    if (requirements.isNotEmpty() || text.cooldownMs > 0) {
        TooltipSection(title = stringResource(Res.string.skill_tooltip_requirements)) {
            if (requirements.isNotEmpty()) {
                Text(requirements.joinToString("\n"), style = AppTheme.typography.body, color = AppTheme.colors.textPrimary)
            }
            if (text.cooldownMs > 0) {
                val seconds = "${text.cooldownMs / MS_IN_SECOND}.${text.cooldownMs % MS_IN_SECOND / MS_IN_TENTH}"
                Text(stringResource(Res.string.skill_tooltip_cooldown, seconds), style = AppTheme.typography.bodyBold, color = AppTheme.colors.accentGold)
            }
        }
    }
    Section(stringResource(Res.string.skill_tooltip_command), text.command.trim(), keys)
    val effects = text.effects.joinToString("\n\n") { it.joinToString("\n") }
    Section(stringResource(Res.string.skill_tooltip_effects), effects.trim(), keys)
}

/** Блок с заголовком и текстом; без текста блока нет. */
@Composable
private fun Section(title: String, markup: String, keys: KeyBindings) {
    if (markup.isBlank()) return
    TooltipSection(title = title) {
        SkillMarkupText(markup, keys, AppTheme.typography.body, AppTheme.colors.textPrimary)
    }
}

private const val MS_IN_SECOND = 1000
private const val MS_IN_TENTH = 100
