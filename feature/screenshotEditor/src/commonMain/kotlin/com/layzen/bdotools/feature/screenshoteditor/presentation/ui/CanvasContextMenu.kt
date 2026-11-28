package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_cancel
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_copy
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_redo
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_undo
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_delete
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_mark_copy
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_mark_duplicate
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_mark_edit_text
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_mark_paste
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_redo
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_undo
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_text
import com.layzen.bdotools.uikit.components.ActionMenuDivider
import com.layzen.bdotools.uikit.components.ActionMenuItem
import com.layzen.bdotools.uikit.components.ContextMenu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Меню правого клика по холсту: над пометкой — что с ней сделать (изменить текст, копировать, дублировать, удалить),
 * потом вставка, отмена и повтор, затем инструменты — выбранный подсвечен золотом. У стрелки, рамки и текста при
 * наведении раскрывается панель с их видами.
 */
@Composable
internal fun CanvasContextMenu(state: ScreenshotEditorState, onDismiss: () -> Unit, onEvent: (ScreenshotEditorEvent) -> Unit) {
    fun run(event: ScreenshotEditorEvent) = { onDismiss(); onEvent(event) }
    ContextMenu<EditorTool>(
        expanded = true,
        onDismissRequest = onDismiss,
        submenu = { tool ->
            val current = tool.styleIn(state)
            tool.styles.forEach { style ->
                ActionMenuItem(
                    stringResource(style.title),
                    run(ScreenshotEditorEvent.SelectStyle(style)),
                    icon = painterResource(style.icon),
                    toggled = true.takeIf { tool == state.tool && style == current },
                )
            }
        },
    ) { submenuTrigger ->
        val selected = state.selected
        if (selected != null) {
            if (state.history.edits.getOrNull(selected) is Edit.Label) {
                ActionMenuItem(stringResource(Res.string.screenshots_mark_edit_text), run(ScreenshotEditorEvent.EditLabel(selected)), icon = painterResource(Res.drawable.tool_text))
            }
            ActionMenuItem(stringResource(Res.string.screenshots_mark_copy), run(ScreenshotEditorEvent.CopyMark), icon = painterResource(Res.drawable.menu_copy))
            ActionMenuItem(stringResource(Res.string.screenshots_mark_duplicate), run(ScreenshotEditorEvent.DuplicateMark), icon = painterResource(Res.drawable.menu_copy))
            ActionMenuItem(stringResource(Res.string.screenshots_delete), run(ScreenshotEditorEvent.DeleteSelected), icon = painterResource(Res.drawable.menu_cancel))
            ActionMenuDivider()
        }
        if (state.copied != null) {
            ActionMenuItem(stringResource(Res.string.screenshots_mark_paste), run(ScreenshotEditorEvent.PasteMark), icon = painterResource(Res.drawable.menu_copy))
        }
        ActionMenuItem(
            stringResource(Res.string.screenshots_undo),
            run(ScreenshotEditorEvent.Undo),
            enabled = state.history.canUndo,
            icon = painterResource(Res.drawable.menu_undo),
        )
        ActionMenuItem(
            stringResource(Res.string.screenshots_redo),
            run(ScreenshotEditorEvent.Redo),
            enabled = state.history.canRedo,
            icon = painterResource(Res.drawable.menu_redo),
        )
        ActionMenuDivider()
        EditorTool.entries.forEach { tool ->
            ActionMenuItem(
                stringResource(tool.title),
                run(ScreenshotEditorEvent.SelectTool(tool)),
                modifier = if (tool.styles.isEmpty()) Modifier else submenuTrigger(tool),
                icon = painterResource(tool.styleIn(state)?.icon ?: tool.icon),
                toggled = true.takeIf { tool == state.tool },
            )
        }
    }
}
