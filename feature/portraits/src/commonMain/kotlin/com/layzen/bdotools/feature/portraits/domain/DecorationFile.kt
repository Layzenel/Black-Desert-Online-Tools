package com.layzen.bdotools.feature.portraits.domain

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

/** Читает файл украшений; неизвестные ключи и значки пропускаются. */
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
