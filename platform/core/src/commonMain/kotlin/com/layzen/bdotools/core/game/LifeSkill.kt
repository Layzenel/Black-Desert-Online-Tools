package com.layzen.bdotools.core.game

/**
 * Жизненный навык (занятие) Black Desert Online. [key] попадает в файлы тулз, поэтому после выпуска его не меняют.
 * Иконки и названия — в дизайн-системе: `LifeSkill.icon` и `LifeSkill.title`.
 */
enum class LifeSkill(val key: String) {
    Alchemy("alchemy"),
    Barter("barter"),
    Processing("processing"),
    Cooking("cooking"),
    Hunting("hunting"),
    Fishing("fishing"),
    Gathering("gathering"),
    Sailing("sailing"),
    Trading("trading"),
    Training("training"),
    Farming("farming"),
    ;

    companion object {
        fun fromKey(key: String): LifeSkill? = entries.firstOrNull { it.key == key }
    }
}
