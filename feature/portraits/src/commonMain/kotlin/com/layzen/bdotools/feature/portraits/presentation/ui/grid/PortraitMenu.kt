package com.layzen.bdotools.feature.portraits.presentation.ui.grid

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.DecorationMode
import com.layzen.bdotools.feature.portraits.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_menu_badges
import com.layzen.bdotools.feature.portraits.resources.portraits_menu_caption
import com.layzen.bdotools.feature.portraits.resources.portraits_menu_replace
import com.layzen.bdotools.feature.portraits.resources.portraits_menu_restore
import com.layzen.bdotools.uikit.components.PopupMenuDivider
import com.layzen.bdotools.uikit.components.PopupMenuItem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Меню портрета: замена, надпись, значки и — если есть копии — восстановление. */
@Composable
internal fun ColumnScope.PortraitMenu(
    characterId: String,
    hasBackups: Boolean,
    close: () -> Unit,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val items = buildList {
        add(Res.string.portraits_menu_replace to PortraitsEvent.OpenSource(ReplaceTarget.One(characterId)))
        add(Res.string.portraits_menu_caption to PortraitsEvent.OpenDecoration(characterId, DecorationMode.Caption))
        add(Res.string.portraits_menu_badges to PortraitsEvent.OpenDecoration(characterId, DecorationMode.Badges))
        if (hasBackups) add(Res.string.portraits_menu_restore to PortraitsEvent.OpenRestore(ReplaceTarget.One(characterId)))
    }
    items.forEachIndexed { index, (title, event) ->
        if (index > 0) PopupMenuDivider()
        MenuAction(title) {
            close()
            onEvent(event)
        }
    }
}

@Composable
private fun MenuAction(title: StringResource, onClick: () -> Unit) {
    PopupMenuItem(stringResource(title), onClick = onClick)
}
