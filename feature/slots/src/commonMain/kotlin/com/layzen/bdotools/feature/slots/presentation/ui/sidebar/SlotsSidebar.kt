package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_account
import com.layzen.bdotools.feature.slots.resources.slots_account_description
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Левая панель: аккаунт, пресеты, сетка, подложка и координаты выбранного слота. */
@Composable
internal fun SlotsSidebar(state: SlotsState, onEvent: (SlotsEvent) -> Unit, modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    Column(
        modifier
            .verticalScrollbar(scroll)
            .verticalScroll(scroll)
            .padding(AppTheme.dimens.spaceL),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
    ) {
        val account = state.loaded?.account
        if (state.accounts.size > 1 && account != null) {
            SectionHeader(stringResource(Res.string.slots_account), description = stringResource(Res.string.slots_account_description))
            DropdownSelect(account, state.accounts, { onEvent(SlotsEvent.SelectAccount(it)) }, { it })
            SectionDivider()
        }
        GamePresetSettings(state, onEvent)
        SectionDivider()
        PresetList(state, onEvent)
        SectionDivider()
        GridSettings(state, onEvent)
        SectionDivider()
        BackgroundSettings(state, onEvent)
        SectionDivider()
        SlotInspector(state, onEvent)
    }
}
