package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.HoverMenuBox
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.PopupMenuItem
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.components.ValueText
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.showcase_exit_change_server
import com.layzen.bdotools.uikit.resources.showcase_exit_character
import com.layzen.bdotools.uikit.resources.showcase_exit_family
import com.layzen.bdotools.uikit.resources.showcase_exit_quit
import com.layzen.bdotools.uikit.resources.showcase_exit_select
import com.layzen.bdotools.uikit.resources.showcase_exit_tile_stats
import com.layzen.bdotools.uikit.resources.showcase_exit_title
import com.layzen.bdotools.uikit.resources.showcase_modal_close
import com.layzen.bdotools.uikit.resources.showcase_modal_description
import com.layzen.bdotools.uikit.resources.showcase_modal_header
import com.layzen.bdotools.uikit.resources.showcase_modal_title
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Пример окна выбора персонажа: плитки с меню по наведению и модальное окно. */
@Composable
internal fun TilesSample(modifier: Modifier) {
    var selected by remember { mutableIntStateOf(0) }
    var modalOpen by remember { mutableStateOf(false) }
    if (modalOpen) {
        ModalWindow(
            title = stringResource(Res.string.showcase_modal_title),
            onDismissRequest = { modalOpen = false },
            footer = { ActionButton(stringResource(Res.string.showcase_modal_close), onClick = { modalOpen = false }) },
        ) {
            SectionHeader(
                stringResource(Res.string.showcase_modal_header),
                modifier = Modifier.padding(AppTheme.dimens.spaceL),
                description = stringResource(Res.string.showcase_modal_description),
            )
        }
    }
    GameWindow(
        title = stringResource(Res.string.showcase_exit_title),
        modifier = modifier,
        footer = {
            ActionButton(stringResource(Res.string.showcase_exit_change_server), onClick = {}, modifier = Modifier.weight(1f))
            ActionButton(stringResource(Res.string.showcase_exit_quit), onClick = {}, modifier = Modifier.weight(1f))
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceM), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    ValueText(stringResource(Res.string.showcase_exit_character, 68, "Exegi"))
                    Text(
                        stringResource(Res.string.showcase_exit_family),
                        style = AppTheme.typography.bodyBold,
                        color = AppTheme.colors.textBeige,
                    )
                }
                ActionButton(stringResource(Res.string.showcase_exit_select), onClick = { modalOpen = true })
            }
            (0 until 2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                    (0 until 4).forEach { col ->
                        val index = row * 4 + col
                        HoverMenuBox(
                            modifier = Modifier.size(ShowcaseDimens.tileSize),
                            menu = { close ->
                                PopupMenuItem(stringResource(Res.string.showcase_exit_select), onClick = {
                                    selected = index
                                    close()
                                })
                                PopupMenuItem(stringResource(Res.string.showcase_modal_title), onClick = {
                                    close()
                                    modalOpen = true
                                })
                            },
                        ) { menuOpen ->
                            Tile(Modifier.matchParentSize(), selected = index == selected || menuOpen) {
                                Text(
                                    stringResource(Res.string.showcase_exit_tile_stats, 394, 2),
                                    style = AppTheme.typography.bodyBold,
                                    color = AppTheme.colors.textPrimary,
                                    modifier = Modifier.align(Alignment.BottomCenter).padding(AppTheme.dimens.spaceXs),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
