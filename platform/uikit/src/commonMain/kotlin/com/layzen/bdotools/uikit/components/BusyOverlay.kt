package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Окно ожидания поверх страницы: закрыть нельзя, пока идёт долгая операция.
 * [progress] — доля выполненного или `null`, если её не посчитать.
 */
@Composable
fun BusyOverlay(text: String, progress: Float? = null, details: String? = null) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier
                .width(AppTheme.dimens.busyOverlayWidth)
                .background(AppTheme.colors.backgroundRaised)
                .border(AppTheme.dimens.borderWidth, AppTheme.colors.borderSelected)
                .padding(AppTheme.dimens.spaceL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            Spinner()
            Text(text, style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textBeige)
            if (progress != null) ProgressBar(progress)
            if (details != null) DescriptionText(details)
        }
    }
}
