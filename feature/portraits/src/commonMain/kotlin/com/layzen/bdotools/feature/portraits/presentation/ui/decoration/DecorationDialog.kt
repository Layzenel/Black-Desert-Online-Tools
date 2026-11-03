package com.layzen.bdotools.feature.portraits.presentation.ui.decoration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.render.PortraitDecorator
import com.layzen.bdotools.feature.portraits.presentation.state.DecorationDialogState
import com.layzen.bdotools.feature.portraits.presentation.state.DecorationMode
import com.layzen.bdotools.feature.portraits.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_badges_title
import com.layzen.bdotools.feature.portraits.resources.portraits_cancel
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_title
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_apply
import com.layzen.bdotools.feature.portraits.resources.portraits_preview_hint
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Надпись или значки для одного портрета: слева живой предпросмотр, справа настройки. */
@Composable
internal fun DecorationDialog(
    state: DecorationDialogState,
    decorator: PortraitDecorator,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val title = when (state.mode) {
        DecorationMode.Caption -> Res.string.portraits_caption_title
        DecorationMode.Badges -> Res.string.portraits_badges_title
    }
    val ready = state.base != null && decorator.isReady
    ModalWindow(
        title = stringResource(title),
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
        Row(
            Modifier.padding(AppTheme.dimens.spaceL),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL),
        ) {
            Column(Modifier.width(PortraitsDimens.decorationPreviewWidth), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                DecorationPreview(state, decorator)
                DescriptionText(stringResource(Res.string.portraits_preview_hint))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
                when (state.mode) {
                    DecorationMode.Caption -> CaptionControls(state.draft, onEvent)
                    DecorationMode.Badges -> BadgeControls(state.draft, onEvent)
                }
            }
        }
    }
}
