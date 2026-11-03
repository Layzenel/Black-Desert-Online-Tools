package com.layzen.bdotools.uikit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

/** Числовое поле: принимает только цифры, значение за пределами [range] прижимается к краю. */
@Composable
fun NumberField(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..Int.MAX_VALUE,
) {
    InputField(
        value = value.toString(),
        onValueChange = { text ->
            val digits = text.filter(Char::isDigit).take(10)
            onValueChange((digits.toLongOrNull() ?: 0L).coerceIn(range.first.toLong(), range.last.toLong()).toInt())
        },
        modifier = modifier,
        keyboardType = KeyboardType.Number,
        textAlign = TextAlign.End,
    )
}
