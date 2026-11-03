package com.layzen.bdotools.feature.portraits.domain

/** Значок на портрете. [key] пишется в файл украшений, поэтому после выпуска его не меняют. */
enum class BadgeIcon(val key: String) {
    Fisher("fisher"),
    HorseBreeder("horse-breeder"),
    Gatherer("gatherer"),
    Sailor("sailor"),
    Barter("barter"),
    ;

    companion object {
        fun fromKey(key: String): BadgeIcon? = entries.firstOrNull { it.key == key }
    }
}
