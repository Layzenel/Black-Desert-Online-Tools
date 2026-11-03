package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.layzen.bdotools.uikit.components.HoverMenuBox
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.LabelBadge
import com.layzen.bdotools.uikit.components.LabelBadgeKind
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.NavGroup
import com.layzen.bdotools.uikit.components.NavItem
import com.layzen.bdotools.uikit.components.NumberField
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.PopupMenuItem
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.components.ToggleSwitch
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.components.ValueText
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.showcase_badge_hot
import com.layzen.bdotools.uikit.resources.showcase_badge_new
import com.layzen.bdotools.uikit.resources.showcase_exit_change_server
import com.layzen.bdotools.uikit.resources.showcase_exit_character
import com.layzen.bdotools.uikit.resources.showcase_exit_family
import com.layzen.bdotools.uikit.resources.showcase_exit_quit
import com.layzen.bdotools.uikit.resources.showcase_exit_select
import com.layzen.bdotools.uikit.resources.showcase_exit_tile_stats
import com.layzen.bdotools.uikit.resources.showcase_exit_title
import com.layzen.bdotools.uikit.resources.showcase_modal_close
import com.layzen.bdotools.uikit.resources.showcase_modal_description
import com.layzen.bdotools.uikit.resources.showcase_modal_header
import com.layzen.bdotools.uikit.resources.showcase_modal_title
import com.layzen.bdotools.uikit.resources.showcase_menu_character
import com.layzen.bdotools.uikit.resources.showcase_menu_game_info
import com.layzen.bdotools.uikit.resources.showcase_menu_recent
import com.layzen.bdotools.uikit.resources.showcase_menu_rewards
import com.layzen.bdotools.uikit.resources.showcase_menu_search
import com.layzen.bdotools.uikit.resources.showcase_menu_settings
import com.layzen.bdotools.uikit.resources.showcase_menu_title
import com.layzen.bdotools.uikit.resources.showcase_menu_war
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
        }
    }
}

private class MenuEntry(val title: StringResource, val hotkey: String, val badge: LabelBadgeKind? = null)

private val menuEntries = listOf(
    MenuEntry(Res.string.showcase_menu_recent, "F12"),
    MenuEntry(Res.string.showcase_menu_game_info, "F1", LabelBadgeKind.New),
    MenuEntry(Res.string.showcase_menu_character, "F2"),
    MenuEntry(Res.string.showcase_menu_rewards, "F3"),
    MenuEntry(Res.string.showcase_menu_war, "F7", LabelBadgeKind.Hot),
    MenuEntry(Res.string.showcase_menu_settings, "F10"),
)

@Composable
private fun MenuSample(modifier: Modifier) {
    var selected by remember { mutableIntStateOf(0) }
    GameWindow(title = stringResource(Res.string.showcase_menu_title), modifier = modifier) {
        Column(Modifier.background(AppTheme.colors.backgroundRaised)) {
            InputField(
                value = "",
                onValueChange = {},
                placeholder = stringResource(Res.string.showcase_menu_search),
                modifier = Modifier.fillMaxWidth().padding(AppTheme.dimens.spaceS),
            )
            menuEntries.forEachIndexed { index, entry ->
                val title = stringResource(entry.title)
                MenuItem(
                    text = title,
                    hotkey = entry.hotkey,
                    selected = index == selected,
                    highlighted = entry.badge != null,
                    badge = entry.badge?.let { kind -> { LabelBadge(stringResource(kind.label()), kind = kind) } },
                    icon = { Text(title.take(1)) },
                    onClick = { selected = index },
                )
            }
        }
    }
}

private fun LabelBadgeKind.label() = when (this) {
    LabelBadgeKind.New -> Res.string.showcase_badge_new
    LabelBadgeKind.Hot -> Res.string.showcase_badge_hot
}

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

@Composable
private fun SettingsSample(modifier: Modifier) {
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

@Composable
private fun TilesSample(modifier: Modifier) {
    var selected by remember { mutableIntStateOf(0) }
    var modalOpen by remember { mutableStateOf(false) }
    if (modalOpen) {
        ModalWindow(
            title = stringResource(Res.string.showcase_modal_title),
            onDismissRequest = { modalOpen = false },
            footer = { ActionButton(stringResource(Res.string.showcase_modal_close), onClick = { modalOpen = false }) },
        ) {
            SectionHeader(
                stringResource(Res.string.showcase_modal_header),
                modifier = Modifier.padding(AppTheme.dimens.spaceL),
                description = stringResource(Res.string.showcase_modal_description),
            )
        }
    }
    GameWindow(
        title = stringResource(Res.string.showcase_exit_title),
        modifier = modifier,
        footer = {
            ActionButton(stringResource(Res.string.showcase_exit_change_server), onClick = {}, modifier = Modifier.weight(1f))
            ActionButton(stringResource(Res.string.showcase_exit_quit), onClick = {}, modifier = Modifier.weight(1f))
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceM), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    ValueText(stringResource(Res.string.showcase_exit_character, 68, "Exegi"))
                    Text(
                        stringResource(Res.string.showcase_exit_family),
                        style = AppTheme.typography.bodyBold,
                        color = AppTheme.colors.textBeige,
                    )
                }
                ActionButton(stringResource(Res.string.showcase_exit_select), onClick = { modalOpen = true })
            }
            (0 until 2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                    (0 until 4).forEach { col ->
                        val index = row * 4 + col
                        HoverMenuBox(
                            modifier = Modifier.size(ShowcaseDimens.tileSize),
                            menu = { close ->
                                PopupMenuItem(stringResource(Res.string.showcase_exit_select), onClick = {
                                    selected = index
                                    close()
                                })
                                PopupMenuItem(stringResource(Res.string.showcase_modal_title), onClick = {
                                    close()
                                    modalOpen = true
                                })
                            },
                        ) { menuOpen ->
                            Tile(Modifier.matchParentSize(), selected = index == selected || menuOpen) {
                                Text(
                                    stringResource(Res.string.showcase_exit_tile_stats, 394, 2),
                                    style = AppTheme.typography.bodyBold,
                                    color = AppTheme.colors.textPrimary,
                                    modifier = Modifier.align(Alignment.BottomCenter).padding(AppTheme.dimens.spaceXs),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
