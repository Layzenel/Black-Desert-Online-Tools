package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.components.ContentFrame
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.Skeleton
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.showcase_page_apply
import com.layzen.bdotools.uikit.resources.showcase_page_body
import com.layzen.bdotools.uikit.resources.showcase_page_menu
import com.layzen.bdotools.uikit.resources.showcase_page_open
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

private const val FRAME_TILES = 4

/** Пример страницы «по центру»: подложка с заголовком, сеткой плиток и строкой действий под ней. */
@Composable
internal fun ContentFrameSample(modifier: Modifier) {
    ContentFrame(modifier) {
        SectionHeader(stringResource(Res.string.showcase_page_menu), description = stringResource(Res.string.showcase_page_body))
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            repeat(FRAME_TILES) { Tile(Modifier.size(ShowcaseDimens.tileSize)) { Skeleton(Modifier.matchParentSize()) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            MenuAction(stringResource(Res.string.showcase_page_open), onClick = {}, modifier = Modifier.weight(1f))
            MenuAction(stringResource(Res.string.showcase_page_apply), onClick = {}, modifier = Modifier.weight(1f))
        }
    }
}
