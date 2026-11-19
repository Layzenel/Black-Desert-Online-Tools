package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.layzen.bdotools.composable.resources.Res
import com.layzen.bdotools.composable.resources.overlay_tab_settings

/** Место общей вкладки «Настройки»: первой, левее вкладок фич. */
private const val SETTINGS_ORDER = Int.MIN_VALUE

/**
 * Всё поверх игры в настольной версии: окна фич из [contributions] и одна кнопка меню оверлея с их вкладками.
 * Пункты [OverlayContribution.settings] всех фич собираются в одну вкладку «Настройки» — первую.
 * Ставится в корень приложения рядом с окном программы, а не внутри него: свёрнутое в трей окно не перерисовывается.
 * [onOpenApp] — развернуть программу на вкладке с маршрутом из [OverlayContribution.id].
 */
@Composable
fun OverlayHost(contributions: List<OverlayContribution>, onOpenApp: (id: String) -> Unit) {
    contributions.forEach { it.windows() }
    val settings = contributions.filter { it.settings != null }
    val settingsTab = settings.firstOrNull()?.let { first ->
        first.id to OverlayTab(SETTINGS_ORDER, Res.string.overlay_tab_settings) { settings.forEach { it.settings?.invoke() } }
    }
    val tabs = (listOfNotNull(settingsTab) + contributions.flatMap { contribution -> contribution.tabs.map { contribution.id to it } })
        .sortedBy { it.second.order }
    if (tabs.isEmpty()) return
    var open by remember { mutableStateOf(false) }
    OverlayMenuWindow(open) { dragModifier ->
        OverlayMenu(tabs, open, { open = it }, onOpenApp, dragModifier)
    }
}
