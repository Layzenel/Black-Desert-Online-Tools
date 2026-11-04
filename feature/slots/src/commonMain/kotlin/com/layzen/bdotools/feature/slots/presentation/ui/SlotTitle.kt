package com.layzen.bdotools.feature.slots.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.domain.InterfaceElement
import com.layzen.bdotools.feature.slots.domain.SlotGroup
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_element_black_spirit_rage
import com.layzen.bdotools.feature.slots.resources.slots_element_buffs
import com.layzen.bdotools.feature.slots.resources.slots_element_health
import com.layzen.bdotools.feature.slots.resources.slots_element_pvp
import com.layzen.bdotools.feature.slots.resources.slots_element_rare_item_history
import com.layzen.bdotools.feature.slots.resources.slots_element_skill_history
import com.layzen.bdotools.feature.slots.resources.slots_element_stamina
import com.layzen.bdotools.feature.slots.resources.slots_group_class_resource
import com.layzen.bdotools.feature.slots.resources.slots_group_cooldown
import com.layzen.bdotools.feature.slots.resources.slots_group_hotbar
import com.layzen.bdotools.feature.slots.resources.slots_group_interface
import com.layzen.bdotools.feature.slots.resources.slots_slot_class_resource
import com.layzen.bdotools.feature.slots.resources.slots_slot_cooldown
import com.layzen.bdotools.feature.slots.resources.slots_slot_hotbar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Название элемента, как в режиме правки интерфейса игры: на холсте и в панели выбора. */
@Composable
internal fun slotTitle(id: SlotId): String = when (id.group) {
    SlotGroup.Cooldown -> stringResource(Res.string.slots_slot_cooldown, id.number)
    SlotGroup.Hotbar -> stringResource(Res.string.slots_slot_hotbar, id.number)
    SlotGroup.ClassResource -> stringResource(Res.string.slots_slot_class_resource)
    SlotGroup.Interface -> stringResource(requireNotNull(id.interfaceElement).title)
}

/** Название группы целиком: в списке элементов превью. */
@Composable
internal fun groupTitle(group: SlotGroup): String = stringResource(
    when (group) {
        SlotGroup.Cooldown -> Res.string.slots_group_cooldown
        SlotGroup.Hotbar -> Res.string.slots_group_hotbar
        SlotGroup.ClassResource -> Res.string.slots_group_class_resource
        SlotGroup.Interface -> Res.string.slots_group_interface
    },
)

private val InterfaceElement.title: StringResource
    get() = when (this) {
        InterfaceElement.Health -> Res.string.slots_element_health
        InterfaceElement.Stamina -> Res.string.slots_element_stamina
        InterfaceElement.Pvp -> Res.string.slots_element_pvp
        InterfaceElement.BlackSpiritRage -> Res.string.slots_element_black_spirit_rage
        InterfaceElement.Buffs -> Res.string.slots_element_buffs
        InterfaceElement.SkillHistory -> Res.string.slots_element_skill_history
        InterfaceElement.RareItemHistory -> Res.string.slots_element_rare_item_history
    }
