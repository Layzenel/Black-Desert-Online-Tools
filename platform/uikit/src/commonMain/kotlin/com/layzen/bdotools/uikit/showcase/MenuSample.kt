package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.GroupedOptionsMenu
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.LabelBadge
import com.layzen.bdotools.uikit.components.LabelBadgeKind
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.showcase_badge_hot
import com.layzen.bdotools.uikit.resources.showcase_badge_new
import com.layzen.bdotools.uikit.resources.showcase_grouped_first
import com.layzen.bdotools.uikit.resources.showcase_grouped_open
import com.layzen.bdotools.uikit.resources.showcase_grouped_rest
import com.layzen.bdotools.uikit.resources.showcase_menu_character
import com.layzen.bdotools.uikit.resources.showcase_menu_game_info
import com.layzen.bdotools.uikit.resources.showcase_menu_recent
import com.layzen.bdotools.uikit.resources.showcase_menu_rewards
import com.layzen.bdotools.uikit.resources.showcase_menu_search
import com.layzen.bdotools.uikit.resources.showcase_menu_settings
import com.layzen.bdotools.uikit.resources.showcase_menu_title
import com.layzen.bdotools.uikit.resources.showcase_menu_war
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Пункт примера главного меню: подпись, горячая клавиша и плашка NEW/HOT. */
private class MenuEntry(val title: StringResource, val hotkey: String, val badge: LabelBadgeKind? = null)

private val menuEntries = listOf(
    MenuEntry(Res.string.showcase_menu_recent, "F12"),
    MenuEntry(Res.string.showcase_menu_game_info, "F1", LabelBadgeKind.New),
    MenuEntry(Res.string.showcase_menu_character, "F2"),
    MenuEntry(Res.string.showcase_menu_rewards, "F3"),
    MenuEntry(Res.string.showcase_menu_war, "F7", LabelBadgeKind.Hot),
    MenuEntry(Res.string.showcase_menu_settings, "F10"),
)

/** Пример окна «Меню»: поиск, пункты с горячими клавишами и плашками, внизу — двухуровневый список с поиском. */
@Composable
internal fun MenuSample(modifier: Modifier) {
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
            GroupedPick(selected) { selected = it }
        }
    }
}

private fun LabelBadgeKind.label() = when (this) {
    LabelBadgeKind.New -> Res.string.showcase_badge_new
    LabelBadgeKind.Hot -> Res.string.showcase_badge_hot
}

/** Двухуровневый список с поиском [GroupedOptionsMenu]: те же пункты меню в двух группах. */
@Composable
private fun GroupedPick(selected: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val half = menuEntries.size / 2
    Box(Modifier.padding(AppTheme.dimens.spaceS)) {
        ActionButton(stringResource(Res.string.showcase_grouped_open), onClick = { expanded = true })
        GroupedOptionsMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            groups = listOf(0 until half, half until menuEntries.size),
            groupLabel = { stringResource(if (it.first == 0) Res.string.showcase_grouped_first else Res.string.showcase_grouped_rest) },
            options = { it.toList() },
            selected = selected,
            onSelect = onSelect,
            label = { stringResource(menuEntries[it].title) },
            searchPlaceholder = stringResource(Res.string.showcase_menu_search),
        )
    }
}
