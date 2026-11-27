package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.Adjustments
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.FilterPreset
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_adjust
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_adjust_description
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_brightness
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_contrast
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_exposure
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_filter_field
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_saturation
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_temperature
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_vignette
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ValueSlider
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val SIGNED = -100..100
private val POSITIVE = 0..100

/** Ползунок ретуши: подпись, допустимые значения, чтение и замена своего значения. */
private class AdjustSlider(
    val title: StringResource,
    val range: IntRange,
    val value: (Adjustments) -> Int,
    val with: (Adjustments, Int) -> Adjustments,
)

private val sliders = listOf(
    AdjustSlider(Res.string.screenshots_exposure, SIGNED, { it.exposure }, { a, v -> a.copy(exposure = v) }),
    AdjustSlider(Res.string.screenshots_brightness, SIGNED, { it.brightness }, { a, v -> a.copy(brightness = v) }),
    AdjustSlider(Res.string.screenshots_contrast, SIGNED, { it.contrast }, { a, v -> a.copy(contrast = v) }),
    AdjustSlider(Res.string.screenshots_saturation, SIGNED, { it.saturation }, { a, v -> a.copy(saturation = v) }),
    AdjustSlider(Res.string.screenshots_temperature, SIGNED, { it.temperature }, { a, v -> a.copy(temperature = v) }),
    AdjustSlider(Res.string.screenshots_tint, SIGNED, { it.tint }, { a, v -> a.copy(tint = v) }),
    AdjustSlider(Res.string.screenshots_vignette, POSITIVE, { it.vignette }, { a, v -> a.copy(vignette = v) }),
)

/**
 * Ретушь всей картинки: готовый фильтр и ползунки. Пока ползунок тянут, правка видна сразу, а в историю
 * попадает одним шагом, когда его отпустили.
 */
@Composable
internal fun AdjustSettings(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit) {
    val adjustments = state.shownAdjustments
    SectionHeader(stringResource(Res.string.screenshots_adjust), description = stringResource(Res.string.screenshots_adjust_description))
    DropdownSelect(
        FilterPreset.entries.firstOrNull { it.adjustments == adjustments },
        FilterPreset.entries,
        { onEvent(ScreenshotEditorEvent.Preset(it)) },
        { stringResource(it.title) },
        Modifier.fillMaxWidth(),
        fieldText = stringResource(Res.string.screenshots_filter_field).takeIf { FilterPreset.entries.none { it.adjustments == adjustments } },
    )
    sliders.forEach { slider ->
        DescriptionText(stringResource(slider.title))
        ValueSlider(
            slider.value(adjustments),
            { onEvent(ScreenshotEditorEvent.Adjust(slider.with(state.shownAdjustments, it))) },
            slider.range,
            Modifier.fillMaxWidth(),
            onValueChangeFinished = { onEvent(ScreenshotEditorEvent.AdjustFinished) },
        )
    }
}
