package com.layzen.bdotools.navigation

import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentName

private const val SHOWCASE_ROUTE = "showcase"

/** Адрес экрана в истории браузера: `#portraits`, `#showcase`. */
internal fun Destination.toFragment(): String = when (this) {
    is Destination.Tab -> buildBrowserHistoryFragment(tab.route)
    Destination.Showcase -> buildBrowserHistoryFragment(SHOWCASE_ROUTE)
}

/** Экран по адресу из истории браузера; `null`, если адрес незнакомый. */
internal fun destinationFromFragment(fragment: String): Destination? {
    val name = getBrowserHistoryFragmentName(fragment) ?: return null
    if (name == SHOWCASE_ROUTE) return Destination.Showcase
    return AppTab.entries.firstOrNull { it.route == name }?.let(Destination::Tab)
}
