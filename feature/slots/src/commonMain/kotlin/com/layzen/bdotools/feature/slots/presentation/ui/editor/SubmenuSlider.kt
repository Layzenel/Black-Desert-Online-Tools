package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.theme.AppTheme

/** Содержимое выезжающей панели меню: слайдер с крайними значениями по бокам и текущим по центру. */
@Composable
internal fun SubmenuSlider(value: Int, range: IntRange, onChange: (Int) -> Unit) {
    ValueSlider(value, onChange, range, Modifier.fillMaxWidth().padding(AppTheme.dimens.spaceM))
}
