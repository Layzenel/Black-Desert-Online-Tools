package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_alert_edit
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery_reveal
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery_trash
import com.layzen.bdotools.uikit.components.ActionMenuItem
import com.layzen.bdotools.uikit.components.HoverActionsBox
import com.layzen.bdotools.uikit.components.Skeleton
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Пропорции миниатюры — как у экрана игры. */
private const val THUMBNAIL_RATIO = 16f / 9f

/**
 * Плитка скриншота в галерее: миниатюра и имя. Клик открывает скриншот, по наведению — действия; в настольной
 * версии ([canManageFiles]) ещё «Показать в папке» и «В корзину». Миниатюру просит [onLoad], когда плитка видна.
 */
@Composable
internal fun GalleryTile(
    entry: ScreenshotEntry,
    thumbnail: ImageBitmap?,
    canManageFiles: Boolean,
    onOpen: () -> Unit,
    onReveal: () -> Unit,
    onTrash: () -> Unit,
    onLoad: () -> Unit,
) {
    LaunchedEffect(entry.id) { onLoad() }
    HoverActionsBox(
        actions = {
            ActionMenuItem(stringResource(Res.string.screenshots_alert_edit), onOpen)
            if (canManageFiles) {
                ActionMenuItem(stringResource(Res.string.screenshots_gallery_reveal), onReveal)
                ActionMenuItem(stringResource(Res.string.screenshots_gallery_trash), onTrash)
            }
        },
    ) {
        Tile(onClick = onOpen) {
            Column(Modifier.padding(AppTheme.dimens.spaceXs)) {
                val picture = Modifier.fillMaxWidth().aspectRatio(THUMBNAIL_RATIO)
                if (thumbnail == null) {
                    Skeleton(picture)
                } else {
                    Image(thumbnail, contentDescription = entry.name, modifier = picture, contentScale = ContentScale.Crop)
                }
                Text(
                    entry.name,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
