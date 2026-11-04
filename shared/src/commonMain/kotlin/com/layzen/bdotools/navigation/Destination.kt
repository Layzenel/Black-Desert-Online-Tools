package com.layzen.bdotools.navigation

/** Экран, на который можно перейти. Каталог дизайн-системы открыт только по адресу `#showcase`. */
sealed interface Destination {
    data class Tab(val tab: AppTab) : Destination
    data object Showcase : Destination
}
