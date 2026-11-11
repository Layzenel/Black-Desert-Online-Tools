package com.layzen.bdotools.feature.uieditor.domain.preset

import com.layzen.bdotools.feature.uieditor.domain.file.AnchoredLayout

/** Раскладка с названием — пресет пользователя или содержимое импортированного файла. От монитора не зависит. */
data class NamedLayout(val name: String, val layout: AnchoredLayout)
