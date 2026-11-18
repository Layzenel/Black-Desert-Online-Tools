package com.layzen.bdotools.feature.comboassist.presentation.ui.skill

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Иконка умения: тёмный квадрат, пока иконка грузится. На перезарядке иконка плавно темнеет, по центру — секунды,
 * внизу — тонкая полоска оставшейся доли; [crossed] — перечёркнута (сгоревший шаг).
 *
 * @param cooldownLeftMs сколько осталось до отката, мс; 0 — готово
 * @param cooldownMs полная перезарядка — длина полоски
 */
@Composable
internal fun SkillIconView(
    icon: ImageBitmap?,
    size: Dp,
    modifier: Modifier = Modifier,
    frame: Color = AppTheme.colors.border,
    frameWidth: Dp = ComboDimens.frame,
    cooldownLeftMs: Long = 0,
    cooldownMs: Long = 0,
    crossed: Boolean = false,
) {
    Box(modifier.size(size).background(AppTheme.colors.backgroundInput).border(frameWidth, frame), contentAlignment = Alignment.Center) {
        icon?.let { Image(it, contentDescription = null, Modifier.size(size), contentScale = ContentScale.Crop) }
        val shadeAlpha by animateFloatAsState(if (cooldownLeftMs > 0) SHADE else 0f, tween(FADE_MS))
        if (shadeAlpha > 0f) {
            val share = if (cooldownMs > 0) (cooldownLeftMs.toFloat() / cooldownMs).coerceIn(0f, 1f) else 0f
            val shade = AppTheme.colors.silhouette
            val bar = AppTheme.colors.accentGoldPale
            Canvas(Modifier.size(size)) {
                drawRect(shade.copy(alpha = shadeAlpha))
                val height = this.size.height / BAR_SHARE
                drawRect(bar, topLeft = Offset(0f, this.size.height - height), size = Size(this.size.width * share, height))
            }
        }
        if (cooldownLeftMs > 0) Text(cooldownText(cooldownLeftMs), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textPrimary)
        if (crossed) {
            val color = AppTheme.colors.danger
            Canvas(Modifier.size(size)) {
                val stroke = this.size.minDimension / CROSS_STROKE_SHARE
                drawLine(color, Offset.Zero, Offset(this.size.width, this.size.height), stroke)
                drawLine(color, Offset(this.size.width, 0f), Offset(0f, this.size.height), stroke)
            }
        }
    }
}

/** Секунды до отката: целые, а под конец — с десятыми. */
internal fun cooldownText(ms: Long): String =
    if (ms >= MS_PER_SECOND) ((ms + MS_PER_SECOND - 1) / MS_PER_SECOND).toString() else "0.${ms / TENTH_MS}"

private const val MS_PER_SECOND = 1000L
private const val TENTH_MS = 100L
private const val SHADE = 0.55f
private const val FADE_MS = 250
private const val BAR_SHARE = 12f
private const val CROSS_STROKE_SHARE = 12f
