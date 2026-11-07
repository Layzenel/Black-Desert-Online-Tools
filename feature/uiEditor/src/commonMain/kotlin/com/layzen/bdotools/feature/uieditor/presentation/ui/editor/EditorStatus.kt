package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_off_screen
import com.layzen.bdotools.uikit.components.WarningText
import org.jetbrains.compose.resources.stringResource

/**
 * Внизу меню слева, над кнопками: сколько элементов и отдельных окон чата уехало за край экрана.
 * Не под холстом: появляясь, текст сдвигал бы холст.
 */
@Composable
internal fun EditorStatus(state: SlotsState) {
    val screen = state.loaded?.game?.screen ?: return
    val slots = state.slots.count { id -> state.layout[id]?.let { !screen.fits(id, it) } == true }
    val chats = state.layout.chats.count { (window, chat) -> state.layout.isChatShown(window) && !chat.fits(screen) }
    val offScreen = slots + chats
    if (offScreen > 0) WarningText(stringResource(Res.string.slots_off_screen, offScreen))
}
