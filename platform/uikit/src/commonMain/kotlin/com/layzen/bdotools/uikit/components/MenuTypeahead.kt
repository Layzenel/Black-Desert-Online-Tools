package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalFocusManager
import com.layzen.bdotools.uikit.theme.AppTheme

/** Первый печатный знак: управляющие (Tab, Enter) в строку поиска не попадают. */
private const val FIRST_PRINTABLE = 32

/**
 * Ловит набор с клавиатуры, пока список открыт ([active]): буквы дописываются в [query], Backspace стирает,
 * Esc закрывает список ([onEscape]). Сам список фокус не берёт — фокусируемое всплывающее окно в веб-сборке
 * после закрытия съедает следующий клик, — поэтому клавиши ловит этот невидимый элемент на странице.
 */
@Composable
internal fun TypeToSearch(active: Boolean, query: String, onQueryChange: (String) -> Unit, onEscape: () -> Unit) {
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(active) { if (active) focus.requestFocus() else focusManager.clearFocus() }
    Box(
        Modifier
            .focusRequester(focus)
            .onPreviewKeyEvent { event ->
                if (!active) return@onPreviewKeyEvent false
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent event.utf16CodePoint >= FIRST_PRINTABLE
                when {
                    event.key == Key.Escape -> onEscape()
                    event.key == Key.Backspace -> onQueryChange(query.dropLast(1))
                    event.isCtrlPressed || event.isMetaPressed || event.isAltPressed -> return@onPreviewKeyEvent false
                    event.utf16CodePoint >= FIRST_PRINTABLE -> onQueryChange(query + event.utf16CodePoint.toChar())
                    else -> return@onPreviewKeyEvent false
                }
                true
            }
            .focusable(),
    )
}

/**
 * Строка поиска наверху списка: показывает набранное в [TypeToSearch] (или подсказку [placeholder]),
 * крестик очищает. Печатать можно сразу, не кликая в неё.
 */
@Composable
internal fun MenuSearchLine(query: String, placeholder: String, modifier: Modifier, onClear: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Box(modifier.menuPanel().padding(dimens.spaceXs)) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = dimens.inputHeight)
                .background(colors.backgroundInput)
                .border(dimens.borderWidth, colors.border)
                .padding(horizontal = dimens.spaceS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
        ) {
            Text(
                query.ifEmpty { placeholder },
                Modifier.weight(1f),
                style = AppTheme.typography.bodyBold,
                color = if (query.isEmpty()) colors.textMuted else colors.textPrimary,
                maxLines = 1,
            )
            if (query.isNotEmpty()) CloseButton(onClick = onClear)
        }
    }
}
