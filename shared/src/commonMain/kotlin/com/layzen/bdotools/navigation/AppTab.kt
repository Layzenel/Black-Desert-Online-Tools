package com.layzen.bdotools.navigation

import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.tab_3
import com.layzen.bdotools.resources.tab_4
import com.layzen.bdotools.resources.tab_5
import com.layzen.bdotools.resources.tab_portraits
import com.layzen.bdotools.resources.tab_portraits_icon
import com.layzen.bdotools.resources.tab_slots
import com.layzen.bdotools.resources.tab_slots_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * Вкладки верхней навигации. [route] попадает в адрес страницы: `#portraits`.
 * [fillsScreen] — вкладка занимает всё окно под навигацией без внешних отступов (редактор с меню слева)
 * и прокручивает свои части сама, а не страницей. [icon] — белый силуэт слева от подписи вкладки.
 */
enum class AppTab(
    val route: String,
    val title: StringResource,
    val icon: DrawableResource,
    val fillsScreen: Boolean = false,
) {
    Portraits("portraits", Res.string.tab_portraits, Res.drawable.tab_portraits_icon),
    Slots("slots", Res.string.tab_slots, Res.drawable.tab_slots_icon, fillsScreen = true)
}
