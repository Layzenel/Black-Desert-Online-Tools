package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.composable.canvas.CanvasViewport
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.labelSize
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Поле надписи прямо на скриншоте — там, куда кликнули инструментом «Текст», того же цвета и размера, что и будущая
 * надпись. Enter или клик мимо ставит надпись, Esc — отменяет (клавиши ловит холст).
 */
@Composable
internal fun CanvasTextInput(state: ScreenshotEditorState, fit: ImageFit, viewport: CanvasViewport, onEvent: (ScreenshotEditorEvent) -> Unit) {
    val at = state.textAt ?: return
    val image = state.image ?: return
    val focus = remember { FocusRequester() }
    LaunchedEffect(at) { focus.requestFocus() }
    val position = viewport.toWindow(fit.toScreen(at))
    // правят старую надпись — её размер, новую — размер из настроек
    val size = (state.editing?.let(state.history.edits::getOrNull) as? Edit.Label)?.size ?: labelSize(state.thickness, image.width, image.height)
    val sizePx = size * fit.scale * viewport.zoom
    val color = Color(state.color)
    val fontSize = with(LocalDensity.current) { sizePx.toSp() }
    BasicTextField(
        value = state.text,
        onValueChange = { onEvent(ScreenshotEditorEvent.Text(it)) },
        modifier = Modifier
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .widthIn(min = ScreenshotEditorDimens.textInputMinWidth)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.accentGold)
            .focusRequester(focus),
        singleLine = true,
        textStyle = TextStyle(color = color, fontSize = fontSize, fontWeight = FontWeight.Bold),
        cursorBrush = SolidColor(color),
    )
}
