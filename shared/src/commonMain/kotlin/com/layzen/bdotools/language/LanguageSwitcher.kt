package com.layzen.bdotools.language

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.layzen.bdotools.core.language.AppLanguage
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.OptionsMenu

/**
 * Выбор языка интерфейса в верхней навигации: кнопка с кодом текущего языка (`RU`, `EN`) и под ней список
 * всех языков приложения под их собственными названиями.
 */
@Composable
internal fun LanguageSwitcher(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        ActionButton(selected.tag.uppercase(), onClick = { open = true })
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
