package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

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
import androidx.compose.ui.graphics.ColorFilter
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.BadgeGroup
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.SlotIcon
import com.layzen.bdotools.feature.portraitconstructor.presentation.drawable
import com.layzen.bdotools.feature.portraitconstructor.presentation.label
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_badge_add
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_badge_change
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_badge_group_class
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_badge_group_functional
import com.layzen.bdotools.uikit.components.GroupedOptionsMenu
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Значок на предпросмотре: клик открывает список «Класс» / «Функциональные», наведение на группу
 * раскрывает её значки. Пустой значок — «+», у поставленного справа сверху крестик, который его убирает.
 */
@Composable
internal fun BadgeButton(icon: SlotIcon?, onSelect: (SlotIcon?) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        BadgeTarget(
            filled = icon != null,
            contentDescription = stringResource(if (icon == null) Res.string.portraits_badge_add else Res.string.portraits_badge_change),
            onClick = { expanded = true },
            modifier = Modifier.matchParentSize(),
        )
        if (icon != null) RemoveButton(onClick = { onSelect(null) }, modifier = Modifier.align(Alignment.TopEnd))
        GroupedOptionsMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            groups = BadgeGroup.entries,
            groupLabel = { stringResource(it.label) },
            options = { it.icons },
            selected = icon,
            onSelect = onSelect,
            label = { stringResource(it.label) },
            icon = { BadgeMenuIcon(it) },
        )
    }
}

@Composable
private fun BadgeMenuIcon(icon: SlotIcon) {
    Image(
        painterResource(icon.drawable),
        contentDescription = null,
        colorFilter = ColorFilter.tint(AppTheme.colors.textBeige),
        modifier = Modifier.size(AppTheme.dimens.iconSize),
    )
}

private val BadgeGroup.label
    get() = when (this) {
        BadgeGroup.Class -> Res.string.portraits_badge_group_class
        BadgeGroup.Functional -> Res.string.portraits_badge_group_functional
    }
