package com.layzen.bdotools.navigation

import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentName

private const val SHOWCASE_ROUTE = "showcase"
private const val SETTINGS_ROUTE = "settings"

/** Адрес экрана в истории браузера: `#portraits`, `#showcase`. */
internal fun Destination.toFragment(): String = when (this) {
    is Destination.Tab -> buildBrowserHistoryFragment(tab.route)
    Destination.Showcase -> buildBrowserHistoryFragment(SHOWCASE_ROUTE)
    Destination.Settings -> buildBrowserHistoryFragment(SETTINGS_ROUTE)
}

/** Экран по адресу из истории браузера; `null`, если адрес незнакомый. */
internal fun destinationFromFragment(fragment: String): Destination? {
    val name = getBrowserHistoryFragmentName(fragment) ?: return null
    if (name == SHOWCASE_ROUTE) return Destination.Showcase
    if (name == SETTINGS_ROUTE) return Destination.Settings
    return AppTab.available.firstOrNull { it.route == name }?.let(Destination::Tab)
}
