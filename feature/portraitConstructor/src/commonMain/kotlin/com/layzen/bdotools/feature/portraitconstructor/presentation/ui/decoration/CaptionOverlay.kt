package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitOverlayLayout as Layout
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitTexture
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitDecorator
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Надпись на предпросмотре — сразу поле ввода: тот же шрифт, размер, тень и место, что у надписи,
 * которую отрисовщик пишет в файл (затемнение под ней рисует сам предпросмотр). Клик по надписи
 * ставит курсор, пустое поле показывает [hint] тем же стилем. [color] — цвет надписи, `null` — по умолчанию. У непустой надписи справа — крестик.
 */
@Composable
internal fun BoxScope.CaptionOverlay(
    caption: String,
    color: Int?,
    hint: String,
    decorator: PortraitDecorator,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val shown = caption.ifBlank { hint }
    val fontSize = remember(shown, decorator) { decorator.captionFontSize(shown) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.matchParentSize()) {
        val texturePixel = maxWidth / PortraitTexture.WIDTH
        val style = decorator.captionStyle(fontSize, color).scaled(texturePixel, density)
        val left = texturePixel * Layout.CAPTION_PADDING_HORIZONTAL
        val width = maxWidth - left * 2
        val bottom = texturePixel * (Layout.CAPTION_BOTTOM - Layout.CAPTION_PADDING_VERTICAL)
        Box(Modifier.offset(x = left).width(width).height(bottom), contentAlignment = Alignment.BottomCenter) {
            CaptionField(caption, hint, style, onEvent, onTextLayout = { layout = it })
        }
        val text = layout
        if (caption.isNotEmpty() && text != null) {
            val textRight = with(density) { (0 until text.lineCount).maxOf { text.getLineRight(it) }.toDp() }
            val textTop = bottom - with(density) { text.size.height.toDp() }
            RemoveButton(
                onClick = { onEvent(PortraitsEvent.CaptionChanged("")) },
                modifier = Modifier.offset(x = left + textRight, y = textTop),
            )
        }
    }
}

@Composable
private fun CaptionField(
    caption: String,
    hint: String,
    style: TextStyle,
    onEvent: (PortraitsEvent) -> Unit,
    onTextLayout: (TextLayoutResult) -> Unit,
) {
    BasicTextField(
        value = caption,
        onValueChange = { onEvent(PortraitsEvent.CaptionChanged(it)) },
        textStyle = style,
        maxLines = PortraitDecoration.CAPTION_MAX_LINES,
        cursorBrush = SolidColor(AppTheme.colors.accentGold),
        onTextLayout = onTextLayout,
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box {
                if (caption.isEmpty()) {
                    Text(hint, style = style.copy(color = AppTheme.colors.textMuted), modifier = Modifier.fillMaxWidth())
                }
                inner()
            }
        },
    )
}

/** Стиль из пикселей текстуры в размер предпросмотра: [texturePixel] — сколько dp в одном пикселе. */
private fun TextStyle.scaled(texturePixel: Dp, density: Density): TextStyle = with(density) {
    val factor = texturePixel.value
    copy(
        fontSize = (fontSize.value * factor).dp.toSp(),
        lineHeight = (lineHeight.value * factor).dp.toSp(),
        shadow = shadow?.let {
            it.copy(
                offset = Offset(it.offset.x, it.offset.y) * (factor * density.density),
                blurRadius = it.blurRadius * factor * density.density,
            )
        },
    )
}
