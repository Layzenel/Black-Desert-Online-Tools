package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.skill.KeyBindings
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillControl
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillDefense
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillText
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_control_pve_only
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Значки эффектов защиты [defenses] в ряд. При наведении — их строки из тултипа навыка [text] с цветами сайта данных
 * («Защита от атак спереди во время использования навыка»); без текста — название эффекта.
 */
@Composable
internal fun DefenseIcons(defenses: List<SkillDefense>, text: SkillText?, keys: KeyBindings) {
    defenses.forEach { EffectIcon(it.effect.icon, stringResource(it.effect.title), tooltipLines(text, it.lines), keys) }
}

/**
 * Значки контролей [controls] в ряд. При наведении — их строки из тултипа навыка [text] («Эффект удара: Опрокидывание
 * (Недоступно в PvP)»); без текста — название контроля.
 */
@Composable
internal fun ControlIcons(controls: List<SkillControl>, text: SkillText?, keys: KeyBindings) {
    controls.forEach { control ->
        val name = stringResource(control.type.title)
        val title = if (control.pveOnly) stringResource(Res.string.slots_control_pve_only, name) else name
        EffectIcon(control.type.icon, title, tooltipLines(text, control.lines), keys)
    }
}

/** Вертикальная черта между группами значков. */
@Composable
internal fun EffectDivider() {
    Box(Modifier.width(AppTheme.dimens.borderWidth).height(SlotsDimens.effectIcon).background(AppTheme.colors.border))
}

/** Ряд значков с промежутком [SlotsDimens.effectGap]. */
@Composable
internal fun EffectRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(SlotsDimens.effectGap), content = content)
}

/** Строки [lines] тултипа [text] разметкой сайта данных, каждая — с новой строки; нет текста — `null`. */
private fun tooltipLines(text: SkillText?, lines: List<Int>): String? =
    text?.let { lines.mapNotNull { i -> it.effects.getOrNull(i)?.joinToString("")?.trim() } }
        ?.filter { it.isNotEmpty() }?.takeIf { it.isNotEmpty() }?.joinToString("\n")

@Composable
private fun EffectIcon(icon: DrawableResource, title: String, markup: String?, keys: KeyBindings) {
    val image = @Composable { Image(painterResource(icon), contentDescription = title, modifier = Modifier.size(SlotsDimens.effectIcon)) }
    if (markup == null) {
        HoverTooltip(title) { image() }
    } else {
        HoverTooltip(tooltip = {
            Text(
                skillMarkupText(markup, keys),
                style = AppTheme.typography.bodyBold,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.widthIn(max = AppTheme.dimens.tooltipMaxWidth),
            )
        }) { image() }
    }
}
