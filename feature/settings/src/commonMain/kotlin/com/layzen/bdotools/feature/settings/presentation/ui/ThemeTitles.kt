package com.layzen.bdotools.feature.settings.presentation.ui

import com.layzen.bdotools.core.theme.WindowTheme
import com.layzen.bdotools.feature.settings.resources.Res
import com.layzen.bdotools.feature.settings.resources.settings_theme_classic
import com.layzen.bdotools.feature.settings.resources.settings_theme_morning
import com.layzen.bdotools.feature.settings.resources.settings_theme_spring
import com.layzen.bdotools.feature.settings.resources.settings_theme_summer
import com.layzen.bdotools.feature.settings.resources.settings_theme_winter
import org.jetbrains.compose.resources.StringResource

/** Название темы окон, как в настройках игры. */
internal val WindowTheme.title: StringResource
    get() = when (this) {
        WindowTheme.Classic -> Res.string.settings_theme_classic
        WindowTheme.Summer -> Res.string.settings_theme_summer
        WindowTheme.Winter -> Res.string.settings_theme_winter
        WindowTheme.Spring -> Res.string.settings_theme_spring
        WindowTheme.MorningLand -> Res.string.settings_theme_morning
    }
