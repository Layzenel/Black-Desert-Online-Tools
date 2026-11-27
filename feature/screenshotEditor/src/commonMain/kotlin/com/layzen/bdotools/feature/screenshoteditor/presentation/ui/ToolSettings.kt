package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.CropAspect
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorColors
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.MAX_THICKNESS
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_aspect
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_color
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_color_default
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_select_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_text_placeholder
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_text_size
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_thickness
import com.layzen.bdotools.uikit.components.ColorPalette
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ValueSlider
import org.jetbrains.compose.resources.stringResource

/** Настройки выбранного инструмента в меню слева: как им пользоваться и его параметры. */
@Composable
internal fun ToolSettings(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit) {
    SectionHeader(stringResource(state.tool.title), description = stringResource(state.tool.hint))
    if (state.tool == EditorTool.Crop) CropSettings(state, onEvent)
    if (state.tool == EditorTool.Text) {
        InputField(
            state.text,
            { onEvent(ScreenshotEditorEvent.Text(it)) },
            Modifier.fillMaxWidth(),
            placeholder = stringResource(Res.string.screenshots_text_placeholder),
        )
    }
    if (state.tool.usesColor) MarkSettings(state, onEvent)
    DescriptionText(stringResource(Res.string.screenshots_select_hint))
}

@Composable
private fun CropSettings(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit) {
    SectionHeader(stringResource(Res.string.screenshots_aspect))
    DropdownSelect(
        state.aspect,
        CropAspect.entries,
        { onEvent(ScreenshotEditorEvent.Aspect(it)) },
        { stringResource(it.title) },
        Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MarkSettings(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit) {
    SectionHeader(stringResource(Res.string.screenshots_color))
    ColorPalette(
        colors = EditorColors.choices.map(::Color),
        selected = state.colorIndex,
        onSelect = { onEvent(ScreenshotEditorEvent.SelectColor(it)) },
        defaultColor = Color(EditorColors.DEFAULT),
        defaultLabel = stringResource(Res.string.screenshots_color_default),
    )
    val sizeTitle = if (state.tool == EditorTool.Text) Res.string.screenshots_text_size else Res.string.screenshots_thickness
    SectionHeader(stringResource(sizeTitle))
    ValueSlider(state.thickness, { onEvent(ScreenshotEditorEvent.Thickness(it)) }, 1..MAX_THICKNESS, Modifier.fillMaxWidth())
}
