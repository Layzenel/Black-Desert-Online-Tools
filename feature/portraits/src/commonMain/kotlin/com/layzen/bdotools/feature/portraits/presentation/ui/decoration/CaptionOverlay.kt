package com.layzen.bdotools.feature.portraits.presentation.ui.decoration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import com.layzen.bdotools.feature.portraits.domain.PortraitOverlayLayout
import com.layzen.bdotools.feature.portraits.domain.PortraitTexture
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_description
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_placeholder
import com.layzen.bdotools.uikit.components.CloseButton
import com.layzen.bdotools.uikit.components.InfoButton
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Поле надписи на месте надписи на портрете: по ширине — как надпись, нижний край — у полосы игры.
 * Пока [editing], поле видно, а готовая надпись на предпросмотре скрыта. Когда редактирование заканчивается,
 * надпись снова рисуется как в игре, а поверх неё остаётся прозрачная область: клик по ней — снова поле.
 * Пустое поле видно всегда, чтобы была видна подсказка.
 */
@Composable
internal fun BoxScope.CaptionOverlay(
    caption: String,
    editing: Boolean,
    onEditingChange: (Boolean) -> Unit,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val focus = remember { FocusRequester() }
    BoxWithConstraints(Modifier.matchParentSize()) {
        val texturePixel = maxWidth / PortraitTexture.WIDTH
        val left = texturePixel * PortraitOverlayLayout.CAPTION_PADDING_HORIZONTAL
        val width = maxWidth - left * 2
        val top = texturePixel * PortraitOverlayLayout.CAPTION_MIN_TOP
        val bottom = texturePixel * PortraitOverlayLayout.CAPTION_BOTTOM
        if (editing || caption.isEmpty()) {
            CaptionField(
                caption = caption,
                onEvent = onEvent,
                onEditingChange = onEditingChange,
                modifier = Modifier
                    .offset(x = left, y = bottom - AppTheme.dimens.inputHeight)
                    .width(width)
                    .focusRequester(focus),
            )
        } else {
            Box(
                Modifier
                    .offset(x = left, y = top)
                    .size(width, bottom - top)
                    .pointerHoverIcon(PointerIcon.Text)
                    .clickable(remember { MutableInteractionSource() }, indication = null) { onEditingChange(true) },
            )
        }
    }
    LaunchedEffect(editing) { if (editing) focus.requestFocus() }
}

@Composable
private fun CaptionField(
    caption: String,
    onEvent: (PortraitsEvent) -> Unit,
    onEditingChange: (Boolean) -> Unit,
    modifier: Modifier,
) {
    InputField(
        value = caption,
        onValueChange = { onEvent(PortraitsEvent.CaptionChanged(it)) },
        placeholder = stringResource(Res.string.portraits_caption_placeholder),
        textAlign = TextAlign.Center,
        trailing = {
            if (caption.isNotEmpty()) CloseButton(onClick = { onEvent(PortraitsEvent.CaptionChanged("")) })
            InfoButton(stringResource(Res.string.portraits_caption_description))
        },
        // Конец редактирования — только клик мимо (см. DecorationDialog): «!» и крестик забирают фокус,
        // и если прятать поле по потере фокуса, вместе с ним пропадёт и подсказка
        modifier = modifier.onFocusChanged { if (it.isFocused) onEditingChange(true) },
    )
}
