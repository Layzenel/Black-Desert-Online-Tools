package com.layzen.bdotools.feature.slots.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.data.SlotsError
import com.layzen.bdotools.feature.slots.presentation.state.SlotsNotice
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_error_file_changed
import com.layzen.bdotools.feature.slots.resources.slots_error_no_game_variable
import com.layzen.bdotools.feature.slots.resources.slots_error_not_game_folder
import com.layzen.bdotools.feature.slots.resources.slots_error_not_image
import com.layzen.bdotools.feature.slots.resources.slots_error_permission
import com.layzen.bdotools.feature.slots.resources.slots_error_unknown
import com.layzen.bdotools.feature.slots.resources.slots_notice_applied
import com.layzen.bdotools.feature.slots.resources.slots_notice_deleted
import com.layzen.bdotools.feature.slots.resources.slots_notice_imported
import com.layzen.bdotools.feature.slots.resources.slots_notice_saved
import org.jetbrains.compose.resources.stringResource

/** Текст всплывающего сообщения. */
@Composable
internal fun noticeText(notice: SlotsNotice): String = when (notice) {
    is SlotsNotice.Applied -> stringResource(Res.string.slots_notice_applied, notice.backupPath)
    is SlotsNotice.PresetSaved -> stringResource(Res.string.slots_notice_saved, notice.name)
    is SlotsNotice.PresetDeleted -> stringResource(Res.string.slots_notice_deleted, notice.name)
    is SlotsNotice.Imported -> stringResource(Res.string.slots_notice_imported, notice.name)
    is SlotsNotice.Failed -> stringResource(
        when (notice.error) {
            SlotsError.NotGameFolder -> Res.string.slots_error_not_game_folder
            SlotsError.Permission -> Res.string.slots_error_permission
            SlotsError.NoGameVariable -> Res.string.slots_error_no_game_variable
            SlotsError.FileChanged -> Res.string.slots_error_file_changed
            SlotsError.NotImage -> Res.string.slots_error_not_image
            SlotsError.Cancelled, SlotsError.Unknown -> Res.string.slots_error_unknown
        },
    )
}
