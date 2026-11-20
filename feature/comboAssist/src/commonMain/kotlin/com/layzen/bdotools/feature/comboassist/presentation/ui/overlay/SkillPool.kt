package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Опциональные умения комбо ([OverlayState.pool]) — всегда на трекере, отдельно от шагов, в порядке из редактора:
 * готовое умение в золотой рамке, на перезарядке — затемнено, с секундами. Рядами по [columns] иконок.
 * Без опциональных умений ничего не рисует.
 */
@Composable
internal fun SkillPool(overlay: OverlayState, skills: Map<Int, SkillItem>, columns: Int) {
    if (overlay.pool.isEmpty()) return
    val gap = Arrangement.spacedBy(AppTheme.dimens.spaceXs)
    Column(verticalArrangement = gap) {
        overlay.pool.chunked(columns.coerceAtLeast(1)).forEach { row ->
            Row(horizontalArrangement = gap) { row.forEach { PoolSkillIcon(it, overlay, skills) } }
        }
    }
}

/** Иконка опционального умения: золотая рамка, пока оно готово. */
@Composable
private fun PoolSkillIcon(skillNo: Int, overlay: OverlayState, skills: Map<Int, SkillItem>) {
    val item = skills[skillNo]
    val left = overlay.cooldownLeft[skillNo] ?: 0
    SkillIconView(
        item?.icon, ComboDimens.poolIcon,
        frame = if (left == 0L) AppTheme.colors.accentGold else AppTheme.colors.border,
        cooldownLeftMs = left, cooldownMs = item?.skill?.cooldownMs ?: 0,
    )
}
