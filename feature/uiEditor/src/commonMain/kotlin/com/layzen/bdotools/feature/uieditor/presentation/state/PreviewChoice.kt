package com.layzen.bdotools.feature.uieditor.presentation.state

import com.layzen.bdotools.feature.uieditor.domain.SlotId

/** Отметки превью целиком — элементы и окна чата по `WindowIndex`: шаг «Отменить» и «Повторить». */
data class PreviewChoice(val slots: Set<SlotId>, val chats: Set<Int>)
