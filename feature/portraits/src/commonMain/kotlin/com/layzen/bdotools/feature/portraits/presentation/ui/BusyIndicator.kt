package com.layzen.bdotools.feature.portraits.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraits.presentation.state.Busy
import com.layzen.bdotools.feature.portraits.presentation.state.BusyKind
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_deleting
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_progress
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_reading_favorites
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_rendering
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_restoring
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_saving
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_saving_favorites
import com.layzen.bdotools.uikit.components.BusyOverlay
import org.jetbrains.compose.resources.stringResource

/** Окно ожидания с текстом по виду операции и прогрессом. */
@Composable
internal fun BusyIndicator(busy: Busy) {
    val text = when (busy.kind) {
        BusyKind.Rendering -> Res.string.portraits_busy_rendering
        BusyKind.Saving -> Res.string.portraits_busy_saving
        BusyKind.Restoring -> Res.string.portraits_busy_restoring
        BusyKind.Deleting -> Res.string.portraits_busy_deleting
        BusyKind.SavingFavorites -> Res.string.portraits_busy_saving_favorites
        BusyKind.ReadingFavorites -> Res.string.portraits_busy_reading_favorites
    }
    BusyOverlay(
        text = stringResource(text),
        progress = busy.progress,
        details = if (busy.total > 0) stringResource(Res.string.portraits_busy_progress, busy.done, busy.total) else null,
    )
}
