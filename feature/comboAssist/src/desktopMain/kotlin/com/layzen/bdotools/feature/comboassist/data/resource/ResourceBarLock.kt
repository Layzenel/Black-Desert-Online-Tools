package com.layzen.bdotools.feature.comboassist.data.resource

/**
 * Найденная полоска ресурса на снимке области поиска.
 *
 * @property rows ряды полоски, px от верха снимка
 * @property left левый край заполнения, px от левого края снимка
 */
internal data class ResourceBarLock(val rows: IntRange, val left: Int)
