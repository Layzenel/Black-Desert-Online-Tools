package com.layzen.bdotools.feature.slots.domain

import kotlinx.serialization.Serializable

/** Слот в файле раскладки: номер, как подписано в игре, и центр от центра экрана. */
@Serializable
data class LayoutSlot(val slot: Int, val x: Int, val y: Int)
