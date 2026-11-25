package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.delay

/** Время ПК в строке информационной панели; обновляется раз в секунду, чтобы смена минуты была видна сразу. */
@Composable
internal fun TimeValue() {
    val time by produceState(pcTime()) {
        while (true) {
            delay(TICK_MS)
            value = pcTime()
        }
    }
    Text(time, style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textPrimary)
}

private const val TICK_MS = 1000L
