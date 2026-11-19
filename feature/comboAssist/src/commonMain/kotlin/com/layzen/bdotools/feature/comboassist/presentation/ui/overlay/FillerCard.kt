package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_filler
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Филлер на время ожидания: иконка в бежевой рамке, подпись и название. Подложку рисует тот, кто его ставит. */
@Composable
internal fun FillerCard(skillNo: Int, skills: Map<Int, SkillItem>) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS), verticalAlignment = Alignment.CenterVertically) {
        SkillIconView(skills[skillNo]?.icon, ComboDimens.upcomingIcon, frame = AppTheme.colors.textBeige)
        Column {
            Text(stringResource(Res.string.combo_filler), style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
            Text(skillName(skills, skillNo), style = AppTheme.typography.caption, color = AppTheme.colors.textPrimary)
        }
    }
}
