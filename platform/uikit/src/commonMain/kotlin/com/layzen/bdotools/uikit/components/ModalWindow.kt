package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Модальное окно поверх страницы: [GameWindow] с крестиком в шапке.
 * Закрывается крестиком, клавишей Esc и кликом мимо окна, если [dismissible].
 * [aside] — панель слева от окна вровень с его верхним краем (описание элемента, на который навели):
 * окно из-за неё не сдвигается.
 */
@Composable
fun ModalWindow(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissible: Boolean = true,
    footer: (@Composable RowScope.() -> Unit)? = null,
    aside: (@Composable () -> Unit)? = null,
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
        val margin = AppTheme.dimens.spaceXl
        val gap = AppTheme.dimens.spaceS
        Layout(
            content = {
                GameWindow(
                    title = title,
                    modifier = modifier.padding(margin),
                    actions = { if (dismissible) CloseButton(onClick = onDismissRequest) },
                    footer = footer,
                    content = content,
                )
                Box { aside?.invoke() }
            },
        ) { measurables, constraints ->
            val window = measurables[0].measure(constraints)
            val side = measurables[1].measure(Constraints())
            // Размер — только окна: панель рисуется за его краем и не двигает окно по центру
            layout(window.width, window.height) {
                window.place(0, 0)
                side.place(margin.roundToPx() - gap.roundToPx() - side.width, margin.roundToPx())
            }
        }
    }
}
