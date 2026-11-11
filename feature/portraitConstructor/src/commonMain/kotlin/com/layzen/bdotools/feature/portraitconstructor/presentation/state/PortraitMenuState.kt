package com.layzen.bdotools.feature.portraitconstructor.presentation.state

import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Список элементов контекстного меню портрета*/
internal data class PortraitMenuState(
    val items: List<PortraitMenuItem> = listOf()
)

/** Контекстное меню портрета: название кнопки, иконка, событие. */
internal data class PortraitMenuItem(
    val name: StringResource,
    val iconId: DrawableResource,
    val event: PortraitsEvent
) {

    /** Событие пункта для портрета [characterId]; сейчас оно одно на все портреты. */
    fun getEvent(characterId: String): PortraitsEvent = event
}
