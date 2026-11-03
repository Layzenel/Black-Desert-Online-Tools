package com.layzen.bdotools.feature.portraits.domain

/**
 * Место значка. Углы портрета заняты значками игры, поэтому один значок по центру верхней полосы
 * и по одному у левого и правого краёв.
 */
enum class BadgeSlot(val key: String) {
    Top("top"),
    Left("left"),
    Right("right"),
    ;

    companion object {
        /** `left-top`, `left-bottom` и т. д. — ключи из версии, где сбоку было по два значка. */
        fun fromKey(key: String): BadgeSlot? = when (key) {
            "left-top", "left-bottom" -> Left
            "right-top", "right-bottom" -> Right
            else -> entries.firstOrNull { it.key == key }
        }
    }
}
