package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.presentation.gallery.GalleryEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.gallery.GalleryState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_open
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_reload
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery_count
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery_empty
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery_refresh
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery_refresh_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery_title
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_open_file
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_open_tooltip
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Скриншоты игры сеткой, новые первыми; при каждом показе папка перечитывается. Пока доступа к папке нет —
 * вступление редактора с выбором папки ([EditorIntro]).
 */
@Composable
internal fun GalleryScreen(
    state: GalleryState,
    onEvent: (GalleryEvent) -> Unit,
    onOpen: (ScreenshotEntry) -> Unit,
    onOpenFile: () -> Unit,
) {
    LaunchedEffect(Unit) { onEvent(GalleryEvent.Refresh) }
    val access = state.access
    if (access !is FolderAccess.Granted) {
        EditorIntro(access, onOpenFile, onEvent)
        return
    }
    Column(
        Modifier.fillMaxSize().padding(AppTheme.dimens.spaceXl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            SectionHeader(
                stringResource(Res.string.screenshots_gallery_title),
                Modifier.weight(1f),
                description = stringResource(Res.string.screenshots_gallery_count, state.entries.size),
            )
            ActionButton(
                ActionButtonState.Neutral(
                    stringResource(Res.string.screenshots_gallery_refresh),
                    icon = painterResource(Res.drawable.menu_reload),
                    onClick = { onEvent(GalleryEvent.Refresh) },
                    tooltip = stringResource(Res.string.screenshots_gallery_refresh_tooltip),
                ),
            )
            ActionButton(
                ActionButtonState.Neutral(
                    stringResource(Res.string.screenshots_open_file),
                    icon = painterResource(Res.drawable.menu_open),
                    onClick = onOpenFile,
                    tooltip = stringResource(Res.string.screenshots_open_tooltip),
                ),
            )
        }
        if (state.entries.isEmpty()) DescriptionText(stringResource(Res.string.screenshots_gallery_empty))
        LazyVerticalGrid(
            GridCells.Adaptive(ScreenshotEditorDimens.galleryTile),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            items(state.entries, key = { it.id }) { entry ->
                GalleryTile(
                    entry,
                    state.thumbnails[entry.id],
                    state.canManageFiles,
                    onOpen = { onOpen(entry) },
                    onReveal = { onEvent(GalleryEvent.Reveal(entry)) },
                    onTrash = { onEvent(GalleryEvent.Trash(entry)) },
                    onLoad = { onEvent(GalleryEvent.LoadThumbnail(entry)) },
                )
            }
        }
    }
}
