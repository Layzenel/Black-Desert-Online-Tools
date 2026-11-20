package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection

/**
 * Щиты в правом верхнем углу слотов перезарядки [slots] по защите умения в PvP ([protections]): жёлтый — устойчивость,
 * синий — защита спереди, красный — неуязвимость; несколько защит — щиты в ряд, самый сильный с края. Только экран,
 * внизу иконки — значки контролей умения в PvP ([controls]). Только экран, клики проходят в игру. Слоты, закрытые окном игры, сюда не передаются — на них щитов нет.
 * Есть только в настольной версии.
 */
@Composable
internal expect fun SlotShieldsWindow(slots: List<ScreenSlot>, protections: Map<Int, Set<SkillProtection>>, controls: Map<Int, Set<CrowdControl>>)
