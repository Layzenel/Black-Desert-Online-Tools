package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLabel
import com.layzen.bdotools.feature.uieditor.domain.skill.commandActions
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.skill.skillPartsText
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_memo_from_skill
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_names_language
import com.layzen.bdotools.feature.uieditor.resources.tool_label
import com.layzen.bdotools.uikit.components.ActionMenuDivider
import com.layzen.bdotools.uikit.components.ActionMenuItem
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Под разделителем панели «Заметка» слота [slot] с навыком [skillNo] — подпись «Заполнить из описания умения:»
 * и первая связка клавиш из блока «Команда» тултипа ([commandActions]): зелёный текст первой строки, где он есть,
 * с клавишами персонажа, не длиннее лимита заметки игры. Клик ставит связку в заметку. Пока описание скачивается,
 * видна только подпись; без связок блока нет.
 */
@Composable
internal fun MemoFromSkillItem(state: SlotsState, slot: SlotId, skillNo: Int, run: (SlotsEvent) -> () -> Unit, onEvent: (SlotsEvent) -> Unit) {
    LaunchedEffect(skillNo) { onEvent(SlotsEvent.RequestSkillDetails(skillNo)) }
    if (skillNo in state.skills.detailsFailed) return
    val text = state.skills.details[skillNo]?.text(stringResource(Res.string.slots_skill_names_language))
    val action = text?.let { skillText ->
        commandActions(skillText.command)
            .map { skillPartsText(it, state.skills.keyBindings).trim().take(SlotLabel.MAX_CHARS).trim() }
            .firstOrNull { it.isNotEmpty() }
    }
    if (text != null && action == null) return
    ActionMenuDivider()
    DescriptionText(stringResource(Res.string.slots_memo_from_skill), Modifier.padding(start = AppTheme.dimens.spaceM, end = AppTheme.dimens.spaceM, top = AppTheme.dimens.spaceS))
    val icon = painterResource(Res.drawable.tool_label)
    if (action != null) ActionMenuItem(action, run(SlotsEvent.SetMemo(slot, action)), icon = icon)
}
