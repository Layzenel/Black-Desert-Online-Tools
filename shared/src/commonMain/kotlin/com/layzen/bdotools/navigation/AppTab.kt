package com.layzen.bdotools.navigation

import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.tab_3
import com.layzen.bdotools.resources.tab_4
import com.layzen.bdotools.resources.tab_5
import com.layzen.bdotools.resources.tab_portraits
import com.layzen.bdotools.resources.tab_slots
import org.jetbrains.compose.resources.StringResource

/** Вкладки верхней навигации. [route] попадает в адрес страницы: `#portraits`. */
enum class AppTab(val route: String, val title: StringResource) {
    Portraits("portraits", Res.string.tab_portraits),
    Slots("slots", Res.string.tab_slots),
    Tab3("tab-3", Res.string.tab_3),
    Tab4("tab-4", Res.string.tab_4),
    Tab5("tab-5", Res.string.tab_5),
}
