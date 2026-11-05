package com.layzen.bdotools.account

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.account_option
import com.layzen.bdotools.resources.account_switcher_label
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Смена аккаунта в верхней навигации: подпись и список «номер · число персонажей». */
@Composable
internal fun AccountSwitcher(selected: GameAccount, accounts: List<GameAccount>, onSelect: (String) -> Unit) {
    Text(stringResource(Res.string.account_switcher_label), style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textBeige)
    DropdownSelect(
        selected = selected,
        options = accounts,
        onSelect = { onSelect(it.id) },
        label = { stringResource(Res.string.account_option, it.id, it.characterIds.size) },
        modifier = Modifier.width(AccountDimens.switcherWidth),
    )
}
