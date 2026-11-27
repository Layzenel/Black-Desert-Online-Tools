package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextMeasurer
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_copy
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_gallery
import com.layzen.bdotools.feature.screenshoteditor.resources.menu_save
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_copy
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_copy_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_gallery_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_save
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Низ меню: к галерее, скопировать и последним — главное действие «Сохранить копию». */
@Composable
internal fun ColumnScope.FileActions(onEvent: (ScreenshotEditorEvent) -> Unit, measurer: TextMeasurer) {
    ActionButton(
        ActionButtonState.Neutral(
            stringResource(Res.string.screenshots_gallery),
            icon = painterResource(Res.drawable.menu_gallery),
            onClick = { onEvent(ScreenshotEditorEvent.Close) },
            tooltip = stringResource(Res.string.screenshots_gallery_tooltip),
        ),
        Modifier.fillMaxWidth(),
    )
    ActionButton(
        ActionButtonState.Neutral(
            stringResource(Res.string.screenshots_copy),
            icon = painterResource(Res.drawable.menu_copy),
            onClick = { onEvent(ScreenshotEditorEvent.Copy(measurer)) },
            tooltip = stringResource(Res.string.screenshots_copy_tooltip),
        ),
        Modifier.fillMaxWidth(),
    )
    ActionButton(
        ActionButtonState.Positive(
            stringResource(Res.string.screenshots_save),
            icon = painterResource(Res.drawable.menu_save),
            onClick = { onEvent(ScreenshotEditorEvent.Save(measurer)) },
            tooltip = stringResource(saveTooltip),
        ),
        Modifier.fillMaxWidth(),
    )
}
