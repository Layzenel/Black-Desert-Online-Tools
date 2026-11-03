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
import com.layzen.bdotools.feature.portraits.resources.portraits_badges_hint
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

/**
 * Надпись или значки для одного портрета. Надпись: слева живой предпросмотр, справа поле.
 * Значки ставятся прямо на предпросмотре, поэтому окно значков — только портрет с подсказкой.
 */
@Composable
internal fun DecorationDialog(
    state: DecorationDialogState,
    decorator: PortraitDecorator,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val (title, width) = when (state.mode) {
        DecorationMode.Caption -> Res.string.portraits_caption_title to PortraitsDimens.decorationDialogWidth
        DecorationMode.Badges -> Res.string.portraits_badges_title to PortraitsDimens.badgesDialogWidth
    }
    val ready = state.base != null && decorator.isReady
    ModalWindow(
        title = stringResource(title),
        onDismissRequest = { onEvent(PortraitsEvent.CloseDecoration) },
        modifier = Modifier.width(width),
        footer = {
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = { onEvent(PortraitsEvent.CloseDecoration) })
            ActionButton(
                stringResource(Res.string.portraits_editor_apply),
                onClick = { onEvent(PortraitsEvent.ApplyDecoration) },
                enabled = ready,
            )
        },
    ) {
        when (state.mode) {
            DecorationMode.Caption -> CaptionLayout(state, decorator, onEvent)
            DecorationMode.Badges -> PreviewColumn(state, decorator, Modifier.padding(AppTheme.dimens.spaceL), onEvent) {
                DescriptionText(stringResource(Res.string.portraits_badges_hint))
            }
        }
    }
}

@Composable
private fun CaptionLayout(state: DecorationDialogState, decorator: PortraitDecorator, onEvent: (PortraitsEvent) -> Unit) {
    Row(
        Modifier.padding(AppTheme.dimens.spaceL),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL),
    ) {
        PreviewColumn(state, decorator, Modifier.width(PortraitsDimens.decorationPreviewWidth))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
            CaptionControls(state.draft, onEvent)
        }
    }
}

/** Предпросмотр с подсказками под ним. В режиме значков поверх него стоят места значков. */
@Composable
private fun PreviewColumn(
    state: DecorationDialogState,
    decorator: PortraitDecorator,
    modifier: Modifier,
    onEvent: ((PortraitsEvent) -> Unit)? = null,
    hint: @Composable () -> Unit = {},
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        DecorationPreview(state, decorator) {
            if (onEvent != null) BadgeSlotsOverlay(state.draft.badges, onEvent)
        }
        hint()
        DescriptionText(stringResource(Res.string.portraits_preview_hint))
    }
}
