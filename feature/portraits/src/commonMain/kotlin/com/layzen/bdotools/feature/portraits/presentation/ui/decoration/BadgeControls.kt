package com.layzen.bdotools.feature.portraits.presentation.ui.decoration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.domain.BadgeSlot
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.label
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_none
import com.layzen.bdotools.feature.portraits.resources.portraits_badges_clear
import com.layzen.bdotools.feature.portraits.resources.portraits_badges_description
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import org.jetbrains.compose.resources.stringResource

/** Выбор значка для каждого места и кнопка «Убрать значки». */
@Composable
internal fun BadgeControls(draft: PortraitDecoration, onEvent: (PortraitsEvent) -> Unit) {
    DescriptionText(stringResource(Res.string.portraits_badges_description))
    val options: List<BadgeIcon?> = listOf(null) + BadgeIcon.entries
    val none = stringResource(Res.string.portraits_badge_none)
    BadgeSlot.entries.forEach { slot ->
        SectionHeader(stringResource(slot.label))
        DropdownSelect(
            selected = draft.badges[slot],
            options = options,
            onSelect = { onEvent(PortraitsEvent.BadgeChanged(slot, it)) },
            label = { icon -> icon?.let { stringResource(it.label) } ?: none },
            modifier = Modifier.fillMaxWidth(),
        )
    }
    SectionDivider()
    ActionButton(
        stringResource(Res.string.portraits_badges_clear),
        onClick = { BadgeSlot.entries.forEach { onEvent(PortraitsEvent.BadgeChanged(it, null)) } },
        enabled = draft.badges.isNotEmpty(),
    )
}
