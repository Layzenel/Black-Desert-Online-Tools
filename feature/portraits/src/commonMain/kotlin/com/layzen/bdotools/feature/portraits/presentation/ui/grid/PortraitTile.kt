package com.layzen.bdotools.feature.portraits.presentation.ui.grid

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.layzen.bdotools.feature.portraits.domain.PortraitTexture
import com.layzen.bdotools.uikit.components.Skeleton
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.theme.AppTheme

private const val PORTRAIT_RATIO = PortraitTexture.WIDTH.toFloat() / PortraitTexture.HEIGHT
private const val DIMMED_ALPHA = 0.35f

/**
 * Портрет в ячейке сетки с пропорциями текстуры игры. Пока [image] не загружен — скелетон.
 * [dimmed] приглушает портрет (например, не отмеченный для восстановления).
 */
@Composable
internal fun PortraitTile(
    image: ImageBitmap?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    selected: Boolean = false,
    dimmed: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Tile(modifier.fillMaxWidth().aspectRatio(PORTRAIT_RATIO), selected = selected, onClick = onClick) {
        if (image == null) {
            Skeleton(Modifier.matchParentSize())
        } else {
            Image(
                bitmap = image,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Medium,
                modifier = Modifier.matchParentSize().alpha(if (dimmed) DIMMED_ALPHA else 1f),
            )
        }
    }
}
