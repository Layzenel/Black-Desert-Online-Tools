package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.uikit.theme.AppTheme

// Затемнение за рамкой обрезки: то, что не попадёт в копию, видно, но приглушено
private const val CROP_SHADE_ALPHA = 0.7f

/**
 * Цвета поверх скриншота.
 *
 * @property shade затемнение за обводкой обрезки
 * @property border рамка обрезки и пунктир выбранной правки
 * @property outline тёмная подложка под пунктиром: он виден и на светлом, и на тёмном
 */
internal class CanvasColors(val shade: Color, val border: Color, val outline: Color)

/** Цвета холста из темы. */
@Composable
internal fun canvasColors() = CanvasColors(
    shade = AppTheme.colors.backgroundDeep.copy(alpha = CROP_SHADE_ALPHA),
    border = AppTheme.colors.accentGold,
    outline = AppTheme.colors.backgroundDeep,
)
