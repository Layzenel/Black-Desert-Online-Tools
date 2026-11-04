package com.layzen.bdotools.feature.slots.domain

/** Раскладка с названием — пресет пользователя или содержимое импортированного файла. От монитора не зависит. */
data class NamedLayout(val name: String, val layout: AnchoredLayout)
