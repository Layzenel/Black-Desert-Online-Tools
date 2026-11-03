package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Модальное окно поверх страницы: [GameWindow] с крестиком в шапке.
 * Закрывается крестиком, клавишей Esc и кликом мимо окна, если [dismissible].
 */
@Composable
fun ModalWindow(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissible: Boolean = true,
    footer: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = { if (dismissible) onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible,
            usePlatformDefaultWidth = false,
        ),
    ) {
        GameWindow(
            title = title,
            modifier = modifier.padding(AppTheme.dimens.spaceXl),
            actions = { if (dismissible) CloseButton(onClick = onDismissRequest) },
            footer = footer,
            content = content,
        )
    }
}
