package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Что умеет тулза — под просьбой выбрать папку, пока данных ещё нет: золотые маркеры с пунктами [points]
 * и скриншот [image] с описанием [imageDescription] для чтения с экрана.
 */
@Composable
fun ToolIntro(points: List<String>, image: Painter, imageDescription: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            points.forEach { IntroPoint(it) }
        }
        Image(
            image,
            contentDescription = imageDescription,
            modifier = Modifier.fillMaxWidth().heightIn(max = AppTheme.dimens.introImageMaxHeight),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun IntroPoint(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        Text(INTRO_BULLET, style = AppTheme.typography.bodyBold, color = AppTheme.colors.accentGold)
        Text(text, style = AppTheme.typography.body, color = AppTheme.colors.textPrimary)
    }
}

/** Маркер пункта: тире есть в шрифте web-сборки, в отличие от «•» других шрифтов. */
private const val INTRO_BULLET = "—"
