package com.layzen.bdotools.feature.settings.presentation.state

import com.layzen.bdotools.feature.settings.resources.Res
import com.layzen.bdotools.feature.settings.resources.settings_general
import com.layzen.bdotools.feature.settings.resources.settings_overlay
import com.layzen.bdotools.feature.settings.resources.settings_section_general
import com.layzen.bdotools.feature.settings.resources.settings_section_overlay
import com.layzen.bdotools.feature.settings.resources.settings_section_theme
import com.layzen.bdotools.feature.settings.resources.settings_theme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Раздел экрана настроек в меню слева: подпись и белый силуэт [icon] слева от неё. */
enum class SettingsSection(val title: StringResource, val icon: DrawableResource) {
    General(Res.string.settings_section_general, Res.drawable.settings_general),
    Theme(Res.string.settings_section_theme, Res.drawable.settings_theme),
    Overlay(Res.string.settings_section_overlay, Res.drawable.settings_overlay),
}
