package com.layzen.bdotools.feature.comboassist.data.history

import com.layzen.bdotools.feature.comboassist.domain.history.ScreenSlot
import com.layzen.bdotools.feature.comboassist.domain.history.SkillHistorySource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Браузер не видит окно игры: следить за историей умений умеет только настольная версия. */
internal class BrowserSkillHistorySource : SkillHistorySource {
    override val supported: Boolean = false
    override fun watch(icons: Map<Int, ByteArray>, preferred: Set<Int>): Flow<Int> = emptyFlow()
    override suspend fun playedSlots(icon: suspend (Int) -> ByteArray?): List<ScreenSlot>? = null
}

internal actual fun createSkillHistorySource(): SkillHistorySource = BrowserSkillHistorySource()
