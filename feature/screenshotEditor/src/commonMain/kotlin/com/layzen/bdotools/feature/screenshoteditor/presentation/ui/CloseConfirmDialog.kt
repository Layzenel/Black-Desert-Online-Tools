package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_close_cancel
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_close_cancel_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_close_discard
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_close_discard_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_close_text
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_close_title
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** «Закрыть скриншот?» перед галереей, когда есть несохранённые правки: [onClose] закрывает, [onCancel] — остаться. */
@Composable
internal fun CloseConfirmDialog(onClose: () -> Unit, onCancel: () -> Unit) {
    ModalWindow(
        title = stringResource(Res.string.screenshots_close_title),
        onDismissRequest = onCancel,
        modifier = Modifier.width(ScreenshotEditorDimens.dialogWidth),
        footer = {
            ActionButton(
                ActionButtonState.Neutral(
                    stringResource(Res.string.screenshots_close_cancel),
                    onClick = onCancel,
                    tooltip = stringResource(Res.string.screenshots_close_cancel_tooltip),
                ),
            )
            ActionButton(
                ActionButtonState.Negative(
                    stringResource(Res.string.screenshots_close_discard),
                    onClick = onClose,
                    tooltip = stringResource(Res.string.screenshots_close_discard_tooltip),
                ),
            )
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL)) { WarningText(stringResource(Res.string.screenshots_close_text)) }
    }
}
