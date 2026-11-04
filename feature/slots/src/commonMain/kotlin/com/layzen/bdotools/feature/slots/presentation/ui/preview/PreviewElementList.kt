package com.layzen.bdotools.feature.slots.presentation.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.domain.SlotGroup
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.presentation.ui.groupTitle
import com.layzen.bdotools.feature.slots.presentation.ui.slotTitle
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_preview_elements
import com.layzen.bdotools.feature.slots.resources.slots_preview_group_count
import com.layzen.bdotools.uikit.components.CheckBox
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Что импортировать или экспортировать: галочка у группы отмечает или снимает её целиком, под ней — элементы
 * по одному. Слоты рядов показаны номерами, прочие элементы — названиями.
 */
@Composable
internal fun PreviewElementList(ids: Set<SlotId>, included: Set<SlotId>, onEvent: (SlotsEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        SectionHeader(stringResource(Res.string.slots_preview_elements))
        SlotGroup.entries.forEach { group ->
            val members = group.slots.filter { it in ids }
            if (members.isNotEmpty()) GroupItems(group, members, included, onEvent)
        }
    }
}

@Composable
private fun GroupItems(group: SlotGroup, members: List<SlotId>, included: Set<SlotId>, onEvent: (SlotsEvent) -> Unit) {
    val chosen = members.count { it in included }
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        CheckRow(
            text = stringResource(Res.string.slots_preview_group_count, groupTitle(group), chosen, members.size),
            checked = chosen == members.size,
            onCheckedChange = { onEvent(SlotsEvent.PreviewInclude(members.toSet(), it)) },
            bold = true,
        )
        if (members.size > 1) ItemChecks(group, members, included, onEvent)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemChecks(group: SlotGroup, members: List<SlotId>, included: Set<SlotId>, onEvent: (SlotsEvent) -> Unit) {
    val toggle = { id: SlotId -> { checked: Boolean -> onEvent(SlotsEvent.PreviewInclude(setOf(id), checked)) } }
    if (group == SlotGroup.Interface) {
        Column(Modifier.padding(start = SlotsDimens.previewItemIndent), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            members.forEach { CheckRow(slotTitle(it), it in included, toggle(it)) }
        }
    } else {
        FlowRow(
            Modifier.padding(start = SlotsDimens.previewItemIndent),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
        ) {
            members.forEach { CheckRow(it.number.toString(), it in included, toggle(it)) }
        }
    }
}

@Composable
private fun CheckRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, bold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        CheckBox(checked, onCheckedChange)
        Text(
            text,
            style = if (bold) AppTheme.typography.bodyBold else AppTheme.typography.caption,
            color = if (bold) AppTheme.colors.textBeige else AppTheme.colors.textPrimary,
        )
    }
}
