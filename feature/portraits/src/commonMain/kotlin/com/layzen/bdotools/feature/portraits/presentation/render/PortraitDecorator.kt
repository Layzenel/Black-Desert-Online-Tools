package com.layzen.bdotools.feature.portraits.presentation.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
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
import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.domain.BadgeSlot
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.domain.PortraitOverlayLayout as Layout
import com.layzen.bdotools.feature.portraits.domain.PortraitTexture
import com.layzen.bdotools.feature.portraits.presentation.drawable
import com.layzen.bdotools.uikit.theme.AppColors
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource

private const val GLOW_CENTER_ALPHA = 0.82f
private const val GLOW_MIDDLE_ALPHA = 0.62f
private const val GLOW_SOLID_STOP = 0.55f
private const val BADGE_PLATE_ALPHA = 0.82f
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
    private val icons: Map<BadgeIcon, Painter>,
    private val colors: AppColors,
) {
    /** SVG-значки на web загружаются асинхронно. Пока они не готовы, рисовать нельзя. */
    val isReady: Boolean
        get() = icons.values.all { it.intrinsicSize.isSpecified && it.intrinsicSize.width > 0f }

    fun decorate(base: ImageBitmap, decoration: PortraitDecoration): ImageBitmap {
        val target = ImageBitmap(base.width, base.height)
        CanvasDrawScope().draw(TexturePixels, LayoutDirection.Ltr, Canvas(target), Size(base.width.toFloat(), base.height.toFloat())) {
            drawImage(base)
            // раскладка задана для 624×804; если файл другого размера, растягиваем её
            scale(base.width / PortraitTexture.WIDTH.toFloat(), base.height / PortraitTexture.HEIGHT.toFloat(), pivot = Offset.Zero) {
                if (decoration.caption.isNotBlank()) drawCaption(decoration.caption.trim())
                decoration.badges.forEach { (slot, icon) -> drawBadge(slot, icon) }
            }
        }
        return target
    }

    /**
     * Надпись над нижней полосой игры. Под ней — овальное затемнение: плотное в центре текста
     * и растворяющееся к краям со всех сторон, поэтому нет резкой тёмной полосы.
     */
    private fun DrawScope.drawCaption(caption: String) {
        val text = measureCaption(caption)
        val blockHeight = text.size.height + Layout.CAPTION_PADDING_VERTICAL * 2f
        val blockTop = Layout.CAPTION_BOTTOM - blockHeight
        val center = Offset(PortraitTexture.WIDTH / 2f, blockTop + blockHeight / 2)
        val radiusX = PortraitTexture.WIDTH / 2f + Layout.CAPTION_GLOW_EXTRA_X
        val radiusY = blockHeight / 2 + Layout.CAPTION_GLOW_EXTRA_Y
        val shade = colors.backgroundDeep

        // радиальный градиент круглый, поэтому рисуем круг и сжимаем его по вертикали в овал
        scale(1f, radiusY / radiusX, pivot = center) {
            drawCircle(
                Brush.radialGradient(
                    0f to shade.copy(alpha = GLOW_CENTER_ALPHA),
                    GLOW_SOLID_STOP to shade.copy(alpha = GLOW_MIDDLE_ALPHA),
                    1f to Color.Transparent,
                    center = center,
                    radius = radiusX,
                ),
                radius = radiusX,
                center = center,
            )
        }
        drawText(text, topLeft = Offset(Layout.CAPTION_PADDING_HORIZONTAL.toFloat(), blockTop + Layout.CAPTION_PADDING_VERTICAL))
    }

    /**
     * Самый крупный шрифт, при котором надпись влезает в две строки и не поднимается выше
     * [Layout.CAPTION_MIN_TOP], то есть не заходит на нижние боковые значки.
     */
    private fun measureCaption(caption: String): TextLayoutResult {
        val maxTextHeight = Layout.CAPTION_BOTTOM - Layout.CAPTION_MIN_TOP - Layout.CAPTION_PADDING_VERTICAL * 2
        val width = PortraitTexture.WIDTH - Layout.CAPTION_PADDING_HORIZONTAL * 2
        fun measure(fontSize: Int) = textMeasurer.measure(
            text = caption,
            style = TextStyle(
                color = colors.textPrimary,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * Layout.CAPTION_LINE_HEIGHT_RATIO).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                shadow = Shadow(Color.Black.copy(alpha = TEXT_SHADOW_ALPHA), Offset(0f, TEXT_SHADOW_OFFSET), TEXT_SHADOW_BLUR),
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = PortraitDecoration.CAPTION_MAX_LINES,
            constraints = Constraints.fixedWidth(width),
            density = TexturePixels,
        )
        return Layout.CAPTION_FONT_SIZES.asSequence()
            .map(::measure)
            .firstOrNull { !it.hasVisualOverflow && it.size.height <= maxTextHeight }
            ?: measure(Layout.CAPTION_FONT_SIZES.last())
    }

    private fun DrawScope.drawBadge(slot: BadgeSlot, icon: BadgeIcon) {
        val center = Offset(Layout.badgeCenterX(slot).toFloat(), Layout.badgeCenterY(slot).toFloat())
        val radius = Layout.BADGE_DIAMETER / 2f
        drawCircle(colors.backgroundDeep.copy(alpha = BADGE_PLATE_ALPHA), radius, center)
        drawCircle(colors.accentGoldPale, radius - Layout.BADGE_RING_WIDTH / 2f, center, style = Stroke(Layout.BADGE_RING_WIDTH.toFloat()))

        val painter = icons.getValue(icon)
        val iconSize = Layout.BADGE_ICON_SIZE.toFloat()
        translate(center.x - iconSize / 2, center.y - iconSize / 2) {
            with(painter) { draw(Size(iconSize, iconSize), colorFilter = ColorFilter.tint(colors.textPrimary)) }
        }
    }
}

/** Отрисовщик украшений с текущими шрифтами, SVG-значками и цветами темы. */
@Composable
fun rememberPortraitDecorator(): PortraitDecorator {
    val textMeasurer = rememberTextMeasurer()
    val icons = BadgeIcon.entries.associateWith { painterResource(it.drawable) }
    val colors = AppTheme.colors
    return remember(textMeasurer, icons, colors) { PortraitDecorator(textMeasurer, icons, colors) }
}
