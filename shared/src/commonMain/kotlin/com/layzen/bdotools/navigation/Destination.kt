package com.layzen.bdotools.navigation

import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentName
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.tab_2
import com.layzen.bdotools.resources.tab_3
import com.layzen.bdotools.resources.tab_4
import com.layzen.bdotools.resources.tab_5
import com.layzen.bdotools.resources.tab_portraits
import org.jetbrains.compose.resources.StringResource

/** Вкладки верхней навигации. [route] попадает в адрес страницы: `#portraits`. */
enum class AppTab(val route: String, val title: StringResource) {
    Portraits("portraits", Res.string.tab_portraits),
    Tab2("tab-2", Res.string.tab_2),
    Tab3("tab-3", Res.string.tab_3),
    Tab4("tab-4", Res.string.tab_4),
    Tab5("tab-5", Res.string.tab_5),
}

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
