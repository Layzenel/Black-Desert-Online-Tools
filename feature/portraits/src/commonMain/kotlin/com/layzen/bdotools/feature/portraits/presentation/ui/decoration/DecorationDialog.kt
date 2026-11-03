package com.layzen.bdotools.feature.portraits.presentation.ui.decoration

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.render.PortraitDecorator
import com.layzen.bdotools.feature.portraits.presentation.state.DecorationDialogState
import com.layzen.bdotools.feature.portraits.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_cancel
import com.layzen.bdotools.feature.portraits.resources.portraits_decoration_hint
import com.layzen.bdotools.feature.portraits.resources.portraits_decoration_title
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_apply
import com.layzen.bdotools.feature.portraits.resources.portraits_preview_hint
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Надпись и значки одного портрета — прямо на живом предпросмотре: поле надписи на месте надписи,
 * «+» на местах значков. Клик мимо поля заканчивает ввод, и надпись рисуется как в игре.
 */
@Composable
internal fun DecorationDialog(
    state: DecorationDialogState,
    decorator: PortraitDecorator,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val ready = state.base != null && decorator.isReady
    var editingCaption by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    ModalWindow(
        title = stringResource(Res.string.portraits_decoration_title),
        onDismissRequest = { onEvent(PortraitsEvent.CloseDecoration) },
        modifier = Modifier.width(PortraitsDimens.decorationDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = { onEvent(PortraitsEvent.CloseDecoration) })
            ActionButton(
                stringResource(Res.string.portraits_editor_apply),
                onClick = { onEvent(PortraitsEvent.ApplyDecoration) },
                enabled = ready,
            )
        },
    ) {
        Column(
            Modifier
                // Клик мимо поля, кнопок и мест значков заканчивает ввод надписи
                .pointerInput(Unit) {
                    detectTapGestures {
                        editingCaption = false
                        focusManager.clearFocus()
                    }
                }
                .padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            DecorationPreview(state, decorator, hideCaption = editingCaption) {
                BadgeSlotsOverlay(state.draft.badges, onEvent)
                CaptionOverlay(state.draft.caption, editingCaption, onEditingChange = { editingCaption = it }, onEvent = onEvent)
            }
            DescriptionText(stringResource(Res.string.portraits_decoration_hint))
            DescriptionText(stringResource(Res.string.portraits_preview_hint))
        }
    }
}
