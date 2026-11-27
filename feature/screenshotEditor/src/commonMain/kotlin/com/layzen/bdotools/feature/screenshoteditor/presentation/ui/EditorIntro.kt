package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.screenshoteditor.presentation.gallery.GalleryEvent
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_folder_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_folder_permission
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_folder_permission_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_folder_select
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_folder_select_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_folder_unsupported
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_crop
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_description
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_export
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_marks
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_pixelate
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_intro_title
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_open_file
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_open_tooltip
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.BulletList
import com.layzen.bdotools.uikit.components.ContentFrame
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Пока к папке игры нет доступа ([access]): что умеет редактор, «Открыть файл» и, в браузере, выбор папки
 * `Black Desert` или повторное разрешение на неё — тогда здесь будет галерея.
 */
@Composable
internal fun EditorIntro(access: FolderAccess?, onOpenFile: () -> Unit, onEvent: (GalleryEvent) -> Unit) {
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
        when (access) {
            FolderAccess.Unsupported -> WarningText(stringResource(Res.string.screenshots_folder_unsupported))
            null, is FolderAccess.Granted -> Unit
            else -> DescriptionText(stringResource(Res.string.screenshots_folder_hint))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            folderButton(access, onEvent)?.let { ActionButton(it, Modifier.weight(1f)) }
            ActionButton(
                ActionButtonState.Positive(
                    stringResource(Res.string.screenshots_open_file),
                    onClick = onOpenFile,
                    tooltip = stringResource(Res.string.screenshots_open_tooltip),
                ),
                Modifier.weight(1f),
            )
        }
    }
}

/** Кнопка доступа к папке: выбрать её или снова разрешить; `null` — не нужна. */
@Composable
private fun folderButton(access: FolderAccess?, onEvent: (GalleryEvent) -> Unit): ActionButtonState? = when (access) {
    FolderAccess.NotSelected -> ActionButtonState.Neutral(
        stringResource(Res.string.screenshots_folder_select),
        onClick = { onEvent(GalleryEvent.SelectFolder) },
        tooltip = stringResource(Res.string.screenshots_folder_select_tooltip),
    )
    is FolderAccess.NeedsPermission -> ActionButtonState.Neutral(
        stringResource(Res.string.screenshots_folder_permission),
        onClick = { onEvent(GalleryEvent.RequestPermission) },
        tooltip = stringResource(Res.string.screenshots_folder_permission_tooltip),
    )
    else -> null
}
