package com.layzen.bdotools.feature.settings.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.settings.resources.Res
import com.layzen.bdotools.feature.settings.resources.settings_overlay_soon
import com.layzen.bdotools.feature.settings.resources.settings_overlay_title
import com.layzen.bdotools.uikit.components.SectionHeader
import org.jetbrains.compose.resources.stringResource

/** Раздел «Оверлей»: пока заготовка, настройки оверлея появятся здесь позже. */
@Composable
internal fun OverlaySettings() {
    SectionHeader(stringResource(Res.string.settings_overlay_title), description = stringResource(Res.string.settings_overlay_soon))
}
