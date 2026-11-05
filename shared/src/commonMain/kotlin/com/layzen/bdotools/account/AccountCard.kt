package com.layzen.bdotools.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.account_details
import com.layzen.bdotools.resources.account_last_played
import com.layzen.bdotools.resources.account_more_portraits
import com.layzen.bdotools.resources.account_played_days_ago
import com.layzen.bdotools.resources.account_played_today
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.time.Clock
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val SHOWN_PORTRAITS = 9
private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

/** Аккаунт в окне выбора: номер, давность игры, число персонажей и первые портреты в порядке окна игры. */
@Composable
internal fun AccountCard(
    account: GameAccount,
    lastPlayed: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    readPortrait: suspend (String) -> ByteArray?,
) {
    val dimens = AppTheme.dimens
    Tile(Modifier.fillMaxWidth(), selected = selected, onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(dimens.spaceM), verticalArrangement = Arrangement.spacedBy(dimens.spaceS)) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceS), verticalAlignment = Alignment.CenterVertically) {
                Text(account.id, style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textPrimary)
                if (lastPlayed) {
                    Text(stringResource(Res.string.account_last_played), style = AppTheme.typography.caption, color = AppTheme.colors.accentGold)
                }
            }
            DescriptionText(stringResource(Res.string.account_details, playedAgo(account.lastPlayed), account.characterIds.size))
            AccountPortraits(account.characterIds, readPortrait)
        }
    }
}

@Composable
private fun AccountPortraits(characterIds: List<String>, readPortrait: suspend (String) -> ByteArray?) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        characterIds.take(SHOWN_PORTRAITS).forEach { AccountPortrait(it, readPortrait) }
        val rest = characterIds.size - SHOWN_PORTRAITS
        if (rest > 0) {
            Text(stringResource(Res.string.account_more_portraits, rest), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        }
    }
}

// Без часового пояса: сутки считаются от текущего момента, для «давно ли играли» этого хватает
@Composable
private fun playedAgo(lastPlayed: Long): String {
    val days = ((Clock.System.now().toEpochMilliseconds() - lastPlayed) / DAY_MILLIS).toInt().coerceAtLeast(0)
    return if (days == 0) stringResource(Res.string.account_played_today)
    else pluralStringResource(Res.plurals.account_played_days_ago, days, days)
}
