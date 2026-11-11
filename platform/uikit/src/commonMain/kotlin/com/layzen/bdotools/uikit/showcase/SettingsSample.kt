package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.NavGroup
import com.layzen.bdotools.uikit.components.NavItem
import com.layzen.bdotools.uikit.components.NumberField
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ToggleSwitch
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.showcase_settings_apply
import com.layzen.bdotools.uikit.resources.showcase_settings_audio
import com.layzen.bdotools.uikit.resources.showcase_settings_back
import com.layzen.bdotools.uikit.resources.showcase_settings_camera
import com.layzen.bdotools.uikit.resources.showcase_settings_crop
import com.layzen.bdotools.uikit.resources.showcase_settings_crop_description
import com.layzen.bdotools.uikit.resources.showcase_settings_effects
import com.layzen.bdotools.uikit.resources.showcase_settings_fps
import com.layzen.bdotools.uikit.resources.showcase_settings_help
import com.layzen.bdotools.uikit.resources.showcase_settings_mode_borderless
import com.layzen.bdotools.uikit.resources.showcase_settings_mode_fullscreen
import com.layzen.bdotools.uikit.resources.showcase_settings_mode_windowed
import com.layzen.bdotools.uikit.resources.showcase_settings_ok
import com.layzen.bdotools.uikit.resources.showcase_settings_performance
import com.layzen.bdotools.uikit.resources.showcase_settings_quality
import com.layzen.bdotools.uikit.resources.showcase_settings_reset
import com.layzen.bdotools.uikit.resources.showcase_settings_resolution
import com.layzen.bdotools.uikit.resources.showcase_settings_resolution_description
import com.layzen.bdotools.uikit.resources.showcase_settings_resolution_warning
import com.layzen.bdotools.uikit.resources.showcase_settings_scale
import com.layzen.bdotools.uikit.resources.showcase_settings_scale_description
import com.layzen.bdotools.uikit.resources.showcase_settings_screen
import com.layzen.bdotools.uikit.resources.showcase_settings_screen_size
import com.layzen.bdotools.uikit.resources.showcase_settings_title
import com.layzen.bdotools.uikit.resources.showcase_settings_video
import com.layzen.bdotools.uikit.resources.showcase_settings_window
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Варианты режима экрана в примере окна настроек. */
private enum class ScreenMode(val title: StringResource) {
    Fullscreen(Res.string.showcase_settings_mode_fullscreen),
    Borderless(Res.string.showcase_settings_mode_borderless),
    Windowed(Res.string.showcase_settings_mode_windowed),
}

private val videoSections = listOf(
    Res.string.showcase_settings_window,
    Res.string.showcase_settings_quality,
    Res.string.showcase_settings_effects,
    Res.string.showcase_settings_camera,
)

private val resolutions = listOf("1920x1080", "2560x1440", "3840x2160")

/** Пример окна «Настройки»: боковая навигация и все виды полей. */
@Composable
internal fun SettingsSample(modifier: Modifier) {
    val dimens = AppTheme.dimens
    var nav by remember { mutableStateOf(Res.string.showcase_settings_window) }
    var mode by remember { mutableStateOf(ScreenMode.Borderless) }
    var resolution by remember { mutableStateOf(resolutions[1]) }
    var scale by remember { mutableIntStateOf(80) }
    var crop by remember { mutableStateOf(true) }
    var fps by remember { mutableIntStateOf(144) }

    GameWindow(
        title = stringResource(Res.string.showcase_settings_title),
        modifier = modifier,
        actions = {
            Text(
                stringResource(Res.string.showcase_settings_help),
                style = AppTheme.typography.windowTitle,
                color = AppTheme.colors.textPrimary,
            )
        },
        footer = {
            ActionButton(stringResource(Res.string.showcase_settings_apply), onClick = {})
            ActionButton(stringResource(Res.string.showcase_settings_back), onClick = {})
            ActionButton(stringResource(Res.string.showcase_settings_ok), onClick = {})
        },
    ) {
        Row {
            Column(
                Modifier
                    .width(dimens.sidebarWidth)
                    .fillMaxHeight()
                    .background(AppTheme.colors.backgroundSurface)
                    .padding(vertical = dimens.spaceS),
            ) {
                SampleNavGroup(Res.string.showcase_settings_performance)
                SampleNavGroup(Res.string.showcase_settings_video) {
                    videoSections.forEach { section ->
                        NavItem(stringResource(section), selected = nav == section, onClick = { nav = section })
                    }
                }
                SampleNavGroup(Res.string.showcase_settings_audio)
            }
            Column(
                Modifier.weight(1f).padding(dimens.spaceL),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceM),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader(stringResource(Res.string.showcase_settings_screen_size), Modifier.weight(1f))
                    ActionButton(stringResource(Res.string.showcase_settings_reset), onClick = {})
                }
                SectionHeader(stringResource(Res.string.showcase_settings_screen))
                OptionGroup(ScreenMode.entries, mode, { mode = it }, { stringResource(it.title) }, Modifier.fillMaxWidth())
                SectionDivider()
                SectionHeader(
                    stringResource(Res.string.showcase_settings_resolution),
                    warning = stringResource(Res.string.showcase_settings_resolution_warning),
                    description = stringResource(Res.string.showcase_settings_resolution_description),
                )
                DropdownSelect(
                    resolution,
                    resolutions,
                    { resolution = it },
                    { it },
                    Modifier.fillMaxWidth(0.7f).align(Alignment.CenterHorizontally),
                )
                SectionDivider()
                SectionHeader(
                    stringResource(Res.string.showcase_settings_scale),
                    description = stringResource(Res.string.showcase_settings_scale_description),
                )
                ValueSlider(scale, { scale = it }, 50..200)
                SectionDivider()
                ToggleSwitch(
                    crop,
                    { crop = it },
                    label = stringResource(Res.string.showcase_settings_crop),
                    description = stringResource(Res.string.showcase_settings_crop_description),
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.spaceS)) {
                    SectionHeader(stringResource(Res.string.showcase_settings_fps), Modifier.weight(1f))
                    NumberField(fps, { fps = it }, Modifier.width(ShowcaseDimens.numberFieldWidth), range = 30..360)
                }
            }
        }
    }
}

/** Группа навигации, где вместо иконки — первая буква названия. */
@Composable
private fun SampleNavGroup(title: StringResource, children: (@Composable () -> Unit)? = null) {
    val text = stringResource(title)
    NavGroup(text, icon = { Text(text.take(1)) }, children = children)
}
