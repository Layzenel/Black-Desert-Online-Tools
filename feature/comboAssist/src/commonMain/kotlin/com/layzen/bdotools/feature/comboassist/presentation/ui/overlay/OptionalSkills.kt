package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Опциональные шаги, которые можно вставить сейчас ([OverlayState.optional]), отдельно от пути комбо: бледные иконки
 * размером [iconSize] в пунктирной рамке — «можно, но не обязательно». Столбиком при [vertical], иначе в ряд.
 * Пусто — ничего не рисует.
 */
@Composable
internal fun OptionalSkills(overlay: OverlayState, skills: Map<Int, SkillItem>, iconSize: Dp, vertical: Boolean = false) {
    if (overlay.optional.isEmpty()) return
    val icons: @Composable () -> Unit = { overlay.optional.forEach { OptionalSkillIcon(it, overlay, skills, iconSize) } }
    val gap = Arrangement.spacedBy(AppTheme.dimens.spaceXs)
    if (vertical) Column(verticalArrangement = gap) { icons() } else Row(horizontalArrangement = gap) { icons() }
}

/** Иконка опционального шага: без сплошной рамки, поверх — пунктир. */
@Composable
private fun OptionalSkillIcon(skillNo: Int, overlay: OverlayState, skills: Map<Int, SkillItem>, iconSize: Dp) {
    val item = skills[skillNo]
    val dash = AppTheme.colors.textMuted
    SkillIconView(
        item?.icon, iconSize,
        Modifier.alpha(OPTIONAL_ALPHA).drawWithContent {
            drawContent()
            val on = ComboDimens.optionalDash.toPx()
            val width = ComboDimens.frame.toPx()
            drawRect(dash, style = Stroke(width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(on, on))))
        },
        frame = Color.Transparent,
        cooldownLeftMs = overlay.cooldownLeft[skillNo] ?: 0, cooldownMs = item?.skill?.cooldownMs ?: 0,
    )
}

private const val OPTIONAL_ALPHA = 0.75f
