package com.layzen.bdotools.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.account_picker_apply
import com.layzen.bdotools.resources.account_picker_description
import com.layzen.bdotools.resources.account_picker_title
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Окно выбора аккаунта при первом чтении папки, где играли с нескольких аккаунтов. Закрыть его без выбора нельзя:
 * без аккаунта тулзы не знают, чьи портреты и слоты показывать. [accounts] — от последнего сыгранного.
 */
@Composable
internal fun AccountPicker(
    accounts: List<GameAccount>,
    onSelect: (String) -> Unit,
    readPortrait: suspend (String) -> ByteArray?,
) {
    var chosen by remember(accounts) { mutableStateOf(accounts.first().id) }
    val scroll = rememberScrollState()
    ModalWindow(
        title = stringResource(Res.string.account_picker_title),
        onDismissRequest = {},
        modifier = Modifier.width(AccountDimens.pickerWidth),
        dismissible = false,
        footer = { ActionButton(stringResource(Res.string.account_picker_apply), onClick = { onSelect(chosen) }) },
    ) {
        DescriptionText(stringResource(Res.string.account_picker_description))
        Column(
            Modifier
                .heightIn(max = AccountDimens.pickerListMaxHeight)
                .verticalScrollbar(scroll)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            accounts.forEachIndexed { index, account ->
                AccountCard(
                    account = account,
                    lastPlayed = index == 0,
                    selected = account.id == chosen,
                    onClick = { chosen = account.id },
                    readPortrait = readPortrait,
                )
            }
        }
    }
}
