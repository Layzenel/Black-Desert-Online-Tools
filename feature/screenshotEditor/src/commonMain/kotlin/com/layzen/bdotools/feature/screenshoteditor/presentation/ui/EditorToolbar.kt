package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.composable.canvas.CanvasViewport
import com.layzen.bdotools.composable.canvas.ZoomSlider
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_discard
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_redo
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_undo
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_discard_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_redo
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_undo
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_compare
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_flip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_rotate
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_compare
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_flip
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_rotate
import com.layzen.bdotools.uikit.components.ActionBar
import com.layzen.bdotools.uikit.components.ToolbarButton
import com.layzen.bdotools.uikit.components.ToolbarDivider
import com.layzen.bdotools.uikit.components.ToolbarDropdownButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Строка над скриншотом: сброс, отмена, повтор, инструменты, поворот, отражение, сравнение с оригиналом и масштаб [viewport]. */
@Composable
internal fun EditorToolbar(state: ScreenshotEditorState, viewport: CanvasViewport, onEvent: (ScreenshotEditorEvent) -> Unit) {
    ActionBar {
        ToolbarButton(
            painterResource(Res.drawable.menu_discard),
            stringResource(Res.string.screenshots_discard_tooltip),
            onClick = { onEvent(ScreenshotEditorEvent.Discard) },
            enabled = state.history.edits.isNotEmpty(),
        )
        ToolbarButton(
            painterResource(Res.drawable.menu_undo),
            stringResource(Res.string.screenshots_undo),
            onClick = { onEvent(ScreenshotEditorEvent.Undo) },
            enabled = state.history.canUndo,
        )
        ToolbarButton(
            painterResource(Res.drawable.menu_redo),
            stringResource(Res.string.screenshots_redo),
            onClick = { onEvent(ScreenshotEditorEvent.Redo) },
            enabled = state.history.canRedo,
        )
        ToolbarDivider()
        EditorTool.entries.forEach { tool ->
            val selected = tool == state.tool && !state.comparing
            val select = { onEvent(ScreenshotEditorEvent.SelectTool(tool)) }
            if (tool.styles.isEmpty()) {
                ToolbarButton(painterResource(tool.icon), stringResource(tool.title), onClick = select, selected = selected)
            } else {
                // значок инструмента — его текущий вид, а при наведении — список всех видов
                val current = tool.styleIn(state)
                ToolbarDropdownButton(painterResource(current?.icon ?: tool.icon), stringResource(tool.title), onClick = select, selected = selected) {
                    tool.styles.forEach { style ->
                        ToolbarButton(
                            painterResource(style.icon),
                            stringResource(style.title),
                            onClick = { onEvent(ScreenshotEditorEvent.SelectStyle(style)) },
                            selected = selected && style == current,
                        )
                    }
                }
            }
        }
        ToolbarDivider()
        ToolbarButton(painterResource(Res.drawable.tool_rotate), stringResource(Res.string.screenshots_rotate), { onEvent(ScreenshotEditorEvent.Rotate) })
        ToolbarButton(painterResource(Res.drawable.tool_flip), stringResource(Res.string.screenshots_flip), { onEvent(ScreenshotEditorEvent.Flip) })
        ToolbarButton(
            painterResource(Res.drawable.tool_compare),
            stringResource(Res.string.screenshots_compare),
            onClick = { onEvent(ScreenshotEditorEvent.ToggleCompare) },
            selected = state.comparing,
        )
        ToolbarDivider()
        ZoomSlider(viewport)
    }
}
