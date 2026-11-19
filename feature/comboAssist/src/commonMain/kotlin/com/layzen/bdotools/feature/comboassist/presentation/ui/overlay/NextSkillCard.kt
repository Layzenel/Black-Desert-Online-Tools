package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.cooldownText
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_next_entry
import com.layzen.bdotools.feature.comboassist.resources.combo_next_now
import com.layzen.bdotools.feature.comboassist.resources.combo_next_start
import com.layzen.bdotools.feature.comboassist.resources.combo_next_wait
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Карточка «что жать сейчас»: крупная иконка с таймером, подпись и клавиши из той строки команды, где упомянуто
 * предыдущее умение. Названия умения нет — его видно по иконке. Пока умение откатывается — «Через 1.4 с», рамка
 * и подпись жёлтые; вход в оборванную связку — синие. Подложку рисует тот, кто ставит карточку.
 */
@Composable
internal fun NextSkillCard(overlay: OverlayState, skills: Map<Int, SkillItem>, loadDetails: suspend (Int) -> SkillDetails?, modifier: Modifier = Modifier) {
    val next = overlay.next ?: return
    val item = skills[next.skillNo]
    val frame = nextFrameColor(overlay)
    val title = when {
        next.isEntry -> stringResource(Res.string.combo_next_entry)
        overlay.waitMs > 0 -> stringResource(Res.string.combo_next_wait, cooldownText(overlay.waitMs))
        overlay.pass == 0 -> stringResource(Res.string.combo_next_start)
        else -> stringResource(Res.string.combo_next_now)
    }
    val line = rememberNextCommandLine(overlay, skills, loadDetails)
    Row(
        // высота постоянная, а ширина — по содержимому: лишнего места справа от клавиш нет
        modifier.height(ComboDimens.nextCardHeight),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkillIconView(item?.icon, ComboDimens.nextIcon, frame = frame, frameWidth = ComboDimens.nextFrame,
            cooldownLeftMs = overlay.cooldownLeft[next.skillNo] ?: 0, cooldownMs = item?.skill?.cooldownMs ?: 0)
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs)) {
            Text(title, style = AppTheme.typography.caption, color = frame)
            KeyCaps(line)
        }
    }
}
