package com.layzen.bdotools.account

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.account_option
import com.layzen.bdotools.resources.account_switcher_label
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Смена аккаунта в верхней навигации: подпись и кнопка «номер · число персонажей», которая открывает то же окно
 * [AccountPicker], что и при первом выборе, — с портретами и давностью игры.
 */
@Composable
internal fun AccountSwitcher(
    selected: GameAccount,
    accounts: List<GameAccount>,
    onSelect: (String) -> Unit,
    readPortrait: suspend (String) -> ByteArray?,
) {
    var open by remember { mutableStateOf(false) }
    Text(stringResource(Res.string.account_switcher_label), style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textBeige)
    ActionButton(
        stringResource(Res.string.account_option, selected.id, selected.characterIds.size),
        onClick = { open = true },
        modifier = Modifier.width(AccountDimens.switcherWidth),
    )
    if (open) {
        AccountPicker(
            accounts = accounts,
            onSelect = {
                open = false
                onSelect(it)
            },
            readPortrait = readPortrait,
            current = selected.id,
            onDismiss = { open = false },
        )
    }
}
