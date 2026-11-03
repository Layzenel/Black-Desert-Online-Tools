package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Поле ввода (поиск, числа в калькуляторах). Тёмная подложка, бежевая подсказка.
 * [leading] и [trailing] — иконки или кнопки слева и справа от текста.
 */
@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.Start,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = AppTheme.typography.bodyBold.copy(color = colors.textPrimary, textAlign = textAlign),
        cursorBrush = SolidColor(colors.accentGold),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        decorationBox = { inner ->
            Row(
                Modifier
                    .heightIn(min = dimens.inputHeight)
                    .background(colors.backgroundInput)
                    .border(dimens.borderWidth, colors.border)
                    .padding(horizontal = dimens.spaceS),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
            ) {
                if (leading != null) leading()
                // Поле тянется на всю ширину, иначе курсор пустого поля прилипает к левому краю при любом textAlign
                Box(Modifier.weight(1f), propagateMinConstraints = true) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            placeholder,
                            style = AppTheme.typography.bodyBold,
                            color = colors.textMuted,
                            textAlign = textAlign,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    inner()
                }
                if (trailing != null) trailing()
            }
        },
    )
}
