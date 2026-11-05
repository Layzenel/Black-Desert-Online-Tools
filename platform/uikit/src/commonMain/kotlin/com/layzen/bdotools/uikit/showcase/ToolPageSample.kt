package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import com.layzen.bdotools.uikit.components.ActionMenuItem
import com.layzen.bdotools.uikit.components.ContextMenu
import com.layzen.bdotools.uikit.components.onSecondaryClick
import com.layzen.bdotools.uikit.components.ActionBar
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.components.ToolPage
import com.layzen.bdotools.uikit.components.ToolbarButton
import com.layzen.bdotools.uikit.components.ToolbarDivider
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.showcase_page_apply
import com.layzen.bdotools.uikit.resources.showcase_page_body
import com.layzen.bdotools.uikit.resources.showcase_page_disabled
import com.layzen.bdotools.uikit.resources.showcase_page_hotkey
import com.layzen.bdotools.uikit.resources.showcase_page_menu
import com.layzen.bdotools.uikit.resources.showcase_page_mode
import com.layzen.bdotools.uikit.resources.showcase_page_open
import com.layzen.bdotools.uikit.resources.showcase_page_undo
import com.layzen.bdotools.uikit.resources.showcase_page_tool
import com.layzen.bdotools.uikit.resources.showcase_tool
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Пример страницы тулзы без шапки: меню слева с действиями и режимом, панель действий над содержимым. */
@Composable
internal fun ToolPageSample(modifier: Modifier) {
    var mode by remember { mutableStateOf(false) }
    var menuAt by remember { mutableStateOf<Offset?>(null) }
    ToolPage(
        modifier = modifier.height(ShowcaseDimens.toolPageHeight),
        sidebar = {
            SectionHeader(stringResource(Res.string.showcase_page_menu))
            MenuAction(stringResource(Res.string.showcase_page_open), onClick = {})
            MenuAction(
                stringResource(Res.string.showcase_page_mode),
                onClick = { mode = !mode },
                selected = mode,
                hotkey = if (mode) stringResource(Res.string.showcase_page_hotkey) else null,
            )
            MenuAction(stringResource(Res.string.showcase_page_disabled), onClick = {}, enabled = false)
        },
        sidebarFooter = { ActionButton(stringResource(Res.string.showcase_page_apply), onClick = {}, modifier = Modifier.fillMaxWidth()) },
    ) {
        Column {
            ActionBar {
                ActionButton(stringResource(Res.string.showcase_page_undo), onClick = {})
                ToolbarDivider()
                ToolbarButton(
                    painterResource(Res.drawable.showcase_tool),
                    stringResource(Res.string.showcase_page_tool),
                    onClick = { mode = !mode },
                    selected = mode,
                )
                Spacer(Modifier.weight(1f))
                ActionButton(stringResource(Res.string.showcase_page_apply), onClick = {})
            }
            Box(Modifier.fillMaxSize().onSecondaryClick { menuAt = it }) {
                DescriptionText(stringResource(Res.string.showcase_page_body), Modifier.padding(AppTheme.dimens.spaceL))
                menuAt?.let { at ->
                    Box(Modifier.offset { IntOffset(at.x.roundToInt(), at.y.roundToInt()) }) {
                        ContextMenu<Unit>(
                            expanded = true,
                            onDismissRequest = { menuAt = null },
                            submenu = { _ -> ActionMenuItem(stringResource(Res.string.showcase_page_undo), onClick = { menuAt = null }) },
                        ) { submenuTrigger ->
                            ActionMenuItem(stringResource(Res.string.showcase_page_open), onClick = {}, modifier = submenuTrigger(Unit))
                            ActionMenuItem(stringResource(Res.string.showcase_page_disabled), onClick = {}, enabled = false)
                        }
                    }
                }
            }
        }
    }
}
