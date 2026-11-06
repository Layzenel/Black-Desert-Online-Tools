package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.components.NumberField
import com.layzen.bdotools.uikit.theme.AppTheme

/** Насколько далеко от центра можно ввести координату руками, UI-px. */
internal val COORDINATE_RANGE = -5000..5000

/** Подпись слева и поле точного ввода числа справа: координата или размер выбранного. */
@Composable
internal fun CoordinateRow(label: String, value: Int, range: IntRange = COORDINATE_RANGE, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        NumberField(value, onValueChange, Modifier.width(SlotsDimens.coordinateFieldWidth), range = range)
    }
}
