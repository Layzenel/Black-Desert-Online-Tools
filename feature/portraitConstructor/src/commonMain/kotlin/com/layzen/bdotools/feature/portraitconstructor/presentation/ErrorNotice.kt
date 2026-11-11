package com.layzen.bdotools.feature.portraitconstructor.presentation

import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsError
import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsException
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.Notice
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.NoticeKind

/** Сообщение об ошибке для пользователя. `null` — показывать нечего (например, окно выбора закрыли). */
internal fun Exception.toNotice(): Notice? = when ((this as? PortraitsException)?.error) {
    PortraitsError.Cancelled -> null
    PortraitsError.NotGameFolder -> Notice(NoticeKind.NotGameFolder)
    PortraitsError.Permission -> Notice(NoticeKind.Permission)
    PortraitsError.Blocked -> Notice(NoticeKind.Blocked)
    PortraitsError.Network -> Notice(NoticeKind.Network)
    PortraitsError.NotImage -> Notice(NoticeKind.NotImage)
    PortraitsError.LobbyNotFound -> Notice(NoticeKind.LobbyNotFound)
    PortraitsError.GameRunning -> Notice(NoticeKind.GameRunning)
    PortraitsError.Unknown, null -> Notice(NoticeKind.Unknown, message.orEmpty())
}
