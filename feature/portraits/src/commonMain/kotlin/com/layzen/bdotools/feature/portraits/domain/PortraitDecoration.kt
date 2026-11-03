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

/** Надпись и значки, которые рисуются поверх чистого портрета. */
data class PortraitDecoration(
    val caption: String = "",
    val badges: Map<BadgeSlot, BadgeIcon> = emptyMap(),
) {
    val isEmpty: Boolean get() = caption.isBlank() && badges.isEmpty()

    fun withCaption(text: String) = copy(caption = normalizeCaption(text))

    fun withBadge(slot: BadgeSlot, icon: BadgeIcon?) =
        copy(badges = if (icon == null) badges - slot else badges + (slot to icon))

    companion object {
        const val CAPTION_MAX_LENGTH = 24
        const val CAPTION_MAX_LINES = 2

        /** Одна строка без лишних пробелов и не длиннее [CAPTION_MAX_LENGTH]. Переносы расставляет отрисовка. */
        fun normalizeCaption(text: String): String =
            text.replace(Regex("\\s+"), " ").trimStart().take(CAPTION_MAX_LENGTH)
    }
}

private const val CAPTION_KEY = "caption"

/**
 * Файл украшений — строки `ключ=значение`: `caption=Рыбак`, `top=fisher`, `left=gatherer`.
 * Простой формат, чтобы файл можно было открыть и поправить руками.
 */
fun encodeDecoration(decoration: PortraitDecoration): String = buildString {
    if (decoration.caption.isNotBlank()) appendLine("$CAPTION_KEY=${decoration.caption.trim()}")
    BadgeSlot.entries.forEach { slot ->
        decoration.badges[slot]?.let { appendLine("${slot.key}=${it.key}") }
    }
}

fun decodeDecoration(text: String): PortraitDecoration =
    text.lineSequence()
        .map { it.trimEnd('\r') }
        .filter { '=' in it }
        .fold(PortraitDecoration()) { decoration, line ->
            val key = line.substringBefore('=').trim()
            val value = line.substringAfter('=')
            val slot = BadgeSlot.fromKey(key)
            when {
                key == CAPTION_KEY -> decoration.withCaption(value)
                // из двух старых боковых значков остаётся верхний: нижний не перезаписывает занятое место
                slot != null && key.endsWith("-bottom") && slot in decoration.badges -> decoration
                slot != null -> decoration.withBadge(slot, BadgeIcon.fromKey(value.trim()))
                else -> decoration
            }
        }
