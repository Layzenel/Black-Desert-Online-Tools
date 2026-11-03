package com.layzen.bdotools.feature.portraits.presentation

import com.layzen.bdotools.feature.portraits.data.PortraitsError
import com.layzen.bdotools.feature.portraits.data.PortraitsException
import com.layzen.bdotools.feature.portraits.presentation.state.Notice
import com.layzen.bdotools.feature.portraits.presentation.state.NoticeKind

/** Сообщение об ошибке для пользователя. `null` — показывать нечего (например, окно выбора закрыли). */
internal fun Exception.toNotice(): Notice? = when ((this as? PortraitsException)?.error) {
    PortraitsError.Cancelled -> null
    PortraitsError.NotGameFolder -> Notice(NoticeKind.NotGameFolder)
    PortraitsError.Permission -> Notice(NoticeKind.Permission)
    PortraitsError.Blocked -> Notice(NoticeKind.Blocked)
    PortraitsError.Network -> Notice(NoticeKind.Network)
    PortraitsError.NotImage -> Notice(NoticeKind.NotImage)
    PortraitsError.Unknown, null -> Notice(NoticeKind.Unknown, message.orEmpty())
}
