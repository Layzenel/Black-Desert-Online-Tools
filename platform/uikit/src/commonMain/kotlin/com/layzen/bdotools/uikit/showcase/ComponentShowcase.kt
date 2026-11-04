package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/** Каталог компонентов дизайн-системы. Служит живым образцом и проверкой после изменений. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComponentShowcase(modifier: Modifier = Modifier) {
    val dimens = AppTheme.dimens
    Box(
        modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(dimens.spaceXl),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceXl),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceXl),
        ) {
            MenuSample(Modifier.width(ShowcaseDimens.menuWindowWidth))
            SettingsSample(Modifier.widthIn(max = dimens.contentMaxWidth))
            TilesSample(Modifier.width(ShowcaseDimens.tilesWindowWidth))
            FeedbackSample(Modifier.width(ShowcaseDimens.tilesWindowWidth))
        }
    }
}

