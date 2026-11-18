package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.core.skill.domain.SkillTextPart
import com.layzen.bdotools.core.skill.domain.parseSkillMarkup
import com.layzen.bdotools.feature.comboassist.domain.skill.commandLineFor
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.cooldownText
import com.layzen.bdotools.composable.skill.rememberKeyLabel
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.combo_next_entry
import com.layzen.bdotools.feature.comboassist.resources.combo_next_no_keys
import com.layzen.bdotools.feature.comboassist.resources.combo_next_now
import com.layzen.bdotools.feature.comboassist.resources.combo_next_start
import com.layzen.bdotools.feature.comboassist.resources.combo_next_wait
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Карточка «что жать сейчас»: крупная иконка с таймером, название и клавиши из той строки команды, где упомянуто
 * предыдущее умение («Во время навыка Алый коготь: ПКМ»). Пока умение откатывается — «Через 1.4 с», рамка жёлтая;
 * вход в оборванную связку — синяя.
 */
@Composable
internal fun NextSkillCard(overlay: OverlayState, skills: Map<Int, SkillItem>, loadDetails: suspend (Int) -> SkillDetails?, modifier: Modifier = Modifier) {
    val next = overlay.next ?: return
    val language = stringResource(Res.string.combo_names_language)
    val item = skills[next.skillNo]
    val waiting = overlay.waitMs > 0
    val frame = when {
        next.isEntry -> AppTheme.colors.accentBlue
        waiting -> AppTheme.colors.badgeNew
        else -> AppTheme.colors.accentGold
    }
    val title = when {
        next.isEntry -> stringResource(Res.string.combo_next_entry)
        waiting -> stringResource(Res.string.combo_next_wait, cooldownText(overlay.waitMs))
        overlay.pass == 0 -> stringResource(Res.string.combo_next_start)
        else -> stringResource(Res.string.combo_next_now)
    }
    val details by produceState<SkillDetails?>(null, next.skillNo) { value = loadDetails(next.skillNo) }
    val previousName = overlay.previousSkill?.let { skills[it]?.skill?.name(language) }
    val line = details?.text(language)?.command?.let { commandLineFor(it, previousName) }.orEmpty()
    Row(
        // карточка всегда одного размера: смена умения не двигает оверлей, длинное название обрезается
        modifier.size(ComboDimens.nextCard).background(AppTheme.colors.backgroundDeep).border(ComboDimens.frame, frame).padding(AppTheme.dimens.spaceS),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkillIconView(item?.icon, ComboDimens.nextIcon, frame = frame, frameWidth = ComboDimens.nextFrame,
            cooldownLeftMs = overlay.cooldownLeft[next.skillNo] ?: 0, cooldownMs = item?.skill?.cooldownMs ?: 0)
        Column(Modifier.weight(1f).clipToBounds(), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            Text(title, style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
            Text(item?.skill?.name(language).orEmpty(), style = AppTheme.typography.windowTitle, color = AppTheme.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            KeyCaps(line)
        }
    }
}

/** Клавиши строки команды кнопками, текст между ними — мелко. */
@Composable
private fun KeyCaps(line: String) {
    val parts = parseSkillMarkup(line)
    if (parts.none { it is SkillTextPart.Key }) {
        Text(stringResource(Res.string.combo_next_no_keys), style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
        return
    }
    val label = rememberKeyLabel(KeyBindings.Default)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        parts.forEach { part ->
            when (part) {
                is SkillTextPart.Key -> Box(
                    Modifier.background(AppTheme.colors.button, AppShapes.Small).border(ComboDimens.frame, AppTheme.colors.buttonBorder, AppShapes.Small)
                        .widthIn(min = ComboDimens.keyCapMinWidth).padding(horizontal = ComboDimens.keyCapPadding, vertical = AppTheme.dimens.spaceXxs),
                    contentAlignment = Alignment.Center,
                ) { Text(label(part.action), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textButton) }
                is SkillTextPart.Plain -> part.text.trim().takeIf { it.isNotEmpty() }?.let {
                    Text(it, style = AppTheme.typography.caption, color = AppTheme.colors.textMuted, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }
    }
}
