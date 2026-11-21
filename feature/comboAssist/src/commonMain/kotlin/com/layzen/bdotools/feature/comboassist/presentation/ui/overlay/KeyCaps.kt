package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.composable.skill.rememberKeyLabel
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.core.skill.domain.SkillTextPart
import com.layzen.bdotools.core.skill.domain.parseSkillMarkup
import com.layzen.bdotools.feature.comboassist.domain.skill.commandLineFor
import com.layzen.bdotools.feature.comboassist.domain.keys.keyVariantLine
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.combo_next_no_keys
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Строка команды умения «что жать сейчас»: выбранная в редакторе ([OverlayState.cellKeys]), иначе та, где
 * упомянуто предыдущее умение («Во время навыка Алый коготь: ПКМ»), иначе первая с клавишами. Пусто, пока описание
 * умения не загрузилось.
 */
@Composable
internal fun rememberNextCommandLine(overlay: OverlayState, skills: Map<Int, SkillItem>, loadDetails: suspend (Int) -> SkillDetails?): String {
    val skillNo = overlay.next?.skillNo ?: return ""
    // команда, выбранная в редакторе, — вместо строк описания: игрок жмёт именно её
    overlay.next?.node?.at?.let { overlay.cellKeys[it] }?.let { return keyVariantLine(it) }
    val language = stringResource(Res.string.combo_names_language)
    val details by produceState<SkillDetails?>(null, skillNo) { value = loadDetails(skillNo) }
    val previousName = overlay.previousSkill?.let { skills[it]?.skill?.name(language) }
    return details?.text(language)?.command?.let { commandLineFor(it, previousName) }.orEmpty()
}

/** Цвет рамки шага «что жать сейчас»: вход в оборванную связку — синий, ждём перезарядку — жёлтый, иначе золотой. */
@Composable
internal fun nextFrameColor(overlay: OverlayState): Color = when {
    overlay.next?.isEntry == true -> AppTheme.colors.accentBlue
    overlay.waitMs > 0 -> AppTheme.colors.badgeNew
    else -> AppTheme.colors.accentGold
}

/**
 * Клавиши строки команды мелкими кнопками в одну строку, текст между ними («+») — мелко. Пояснение перед первой
 * клавишей («Во время навыка …:») не показывается: оверлей маленький, и ширина не должна прыгать от шага к шагу.
 */
@Composable
internal fun KeyCaps(line: String, modifier: Modifier = Modifier) {
    val all = parseSkillMarkup(line)
    if (all.none { it is SkillTextPart.Key }) {
        Text(stringResource(Res.string.combo_next_no_keys), modifier, style = AppTheme.typography.caption, color = AppTheme.colors.textMuted,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        return
    }
    val label = rememberKeyLabel(KeyBindings.Default)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs), verticalAlignment = Alignment.CenterVertically) {
        all.dropWhile { it !is SkillTextPart.Key }.forEach { part ->
            when (part) {
                is SkillTextPart.Key -> Box(
                    Modifier.background(AppTheme.colors.button, AppShapes.Small).border(AppTheme.dimens.borderWidth, AppTheme.colors.buttonBorder, AppShapes.Small)
                        .widthIn(min = ComboDimens.keyCapMinWidth).padding(horizontal = ComboDimens.keyCapPadding),
                    contentAlignment = Alignment.Center,
                ) { Text(label(part.action), style = AppTheme.typography.caption, color = AppTheme.colors.textButton, maxLines = 1, softWrap = false) }
                is SkillTextPart.Plain -> part.text.trim().takeIf { it.isNotEmpty() }?.let {
                    Text(it, style = AppTheme.typography.caption, color = AppTheme.colors.textMuted, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
