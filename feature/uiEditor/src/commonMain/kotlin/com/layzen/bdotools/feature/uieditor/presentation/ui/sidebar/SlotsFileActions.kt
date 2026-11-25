package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.EditorStatus
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.menu_apply
import com.layzen.bdotools.feature.uieditor.resources.menu_export
import com.layzen.bdotools.feature.uieditor.resources.menu_import
import com.layzen.bdotools.feature.uieditor.resources.menu_reload
import com.layzen.bdotools.feature.uieditor.resources.menu_save
import com.layzen.bdotools.feature.uieditor.resources.menu_undo_apply
import com.layzen.bdotools.feature.uieditor.resources.slots_apply
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_count
import com.layzen.bdotools.feature.uieditor.resources.slots_apply_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_export
import com.layzen.bdotools.feature.uieditor.resources.slots_export_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_import
import com.layzen.bdotools.feature.uieditor.resources.slots_import_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_reload
import com.layzen.bdotools.feature.uieditor.resources.slots_reload_hint
import com.layzen.bdotools.feature.uieditor.resources.slots_save_as
import com.layzen.bdotools.feature.uieditor.resources.slots_save_as_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_undo_apply
import com.layzen.bdotools.feature.uieditor.resources.slots_undo_apply_hint
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Низ меню слева, всегда на виду: предупреждение об элементах за краем экрана, импорт и экспорт раскладки,
 * сохранение пресета, перечитать файл игры, откат последнего применения (пока он возможен) и последним —
 * главное действие «Применить в игру» со счётчиком изменений. У каждого пункта подсказка при наведении.
 * Что игра должна быть закрыта, говорит окно «Применить в игру».
 */
@Composable
internal fun SlotsFileActions(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val wide = Modifier.fillMaxWidth()
    EditorStatus(state)
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        HintedAction(Res.string.slots_import, Res.string.slots_import_tooltip, Res.drawable.menu_import, Modifier.weight(1f)) {
            onEvent(SlotsEvent.OpenImport)
        }
        HintedAction(Res.string.slots_export, Res.string.slots_export_tooltip, Res.drawable.menu_export, Modifier.weight(1f)) {
            onEvent(SlotsEvent.Export)
        }
    }
    HintedAction(Res.string.slots_save_as, Res.string.slots_save_as_tooltip, Res.drawable.menu_save, wide) { onEvent(SlotsEvent.OpenSaveAs) }
    HoverTooltip(stringResource(Res.string.slots_reload_hint), wide) {
        ActionButton(
            ActionButtonState.Neutral(
                stringResource(Res.string.slots_reload),
                icon = painterResource(Res.drawable.menu_reload),
                onClick = { onEvent(SlotsEvent.Reload) },
            ),
            wide,
        )
    }
    if (state.canUndoApply) {
        HoverTooltip(stringResource(Res.string.slots_undo_apply_hint), wide) {
            ActionButton(
                ActionButtonState.Neutral(
                    stringResource(Res.string.slots_undo_apply),
                    icon = painterResource(Res.drawable.menu_undo_apply),
                    enabled = !state.busy,
                    onClick = { onEvent(SlotsEvent.OpenUndoApply) },
                ),
                wide,
            )
        }
    }
    val count = state.changeCount
    ActionButton(
        ActionButtonState.Positive(
            text = if (count > 0) stringResource(Res.string.slots_apply_count, count) else stringResource(Res.string.slots_apply),
            icon = painterResource(Res.drawable.menu_apply),
            enabled = state.hasChanges && !state.busy,
            onClick = { onEvent(SlotsEvent.OpenApply) },
            tooltip = stringResource(Res.string.slots_apply_tooltip),
        ),
        modifier = wide,
    )
}

/** Пункт меню с подсказкой [hint] при наведении: короткая подпись [text], подробности — в подсказке. */
@Composable
private fun HintedAction(text: StringResource, hint: StringResource, icon: DrawableResource, modifier: Modifier, onClick: () -> Unit) {
    // TooltipBox не передаёт weight в Row, поэтому модификатор — у обёртки, а подсказка внутри неё
    Box(modifier) {
        HoverTooltip(stringResource(hint), Modifier.fillMaxWidth()) {
            ActionButton(ActionButtonState.Neutral(stringResource(text), icon = painterResource(icon), onClick = onClick), Modifier.fillMaxWidth())
        }
    }
}
