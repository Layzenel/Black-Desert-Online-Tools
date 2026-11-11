package com.layzen.bdotools.feature.uieditor.presentation.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.groupTitle
import com.layzen.bdotools.feature.uieditor.presentation.ui.slotTitle
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_elements
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_group_count
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Сколько номеров слотов в строке таблицы: ряды перезарядки и хотбара — по 5 в строке, как 1–5, 6–10. */
private const val NUMBER_COLUMNS = 5

/**
 * Что импортировать или экспортировать: галочка у группы отмечает или снимает её целиком, под ней — элементы
 * по одному. Слоты рядов показаны номерами в ровной таблице, прочие элементы — названиями списком.
 */
@Composable
internal fun PreviewElementList(ids: Set<SlotId>, included: Set<SlotId>, onEvent: (SlotsEvent) -> Unit) {
    SectionHeader(stringResource(Res.string.slots_preview_elements))
    SlotGroup.entries.forEach { group ->
        val members = group.slots.filter { it in ids }
        if (members.isNotEmpty()) GroupItems(group, members, included, onEvent)
    }
}

@Composable
private fun GroupItems(group: SlotGroup, members: List<SlotId>, included: Set<SlotId>, onEvent: (SlotsEvent) -> Unit) {
    val chosen = members.count { it in included }
    val toggle = { id: SlotId -> { checked: Boolean -> onEvent(SlotsEvent.PreviewInclude(setOf(id), checked)) } }
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        PreviewCheckRow(
            text = stringResource(Res.string.slots_preview_group_count, groupTitle(group), chosen, members.size),
            checked = chosen == members.size,
            onCheckedChange = { onEvent(SlotsEvent.PreviewInclude(members.toSet(), it)) },
            bold = true,
        )
        val items = Modifier.padding(start = SlotsDimens.previewItemIndent)
        when {
            members.size == 1 -> Unit
            group == SlotGroup.Interface -> Column(items, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                members.forEach { PreviewCheckRow(slotTitle(it), it in included, toggle(it)) }
            }
            else -> NumberTable(members, included, toggle, items)
        }
    }
}

/** Номера слотов таблицей с колонками одной ширины: «1» и «10» стоят ровно друг под другом. */
@Composable
private fun NumberTable(members: List<SlotId>, included: Set<SlotId>, toggle: (SlotId) -> (Boolean) -> Unit, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        members.chunked(NUMBER_COLUMNS).forEach { row ->
            Row {
                row.forEach { id ->
                    PreviewCheckRow(id.number.toString(), id in included, toggle(id), Modifier.width(SlotsDimens.previewNumberCellWidth))
                }
            }
        }
    }
}
