package com.layzen.bdotools.feature.comboassist.data.history

import com.layzen.bdotools.feature.comboassist.domain.history.SkillHistorySource

/** Источник истории умений своей платформы: на Desktop — экран, в браузере — заглушка. */
internal expect fun createSkillHistorySource(): SkillHistorySource
