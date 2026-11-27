package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportScale
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_export
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_export_size
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_scale_percent
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** Размер и формат копии; в описании — итоговый размер в пикселях с учётом обрезки. */
@Composable
internal fun ExportSettings(state: ScreenshotEditorState, onEvent: (ScreenshotEditorEvent) -> Unit) {
    val image = state.image ?: return
    val crop = state.history.crop
    val size = state.scale.applyTo(
        crop?.width?.roundToInt() ?: image.width,
        crop?.height?.roundToInt() ?: image.height,
    )
    SectionHeader(
        stringResource(Res.string.screenshots_export),
        description = stringResource(Res.string.screenshots_export_size, size.width, size.height),
    )
    DropdownSelect(
        state.scale,
        ExportScale.entries,
        { onEvent(ScreenshotEditorEvent.Scale(it)) },
        { stringResource(Res.string.screenshots_scale_percent, it.percent) },
        Modifier.fillMaxWidth(),
    )
    OptionGroup(
        ExportFormat.entries,
        state.format,
        { onEvent(ScreenshotEditorEvent.Format(it)) },
        // расширение файла одинаково на всех языках
        { it.extension.uppercase() },
    )
}
