package com.layzen.bdotools.feature.portraits.presentation.ui.decoration

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.presentation.drawable
import com.layzen.bdotools.feature.portraits.presentation.label
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_add
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_change
import com.layzen.bdotools.uikit.components.OptionsMenu
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Место значка на предпросмотре: клик открывает список значков. Пустое место показывает «+»,
 * у поставленного значка справа сверху — крестик, который его убирает.
 */
@Composable
internal fun BadgeSlotButton(icon: BadgeIcon?, onSelect: (BadgeIcon?) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        BadgeSlotTarget(
            filled = icon != null,
            contentDescription = stringResource(if (icon == null) Res.string.portraits_badge_add else Res.string.portraits_badge_change),
            onClick = { expanded = true },
            modifier = Modifier.matchParentSize(),
        )
        if (icon != null) BadgeRemoveButton(onClick = { onSelect(null) }, modifier = Modifier.align(Alignment.TopEnd))
        OptionsMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            options = BadgeIcon.entries,
            selected = icon,
            onSelect = onSelect,
            label = { stringResource(it.label) },
            icon = { Image(painterResource(it.drawable), contentDescription = null, modifier = Modifier.size(AppTheme.dimens.iconSize)) },
        )
    }
}
