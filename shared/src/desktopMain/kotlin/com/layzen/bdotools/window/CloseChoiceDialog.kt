package com.layzen.bdotools.window

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.window_close_exit
import com.layzen.bdotools.resources.window_close_exit_tooltip
import com.layzen.bdotools.resources.window_close_text
import com.layzen.bdotools.resources.window_close_title
import com.layzen.bdotools.resources.window_close_tray
import com.layzen.bdotools.resources.window_close_tray_tooltip
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Что сделать по крестику окна: свернуть в трей ([onTray]) или закрыть программу ([onExit]). */
@Composable
internal fun CloseChoiceDialog(onTray: () -> Unit, onExit: () -> Unit, onDismiss: () -> Unit) {
    ModalWindow(
        title = stringResource(Res.string.window_close_title),
        onDismissRequest = onDismiss,
        modifier = Modifier.width(WindowDimens.closeDialogWidth),
        footer = {
            ActionButton(ActionButtonState.Neutral(stringResource(Res.string.window_close_exit), onClick = onExit, tooltip = stringResource(Res.string.window_close_exit_tooltip)))
            ActionButton(ActionButtonState.Neutral(stringResource(Res.string.window_close_tray), onClick = onTray, tooltip = stringResource(Res.string.window_close_tray_tooltip)))
        },
    ) {
        Text(
            stringResource(Res.string.window_close_text),
            Modifier.padding(AppTheme.dimens.spaceL),
            style = AppTheme.typography.body,
            color = AppTheme.colors.textBeige,
        )
    }
}
