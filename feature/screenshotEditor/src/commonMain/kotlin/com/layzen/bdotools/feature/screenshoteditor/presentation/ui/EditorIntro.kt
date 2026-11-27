package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_crop
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_description
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_export
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_marks
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_pixelate
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_title
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_open
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_open_tooltip
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.BulletList
import com.layzen.bdotools.uikit.components.ContentFrame
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Пока скриншот не открыт: что умеет редактор и кнопка «Открыть». */
@Composable
internal fun EditorIntro(onEvent: (ScreenshotEditorEvent) -> Unit) {
    ContentFrame(Modifier.padding(AppTheme.dimens.spaceXl).widthIn(max = AppTheme.dimens.contentMaxWidth)) {
        SectionHeader(
            stringResource(Res.string.screenshots_intro_title),
            description = stringResource(Res.string.screenshots_intro_description),
        )
        BulletList(
            listOf(
                stringResource(Res.string.screenshots_intro_crop),
                stringResource(Res.string.screenshots_intro_pixelate),
                stringResource(Res.string.screenshots_intro_marks),
                stringResource(Res.string.screenshots_intro_export),
            ),
        )
        ActionButton(
            ActionButtonState.Positive(
                stringResource(Res.string.screenshots_open),
                onClick = { onEvent(ScreenshotEditorEvent.Open) },
                tooltip = stringResource(Res.string.screenshots_open_tooltip),
            ),
            Modifier.fillMaxWidth(),
        )
    }
}
