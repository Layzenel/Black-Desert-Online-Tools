package com.layzen.bdotools.navigation

import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentName

/** Экран, на который можно перейти. Каталог дизайн-системы открыт только по адресу `#showcase`. */
sealed interface Destination {
    data class Tab(val tab: AppTab) : Destination
    data object Showcase : Destination
}

private const val SHOWCASE_ROUTE = "showcase"

fun Destination.toFragment(): String = when (this) {
    is Destination.Tab -> buildBrowserHistoryFragment(tab.route)
    Destination.Showcase -> buildBrowserHistoryFragment(SHOWCASE_ROUTE)
}

fun destinationFromFragment(fragment: String): Destination? {
    val name = getBrowserHistoryFragmentName(fragment) ?: return null
    if (name == SHOWCASE_ROUTE) return Destination.Showcase
    return AppTab.entries.firstOrNull { it.route == name }?.let(Destination::Tab)
}
