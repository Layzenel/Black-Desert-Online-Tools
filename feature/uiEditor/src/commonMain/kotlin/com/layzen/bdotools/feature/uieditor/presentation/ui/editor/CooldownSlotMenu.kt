package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLabel
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.menu_add
import com.layzen.bdotools.feature.uieditor.resources.menu_delete
import com.layzen.bdotools.feature.uieditor.resources.menu_edit
import com.layzen.bdotools.feature.uieditor.resources.menu_skill
import com.layzen.bdotools.feature.uieditor.resources.slots_label
import com.layzen.bdotools.feature.uieditor.resources.slots_label_chars
import com.layzen.bdotools.feature.uieditor.resources.slots_memo_add
import com.layzen.bdotools.feature.uieditor.resources.slots_memo_delete
import com.layzen.bdotools.feature.uieditor.resources.slots_memo_edit
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_add
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_remove
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_replace
import com.layzen.bdotools.feature.uieditor.resources.tool_chevron_right
import com.layzen.bdotools.feature.uieditor.resources.tool_label
import com.layzen.bdotools.uikit.components.ActionMenuItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Пункты меню слота перезарядки [slot]. «Заметка»: у слота с навыком справа выезжают «Добавить» или «Редактировать»
 * и «Удалить» ([MemoSubmenu]) — это надпись навыка из игры; у пустого слота клик ставит или убирает заглушку надписи,
 * справа выезжает её ширина. Ниже — «Добавить умение» или «Заменить умение» и «Убрать умение».
 * [trigger] — модификатор пункта, при наведении на который выезжает панель; [run] — действие, закрывающее меню.
 */
@Composable
internal fun CooldownSlotMenu(
    state: SlotsState,
    slot: SlotId,
    trigger: @Composable (ContextSubmenu) -> Modifier,
    run: (SlotsEvent) -> () -> Unit,
    onEvent: (SlotsEvent) -> Unit,
) {
    val skill = state.layout.skillAt(slot)
    val chevron = painterResource(Res.drawable.tool_chevron_right)
    if (skill != null) {
        ActionMenuItem(
            stringResource(Res.string.slots_label),
            onClick = {},
            modifier = trigger(ContextSubmenu.Memo),
            icon = painterResource(Res.drawable.tool_label),
            trailingText = state.layout.memoAt(slot),
            trailingIcon = chevron,
        )
    } else {
        val labelChars = state.layout.labels[slot]
        ActionMenuItem(
            stringResource(Res.string.slots_label),
            onClick = { onEvent(SlotsEvent.SetLabel(slot, if (labelChars == null) SlotLabel.DEFAULT_CHARS else null)) },
            modifier = if (labelChars != null) trigger(ContextSubmenu.LabelWidth) else Modifier,
            icon = painterResource(Res.drawable.tool_label),
            trailingText = labelChars?.let { stringResource(Res.string.slots_label_chars, it) },
            trailingIcon = if (labelChars != null) chevron else null,
            toggled = labelChars != null,
        )
    }
    ActionMenuItem(
        stringResource(if (skill == null) Res.string.slots_skill_add else Res.string.slots_skill_replace),
        run(SlotsEvent.OpenSkillPicker(slot)),
        // Умения — только класса персонажа в редакторе: без справочника или класса выбирать не из чего
        enabled = state.skills.catalog != null && state.skillsClass != null,
        icon = painterResource(Res.drawable.menu_skill),
    )
    if (skill != null) {
        ActionMenuItem(stringResource(Res.string.slots_skill_remove), run(SlotsEvent.RemoveSkill(slot)), icon = painterResource(Res.drawable.menu_delete))
    }
}

/** Панель «Заметки» слота с навыком: добавить или изменить надпись в окне и удалить её. */
@Composable
internal fun MemoSubmenu(state: SlotsState, slot: SlotId, run: (SlotsEvent) -> () -> Unit) {
    val hasMemo = state.layout.memoAt(slot) != null
    ActionMenuItem(
        stringResource(if (hasMemo) Res.string.slots_memo_edit else Res.string.slots_memo_add),
        run(SlotsEvent.OpenMemo(slot)),
        icon = painterResource(if (hasMemo) Res.drawable.menu_edit else Res.drawable.menu_add),
    )
    ActionMenuItem(
        stringResource(Res.string.slots_memo_delete),
        run(SlotsEvent.DeleteMemo(slot)),
        enabled = hasMemo,
        icon = painterResource(Res.drawable.menu_delete),
    )
}
