package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Содержимое выезжающей панели меню: слайдер с крайними значениями по бокам и текущим по центру.
 * [onChangeFinished] — конец жеста (отпустили или кликнули), чтобы правка по ходу жеста легла в историю одним шагом.
 */
@Composable
internal fun SubmenuSlider(value: Int, range: IntRange, onChangeFinished: () -> Unit = {}, onChange: (Int) -> Unit) {
    ValueSlider(value, onChange, range, Modifier.fillMaxWidth().padding(AppTheme.dimens.spaceM), onValueChangeFinished = onChangeFinished)
}
