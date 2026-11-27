package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.theme.AppTheme
import com.layzen.bdotools.uikit.theme.AppShapes

/** Маленькая цветная кнопка прямо на скриншоте: галочка и крестик обрезки, крестик удаления выбранной правки. */
@Composable
internal fun CanvasButton(icon: Painter, color: Color, tooltip: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    HoverTooltip(tooltip, modifier) {
        Box(
            Modifier
                .size(ScreenshotEditorDimens.canvasButton)
                .background(color, AppShapes.Control)
                .border(AppTheme.dimens.borderWidth, AppTheme.colors.backgroundDeep, AppShapes.Control)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(icon, tooltip, Modifier.size(ScreenshotEditorDimens.canvasButtonIcon), colorFilter = ColorFilter.tint(Color.White))
        }
    }
}
