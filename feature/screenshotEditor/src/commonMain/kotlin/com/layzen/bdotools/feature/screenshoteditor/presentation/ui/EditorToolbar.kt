package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_redo
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_undo
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Строка над скриншотом: отмена, повтор, инструменты, поворот, отражение и сравнение с оригиналом. */
@Composable
internal fun EditorToolbar(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit) {
    ActionBar {
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
            ToolbarButton(
                painterResource(tool.icon),
                stringResource(tool.title),
                onClick = { onEvent(ScreenshotEditorEvent.SelectTool(tool)) },
                selected = tool == state.tool && !state.comparing,
            )
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
    }
}
