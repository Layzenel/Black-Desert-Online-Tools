package com.layzen.bdotools.language

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.layzen.bdotools.core.language.AppLanguage
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.language_switcher_tooltip
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.OptionsMenu
import org.jetbrains.compose.resources.stringResource

/**
 * Выбор языка интерфейса в верхней навигации: кнопка с кодом текущего языка (`RU`, `EN`) и под ней список
 * всех языков приложения под их собственными названиями.
 */
@Composable
internal fun LanguageSwitcher(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        ActionButton(ActionButtonState.Neutral(selected.tag.uppercase(), onClick = { open = true }, tooltip = stringResource(Res.string.language_switcher_tooltip)))
        OptionsMenu(
            expanded = open,
            onDismissRequest = { open = false },
            options = AppLanguage.entries,
            selected = selected,
            onSelect = {
                open = false
                onSelect(it)
            },
            label = { it.nativeName },
        )
    }
}
