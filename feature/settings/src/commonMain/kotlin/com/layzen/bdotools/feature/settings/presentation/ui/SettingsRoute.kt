package com.layzen.bdotools.feature.settings.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.layzen.bdotools.feature.settings.presentation.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Экран настроек со своей ViewModel. */
@Composable
fun SettingsRoute(viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(state, viewModel::onEvent)
}
