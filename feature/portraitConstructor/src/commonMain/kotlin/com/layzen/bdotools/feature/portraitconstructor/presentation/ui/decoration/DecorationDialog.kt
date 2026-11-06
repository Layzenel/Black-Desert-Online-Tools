package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitDecorator
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.DecorationDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_cancel
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_caption_description
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_caption_placeholder
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_decoration_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_decoration_title
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_editor_apply
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Надпись и значки одного портрета — прямо на живом предпросмотре: надпись редактируется на своём месте
 * и выглядит как в игре, «+» на местах значков. Клик мимо надписи убирает курсор.
 */
@Composable
internal fun DecorationDialog(
    state: DecorationDialogState,
    decorator: PortraitDecorator,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val ready = state.base != null && decorator.isReady
    val focusManager = LocalFocusManager.current
    val hint = stringResource(Res.string.portraits_caption_placeholder)
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
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            DecorationPreview(state, decorator, captionGlow = state.draft.caption.ifBlank { hint }) {
                BadgeOverlay(state.draft.badge, onEvent)
                CaptionOverlay(state.draft.caption, hint, decorator, onEvent)
            }
            DescriptionText(stringResource(Res.string.portraits_decoration_hint))
            DescriptionText(stringResource(Res.string.portraits_caption_description))
        }
    }
}
