package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.CHAT_MIN_HEIGHT
import com.layzen.bdotools.feature.uieditor.domain.CHAT_MIN_WIDTH
import com.layzen.bdotools.feature.uieditor.domain.ChatWindow
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.chatTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_size
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_height
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_size_description
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_size_title
import com.layzen.bdotools.feature.uieditor.resources.slots_chat_width
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.NumberField
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Самое большое число, которое можно набрать в поле: пределы окна проверяются при применении, а не при наборе. */
private const val MAX_TYPED = 9999

/**
 * Размер окна чата руками, в UI-px. Пока набирается, значение в поле свободное; при «Применить» оно приводится
 * к пределам окна (их показывает пояснение), левый нижний угол остаётся на месте.
 */
@Composable
internal fun ChatSizeDialog(dialog: SlotsDialog.ChatSize, chat: ChatWindow, screen: ScreenSpace, onEvent: (SlotsEvent) -> Unit) {
    var width by remember(dialog) { mutableIntStateOf(chat.width) }
    var height by remember(dialog) { mutableIntStateOf(chat.height) }
    val close = { onEvent(SlotsEvent.CloseDialog) }
    ModalWindow(
        title = stringResource(Res.string.slots_chat_size_title, chatTitle(dialog.window)),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.slots_cancel), onClick = close)
            ActionButton(stringResource(Res.string.slots_apply_size), onClick = { onEvent(SlotsEvent.SetChatSize(dialog.window, width, height)) })
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            SizeRow(stringResource(Res.string.slots_chat_width), width) { width = it }
            SizeRow(stringResource(Res.string.slots_chat_height), height) { height = it }
            DescriptionText(
                stringResource(
                    Res.string.slots_chat_size_description,
                    CHAT_MIN_WIDTH,
                    ChatWindow.maxWidth(screen),
                    CHAT_MIN_HEIGHT,
                    screen.height.toInt(),
                ),
            )
        }
    }
}

@Composable
private fun SizeRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        Text(label, Modifier.width(SlotsDimens.chatSizeLabelWidth), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        NumberField(value, onValueChange, Modifier.width(SlotsDimens.coordinateFieldWidth), range = 0..MAX_TYPED)
    }
}
