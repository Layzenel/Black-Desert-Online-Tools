package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraitconstructor.presentation.drawable
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_backups
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_edit
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_upload
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_menu_edit
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_menu_restore
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_menu_upload
import com.layzen.bdotools.uikit.components.ActionMenuDivider
import com.layzen.bdotools.uikit.components.ActionMenuItem
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Действия над портретом: замена, надпись и значки и — если есть копии — откат. */
@Composable
internal fun ColumnScope.PortraitMenu(
    characterId: String,
    hasBackups: Boolean,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val items = buildList {
        add(
            MenuItem(
                title = Res.string.portraits_menu_upload,
                icon = Res.drawable.menu_upload,
                event = PortraitsEvent.OpenSource(ReplaceTarget.One(characterId))
            )
        )
        add(
            MenuItem(
                title = Res.string.portraits_menu_edit,
                icon = Res.drawable.menu_edit,
                event = PortraitsEvent.OpenDecoration(characterId)
            )
        )
        if (hasBackups) {
            add(
                MenuItem(
                    title = Res.string.portraits_menu_restore,
                    icon = Res.drawable.menu_backups,
                    event = PortraitsEvent.OpenRestore(ReplaceTarget.One(characterId))
                )
            )
        }
    }
    items.forEachIndexed { index, item ->
        if (index > 0) ActionMenuDivider()
        ActionMenuItem(
            icon = painterResource(item.icon),
            text = stringResource(item.title),
            onClick = { onEvent(item.event) }
        )
    }
}

/** Модель данных для списка действие в меню потрета*/
internal data class MenuItem(
    val title: StringResource,
    val icon: DrawableResource,
    val event: PortraitsEvent,
)
