package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.FilterPreset
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.CropAspect
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorNotice
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorTool
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_aspect_free
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_aspect_square
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_aspect_tall
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_aspect_wide
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_copied
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_trashed
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_downloaded
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_error_clipboard
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_error_folder
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_error_trash
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_error_not_image
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_error_save
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_filter_cinema
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_filter_cold
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_filter_mono
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_filter_none
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_filter_vivid
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_filter_warm
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_saved
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_arrow
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_arrow_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_brush
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_brush_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_crop
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_crop_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_frame
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_frame_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_pixelate
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_pixelate_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_text
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_tool_text_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_arrow
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_brush
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_crop
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_frame
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_pixelate
import com.layzen.bdotools.feature.screenshoteditor.resources.tool_text
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Название инструмента — подпись и подсказка кнопки. */
internal val EditorTool.title: StringResource
    get() = when (this) {
        EditorTool.Crop -> Res.string.screenshots_tool_crop
        EditorTool.Pixelate -> Res.string.screenshots_tool_pixelate
        EditorTool.Arrow -> Res.string.screenshots_tool_arrow
        EditorTool.Frame -> Res.string.screenshots_tool_frame
        EditorTool.Brush -> Res.string.screenshots_tool_brush
        EditorTool.Text -> Res.string.screenshots_tool_text
    }

/** Как пользоваться инструментом — описание секции в меню. */
internal val EditorTool.hint: StringResource
    get() = when (this) {
        EditorTool.Crop -> Res.string.screenshots_tool_crop_hint
        EditorTool.Pixelate -> Res.string.screenshots_tool_pixelate_hint
        EditorTool.Arrow -> Res.string.screenshots_tool_arrow_hint
        EditorTool.Frame -> Res.string.screenshots_tool_frame_hint
        EditorTool.Brush -> Res.string.screenshots_tool_brush_hint
        EditorTool.Text -> Res.string.screenshots_tool_text_hint
    }

/** Значок инструмента в строке инструментов. */
internal val EditorTool.icon: DrawableResource
    get() = when (this) {
        EditorTool.Crop -> Res.drawable.tool_crop
        EditorTool.Pixelate -> Res.drawable.tool_pixelate
        EditorTool.Arrow -> Res.drawable.tool_arrow
        EditorTool.Frame -> Res.drawable.tool_frame
        EditorTool.Brush -> Res.drawable.tool_brush
        EditorTool.Text -> Res.drawable.tool_text
    }

/** Подпись пропорций обрезки. */
internal val CropAspect.title: StringResource
    get() = when (this) {
        CropAspect.Free -> Res.string.screenshots_aspect_free
        CropAspect.Wide -> Res.string.screenshots_aspect_wide
        CropAspect.Square -> Res.string.screenshots_aspect_square
        CropAspect.Tall -> Res.string.screenshots_aspect_tall
    }

/** Текст сообщения о результате действия. */
@Composable
internal fun noticeText(notice: EditorNotice): String = when (notice) {
    is EditorNotice.Saved -> stringResource(Res.string.screenshots_saved, notice.path)
    EditorNotice.Downloaded -> stringResource(Res.string.screenshots_downloaded)
    EditorNotice.Copied -> stringResource(Res.string.screenshots_copied)
    EditorNotice.Trashed -> stringResource(Res.string.screenshots_trashed)
    is EditorNotice.Failed -> stringResource(
        when (notice.error) {
            ScreenshotError.NotImage -> Res.string.screenshots_error_not_image
            ScreenshotError.Save -> Res.string.screenshots_error_save
            ScreenshotError.Clipboard -> Res.string.screenshots_error_clipboard
            ScreenshotError.Folder -> Res.string.screenshots_error_folder
            ScreenshotError.Trash -> Res.string.screenshots_error_trash
        },
    )
}

/** Название готового фильтра. */
internal val FilterPreset.title: StringResource
    get() = when (this) {
        FilterPreset.None -> Res.string.screenshots_filter_none
        FilterPreset.Warm -> Res.string.screenshots_filter_warm
        FilterPreset.Cold -> Res.string.screenshots_filter_cold
        FilterPreset.Vivid -> Res.string.screenshots_filter_vivid
        FilterPreset.Cinema -> Res.string.screenshots_filter_cinema
        FilterPreset.Mono -> Res.string.screenshots_filter_mono
    }
