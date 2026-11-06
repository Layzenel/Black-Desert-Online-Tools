package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.domain.PortraitTexture
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Сетка как в окне выбора персонажа: не больше [columns] ячеек в ряду, все одной ширины,
 * неполный ряд прижат влево. Общая для страницы портретов и окна резервных копий.
 */
@Composable
internal fun <T> PortraitGridLayout(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = PortraitTexture.MAX_COLUMNS,
    cell: @Composable (T) -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        items.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                row.forEach { item -> Box(Modifier.weight(1f)) { cell(item) } }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
