package com.layzen.bdotools.feature.portraits.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.LoadProgress
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraits.presentation.ui.grid.PortraitGridView
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_empty
import com.layzen.bdotools.feature.portraits.resources.portraits_loading_progress
import com.layzen.bdotools.feature.portraits.resources.portraits_reorder_banner
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ProgressBar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Правая часть страницы: прогресс чтения, плашка режима изменения порядка и сетка портретов. */
@Composable
internal fun PortraitsContent(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val loading = state.loading
    when {
        state.portraits.isEmpty() && loading != null -> LoadingProgress(loading)
        state.portraits.isEmpty() -> DescriptionText(stringResource(Res.string.portraits_empty))
        else -> {
            loading?.let { LoadingProgress(it) }
            if (state.reorder != null) ReorderBanner()
            PortraitGridView(state, onEvent)
        }
    }
}

/** Над сеткой, пока включено изменение порядка: что делать и как выйти. */
@Composable
private fun ReorderBanner() {
    val colors = AppTheme.colors
    Text(
        stringResource(Res.string.portraits_reorder_banner),
        modifier = Modifier
            .background(colors.backgroundRaised)
            .border(AppTheme.dimens.borderWidth, colors.borderSelected)
            .padding(horizontal = AppTheme.dimens.spaceM, vertical = AppTheme.dimens.spaceS),
        style = AppTheme.typography.bodyBold,
        color = colors.textPrimary,
    )
}

/** Прогресс чтения портретов над сеткой из скелетонов. */
@Composable
private fun LoadingProgress(progress: LoadProgress) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        DescriptionText(stringResource(Res.string.portraits_loading_progress, progress.done, progress.total))
        ProgressBar(progress.fraction)
    }
}
