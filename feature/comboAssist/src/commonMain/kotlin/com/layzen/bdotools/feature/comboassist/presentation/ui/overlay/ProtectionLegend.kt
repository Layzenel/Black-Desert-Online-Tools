package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_protection_forward_guard
import com.layzen.bdotools.feature.comboassist.resources.combo_protection_invincible
import com.layzen.bdotools.feature.comboassist.resources.combo_protection_super_armor
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Легенда щитов защиты под переключателем: щит каждого цвета и его защита. Сдвинута на ширину переключателя,
 * чтобы стоять вровень с его подписью.
 */
@Composable
internal fun ProtectionLegend() {
    val dimens = AppTheme.dimens
    FlowRow(
        Modifier.padding(start = dimens.toggleWidth + dimens.spaceS),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceM),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
    ) {
        SkillProtection.entries.forEach { protection ->
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs), verticalAlignment = Alignment.CenterVertically) {
                ShieldIcon(protection)
                Text(stringResource(protection.title), style = AppTheme.typography.caption, color = AppTheme.colors.textBeige)
            }
        }
    }
}

/** Название защиты для легенды. */
private val SkillProtection.title
    get() = when (this) {
        SkillProtection.SuperArmor -> Res.string.combo_protection_super_armor
        SkillProtection.ForwardGuard -> Res.string.combo_protection_forward_guard
        SkillProtection.Invincible -> Res.string.combo_protection_invincible
    }
