package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.CropAspect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.HideMode
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.MAX_HIDE_STRENGTH
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.color
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorColors
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.MAX_THICKNESS
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_aspect
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_color
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_color_default
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_hide_strength
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_no_tool
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_no_tool_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_picker
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_select_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_text_size
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_thickness
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_picker
import com.layzen.bdotools.uikit.components.ColorPalette
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ToolbarButton
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Настройки выбранного инструмента в меню слева: как им пользоваться и его параметры. Выбрана пометка — её настройки,
 * каким бы инструментом её ни рисовали.
 */
@Composable
internal fun ToolSettings(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit) {
    val tool = state.tool
    if (tool == null) {
        SectionHeader(stringResource(Res.string.screenshots_no_tool), description = stringResource(Res.string.screenshots_no_tool_hint))
    } else {
        SectionHeader(stringResource(tool.title), description = stringResource(tool.hint))
    }
    if (tool == EditorTool.Crop) CropSettings(state, onEvent)
    val selected = state.selectedEdit
    if (tool == EditorTool.Pixelate || selected is Edit.Pixelate) HideSettings(state, onEvent)
    val styled = selected?.takeIf { it.color != null }
    if (tool?.usesColor == true || styled != null) MarkSettings(state, styled?.let { it is Edit.Label } ?: (tool == EditorTool.Text), onEvent)
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

/** Как скрывать область — пикселями или размытием — и насколько сильно. */
@Composable
private fun HideSettings(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit) {
    OptionGroup(HideMode.entries, state.hideMode, { onEvent(ScreenshotEditorEvent.Hide(it)) }, { stringResource(it.title) })
    SectionHeader(stringResource(Res.string.screenshots_hide_strength))
    ValueSlider(state.hideStrength, { onEvent(ScreenshotEditorEvent.HideStrength(it)) }, 1..MAX_HIDE_STRENGTH, Modifier.fillMaxWidth())
}

/**
 * Цвет и толщина пометок; у надписи ([text]) вместо толщины — размер текста. В палитре за основными цветами — взятые
 * пипеткой, кнопка пипетки — справа.
 */
@Composable
private fun MarkSettings(state: ScreenshotEditorState, text: Boolean, onEvent: (ScreenshotEditorEvent) -> Unit) {
    SectionHeader(stringResource(Res.string.screenshots_color))
    val colors = EditorColors.choices + state.recentColors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        ColorPalette(
            colors = colors.map(::Color),
            selected = colors.indexOf(state.color).takeIf { it >= 0 },
            onSelect = { index -> onEvent(ScreenshotEditorEvent.SelectColor(index?.let(colors::get) ?: EditorColors.DEFAULT)) },
            defaultColor = Color(EditorColors.DEFAULT),
            defaultLabel = stringResource(Res.string.screenshots_color_default),
            modifier = Modifier.weight(1f),
        )
        ToolbarButton(
            painterResource(Res.drawable.tool_picker),
            stringResource(Res.string.screenshots_picker),
            onClick = { onEvent(ScreenshotEditorEvent.StartPicking) },
            selected = state.picking,
        )
    }
    val sizeTitle = if (text) Res.string.screenshots_text_size else Res.string.screenshots_thickness
    SectionHeader(stringResource(sizeTitle))
    ValueSlider(state.thickness, { onEvent(ScreenshotEditorEvent.Thickness(it)) }, 1..MAX_THICKNESS, Modifier.fillMaxWidth())
}
