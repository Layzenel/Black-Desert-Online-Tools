package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextMeasurer
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_copy
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_gallery
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_portraits
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_save
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_copy
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_copy_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_portraits
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_portraits_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_save
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Низ меню: к галерее (с вопросом, если есть несохранённые правки), скопировать, отдать фоном портретов и последним —
 * главное действие «Сохранить копию».
 */
@Composable
internal fun ColumnScope.FileActions(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit, measurer: TextMeasurer) {
    var confirmClose by remember { mutableStateOf(false) }
    FileButton(painterResource(Res.drawable.menu_gallery), stringResource(Res.string.screenshots_gallery), stringResource(Res.string.screenshots_gallery_tooltip)) {
        if (state.dirty) confirmClose = true else onEvent(ScreenshotEditorEvent.Close)
    }
    FileButton(painterResource(Res.drawable.menu_copy), stringResource(Res.string.screenshots_copy), stringResource(Res.string.screenshots_copy_tooltip)) {
        onEvent(ScreenshotEditorEvent.Copy(measurer))
    }
    FileButton(painterResource(Res.drawable.menu_portraits), stringResource(Res.string.screenshots_portraits), stringResource(Res.string.screenshots_portraits_tooltip)) {
        onEvent(ScreenshotEditorEvent.ApplyToPortraits(measurer))
    }
    ActionButton(
        ActionButtonState.Positive(
            stringResource(Res.string.screenshots_save),
            icon = painterResource(Res.drawable.menu_save),
            onClick = { onEvent(ScreenshotEditorEvent.Save(measurer)) },
            tooltip = stringResource(saveTooltip),
        ),
        Modifier.fillMaxWidth(),
    )
    if (confirmClose) {
        CloseConfirmDialog(
            onClose = {
                confirmClose = false
                onEvent(ScreenshotEditorEvent.Close)
            },
            onCancel = { confirmClose = false },
        )
    }
}

@Composable
private fun FileButton(icon: Painter, text: String, tooltip: String, onClick: () -> Unit) {
    ActionButton(ActionButtonState.Neutral(text, icon = icon, onClick = onClick, tooltip = tooltip), Modifier.fillMaxWidth())
}
