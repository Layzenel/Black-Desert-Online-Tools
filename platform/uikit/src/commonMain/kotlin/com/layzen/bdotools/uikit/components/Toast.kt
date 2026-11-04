package com.layzen.bdotools.uikit.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.delay

/** Сколько всплывающее сообщение висит на экране. */
const val TOAST_DURATION_MS = 5_000L

/**
 * Всплывающее сообщение внизу по центру окна. Появляется, когда [text] не `null`, и через [durationMillis]
 * (или по клику) вызывает [onDismiss]. Ошибка ([isError]) — красный текст и рамка, остальное — золотая рамка.
 * Не перехватывает фокус и клики мимо себя.
 */
@Composable
fun Toast(
    text: String?,
    onDismiss: () -> Unit,
    isError: Boolean = false,
    durationMillis: Long = TOAST_DURATION_MS,
) {
    val visible = remember { MutableTransitionState(false) }
    visible.targetState = text != null
    // пока сообщение уходит с экрана, показываем последний текст
    var shownText by remember { mutableStateOf("") }
    var shownError by remember { mutableStateOf(false) }
    if (text != null && (text != shownText || isError != shownError)) {
        shownText = text
        shownError = isError
    }
    val dismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(text) {
        if (text != null) {
            delay(durationMillis)
            dismiss()
        }
    }
    if (!visible.currentState && !visible.targetState) return
    val offset = with(LocalDensity.current) { AppTheme.dimens.toastBottomOffset.roundToPx() }
    Popup(popupPositionProvider = remember(offset) { BottomCenter(offset) }, properties = PopupProperties(focusable = false)) {
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            ToastPanel(shownText, shownError, onClick = { dismiss() })
        }
    }
}

@Composable
private fun ToastPanel(text: String, isError: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Box(
        Modifier
            .widthIn(max = dimens.toastMaxWidth)
            .shadow(dimens.menuShadow, AppShapes.Small)
            .background(colors.backgroundRaised, AppShapes.Small)
            .border(dimens.borderWidth, if (isError) colors.danger else colors.accentGoldPale, AppShapes.Small)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = dimens.spaceL, vertical = dimens.spaceM),
    ) {
        Text(text, style = AppTheme.typography.bodyBold, color = if (isError) colors.danger else colors.textPrimary)
    }
}

/** Ставит сообщение внизу по центру окна, на [bottomOffset] пикселей выше края. */
private class BottomCenter(private val bottomOffset: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ) = IntOffset(
        x = (windowSize.width - popupContentSize.width) / 2,
        y = windowSize.height - popupContentSize.height - bottomOffset,
    )
}
