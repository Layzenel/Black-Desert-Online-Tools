package com.layzen.bdotools.feature.uieditor.presentation.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.chatTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_chats
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_group_count
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Окна чата: галочка группы и окна по одному, названиями, как в редакторе. Нет окон — нет и группы. */
@Composable
internal fun PreviewChatList(windows: Set<Int>, included: Set<Int>, onEvent: (SlotsEvent) -> Unit) {
    if (windows.isEmpty()) return
    val chosen = windows.count { it in included }
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        PreviewCheckRow(
            text = stringResource(Res.string.slots_preview_group_count, stringResource(Res.string.slots_preview_chats), chosen, windows.size),
            checked = chosen == windows.size,
            onCheckedChange = { onEvent(SlotsEvent.PreviewInclude(emptySet(), chats = windows, include = it)) },
            bold = true,
        )
        if (windows.size > 1) {
            Column(Modifier.padding(start = SlotsDimens.previewItemIndent), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                windows.forEach { window ->
                    PreviewCheckRow(chatTitle(window), window in included, { onEvent(SlotsEvent.PreviewInclude(emptySet(), chats = setOf(window), include = it)) })
                }
            }
        }
    }
}
