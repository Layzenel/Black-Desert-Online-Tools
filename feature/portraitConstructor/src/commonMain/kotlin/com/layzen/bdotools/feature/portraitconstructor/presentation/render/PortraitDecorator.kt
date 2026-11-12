package com.layzen.bdotools.feature.portraitconstructor.presentation.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.DecorationStyle
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitOverlayLayout as Layout
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.SlotIcon
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitTexture
import com.layzen.bdotools.feature.portraitconstructor.presentation.allSlotIcons
import com.layzen.bdotools.feature.portraitconstructor.presentation.drawable
import com.layzen.bdotools.uikit.theme.AppColors
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource

/** Непрозрачный альфа-канал для цвета украшения `0xRRGGBB`. */
private const val OPAQUE = 0xFF000000.toInt()
private const val TEXT_SHADOW_ALPHA = 0.85f
private const val TEXT_SHADOW_OFFSET = 3f
private const val TEXT_SHADOW_BLUR = 6f

/** 1 sp = 1 px: рисуем в пикселях текстуры. */
private val TexturePixels = Density(1f)

/**
 * Рисует надпись и значки поверх портрета. Используется и для предпросмотра, и для записи в .bmp,
 * поэтому в игре портрет выглядит ровно так, как на странице.
 * Создаётся в композиции через [rememberPortraitDecorator]: ему нужны шрифты и SVG-значки.
 */
class PortraitDecorator internal constructor(
    private val textMeasurer: TextMeasurer,
    private val icons: Map<SlotIcon, Painter>,
    private val colors: AppColors,
) {
    /** SVG-значки на web загружаются асинхронно. Пока они не готовы, рисовать нельзя. */
    val isReady: Boolean
        get() = icons.values.all { it.intrinsicSize.isSpecified && it.intrinsicSize.width > 0f }

    /**
     * Портрет с украшениями: одна мягкая тень по контуру значка и надписи ([drawDecorationShade]), поверх значок
     * и надпись. Без [drawCaptionText] под надписью рисуется только тень:
     * сам текст на предпросмотре показывает поле ввода тем же стилем ([captionStyle]).
     */
    fun decorate(base: ImageBitmap, decoration: PortraitDecoration, drawCaptionText: Boolean = true): ImageBitmap {
        val target = ImageBitmap(base.width, base.height)
        CanvasDrawScope().draw(TexturePixels, LayoutDirection.Ltr, Canvas(target), Size(base.width.toFloat(), base.height.toFloat())) {
            drawImage(base)
            // раскладка задана для 624×804; если файл другого размера, растягиваем её
            scale(base.width / PortraitTexture.WIDTH.toFloat(), base.height / PortraitTexture.HEIGHT.toFloat(), pivot = Offset.Zero) {
                val style = decoration.style
                val caption = decoration.caption.trim().takeIf { it.isNotEmpty() }?.let { measure(it, captionFontSize(it), style.captionColor) }
                val shapes = listOfNotNull(decoration.badge?.let { badgeBounds() }, caption?.let(::captionBounds))
                drawDecorationShade(shapes, shadeColor(style), style.shadeFactor, Layout.SHADE_SPREAD * style.shadeSizeFactor)
                // значок крупный и может заходить под надпись, поэтому надпись рисуется поверх
                decoration.badge?.let {
                    drawIcon(icons.getValue(it), badgeBounds().center, Layout.BADGE_SIZE.toFloat(), decorationColor(style.badgeColor))
                }
                if (caption != null && drawCaptionText) {
                    drawText(caption, topLeft = Offset(Layout.CAPTION_PADDING_HORIZONTAL.toFloat(), captionTop(caption)))
                }
            }
        }
        return target
    }

    /** Верх строк надписи: надпись стоит над нижней полосой игры. */
    private fun captionTop(text: TextLayoutResult): Float =
        Layout.CAPTION_BOTTOM - Layout.CAPTION_PADDING_VERTICAL - text.size.height.toFloat()

    /** Рамка строк надписи по самой широкой строке, по центру портрета: под неё ложится тень. */
    private fun captionBounds(text: TextLayoutResult): Rect {
        val width = (0 until text.lineCount).maxOf { text.getLineRight(it) - text.getLineLeft(it) }
        val left = (PortraitTexture.WIDTH - width) / 2
        val top = captionTop(text)
        return Rect(left, top, left + width, top + text.size.height)
    }

    /** Рамка значка: квадрат по центру портрета. */
    private fun badgeBounds(): Rect = Rect(
        Offset(Layout.BADGE_CENTER_X.toFloat(), Layout.BADGE_CENTER_Y.toFloat()),
        Layout.BADGE_SIZE / 2f,
    )

    /**
     * Самый крупный шрифт (в пикселях текстуры), при котором надпись влезает в две строки и не поднимается
     * выше [Layout.CAPTION_MIN_TOP], то есть не заходит на нижние боковые значки.
     */
    fun captionFontSize(caption: String): Int {
        val maxTextHeight = Layout.CAPTION_BOTTOM - Layout.CAPTION_MIN_TOP - Layout.CAPTION_PADDING_VERTICAL * 2
        return Layout.CAPTION_FONT_SIZES.firstOrNull { size ->
            val text = measure(caption, size)
            !text.hasVisualOverflow && text.size.height <= maxTextHeight
        } ?: Layout.CAPTION_FONT_SIZES.last()
    }

    /**
     * Стиль надписи при шрифте [fontSize] в пикселях текстуры: 1 sp здесь равен 1 пикселю. [color] — цвет
     * надписи `0xRRGGBB`, `null` — кремовый цвет текста по умолчанию.
     */
    fun captionStyle(fontSize: Int, color: Int? = null): TextStyle = TextStyle(
        color = decorationColor(color),
        fontSize = fontSize.sp,
        lineHeight = (fontSize * Layout.CAPTION_LINE_HEIGHT_RATIO).sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        shadow = Shadow(Color.Black.copy(alpha = TEXT_SHADOW_ALPHA), Offset(0f, TEXT_SHADOW_OFFSET), TEXT_SHADOW_BLUR),
    )

    private fun measure(caption: String, fontSize: Int, color: Int? = null): TextLayoutResult = textMeasurer.measure(
        text = caption,
        style = captionStyle(fontSize, color),
        overflow = TextOverflow.Ellipsis,
        maxLines = PortraitDecoration.CAPTION_MAX_LINES,
        constraints = Constraints.fixedWidth(PortraitTexture.WIDTH - Layout.CAPTION_PADDING_HORIZONTAL * 2),
        density = TexturePixels,
    )

    /** Цвет украшения `0xRRGGBB` или кремовый цвет текста темы, если [rgb] `null`. */
    fun decorationColor(rgb: Int?): Color = rgb?.let { Color(OPAQUE or it) } ?: colors.textPrimary

    /** Цвет затенения под надписью и значком: из [style] или почти чёрный фон темы. */
    fun shadeColor(style: DecorationStyle): Color = style.shadeColor?.let { Color(OPAQUE or it) } ?: colors.backgroundDeep

}

/** Отрисовщик украшений с текущими шрифтами, SVG-значками и цветами темы. */
@Composable
fun rememberPortraitDecorator(): PortraitDecorator {
    val textMeasurer = rememberTextMeasurer()
    val icons = allSlotIcons.associateWith { painterResource(it.drawable) }
    val colors = AppTheme.colors
    return remember(textMeasurer, icons, colors) { PortraitDecorator(textMeasurer, icons, colors) }
}
