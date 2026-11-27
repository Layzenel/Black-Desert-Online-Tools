package com.layzen.bdotools.composable.canvas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.composable.resources.Res
import com.layzen.bdotools.composable.resources.canvas_zoom
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val PERCENT = 100

/** Масштаб холста [viewport] в процентах для панели инструментов; колесо мыши над холстом двигает этот же ползунок. */
@Composable
fun ZoomSlider(viewport: CanvasViewport) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalAlignment = Alignment.CenterVertically) {
        DescriptionText(stringResource(Res.string.canvas_zoom))
        ValueSlider(
            (viewport.zoom * PERCENT).roundToInt(),
            { viewport.zoomTo(it.toFloat() / PERCENT) },
            (CanvasViewport.MIN_ZOOM * PERCENT).toInt()..(CanvasViewport.MAX_ZOOM * PERCENT).toInt(),
            Modifier.width(CanvasDimens.zoomSliderWidth),
        )
    }
}
