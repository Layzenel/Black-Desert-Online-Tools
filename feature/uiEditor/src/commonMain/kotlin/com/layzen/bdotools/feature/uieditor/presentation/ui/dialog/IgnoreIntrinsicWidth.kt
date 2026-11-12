package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

/**
 * Элемент не влияет на ширину родителя с `width(IntrinsicSize.Max)`: длинное пояснение переносится по ширине,
 * которую задали остальные элементы, а не растягивает окно в одну строку.
 */
internal fun Modifier.ignoreIntrinsicWidth(): Modifier = this then IgnoreIntrinsicWidth

private object IgnoreIntrinsicWidth : LayoutModifier {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int = 0

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int = 0
}
