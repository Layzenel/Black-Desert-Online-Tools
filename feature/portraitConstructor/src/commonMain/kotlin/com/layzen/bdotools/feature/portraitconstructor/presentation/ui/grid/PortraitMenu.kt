package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_menu_edit
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_menu_replace
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_menu_restore
import com.layzen.bdotools.uikit.components.ActionMenuDivider
import com.layzen.bdotools.uikit.components.ActionMenuItem
import org.jetbrains.compose.resources.stringResource

/** Действия над портретом: замена, надпись и значки и — если есть копии — откат. */
@Composable
internal fun ColumnScope.PortraitMenu(
    characterId: String,
    hasBackups: Boolean,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val items = buildList {
        add(Res.string.portraits_menu_replace to PortraitsEvent.OpenSource(ReplaceTarget.One(characterId)))
        add(Res.string.portraits_menu_edit to PortraitsEvent.OpenDecoration(characterId))
        if (hasBackups) add(Res.string.portraits_menu_restore to PortraitsEvent.OpenRestore(ReplaceTarget.One(characterId)))
    }
    items.forEachIndexed { index, (title, event) ->
        if (index > 0) ActionMenuDivider()
        ActionMenuItem(stringResource(title), onClick = { onEvent(event) })
    }
}
