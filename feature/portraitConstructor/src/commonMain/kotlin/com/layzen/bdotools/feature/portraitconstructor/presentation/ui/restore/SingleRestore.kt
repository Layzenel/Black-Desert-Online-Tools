package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.restore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.label
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitItem
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.RestoreDialogState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.previewKey
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid.PortraitGridLayout
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid.PortraitTile
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_current
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_description_one
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_warning
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Содержимое окна восстановления портрета [characterId]: версии из копий рядом с текущим портретом [current]. */
@Composable
internal fun SingleRestore(state: RestoreDialogState, current: PortraitItem?, characterId: String, onEvent: (PortraitsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        DescriptionText(stringResource(Res.string.portraits_restore_description_one))
        WarningText(stringResource(Res.string.portraits_restore_warning))
        val items = listOf<String?>(null) + state.backups.map { it.name }
        PortraitGridLayout(
            items = items,
            modifier = Modifier.heightIn(max = PortraitsDimens.restoreContentMaxHeight).verticalScroll(rememberScrollState()),
            columns = PortraitsDimens.RESTORE_ONE_COLUMNS,
        ) { name ->
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                if (name == null) {
                    PortraitTile(current?.image)
                    PreviewCaption(stringResource(Res.string.portraits_restore_current))
                } else {
                    PortraitTile(
                        image = state.previews[previewKey(name, characterId)],
                        selected = name == state.selected,
                        onClick = { onEvent(PortraitsEvent.SelectBackup(name)) },
                    )
                    PreviewCaption(state.backups.first { it.name == name }.label)
                }
            }
        }
    }
}
