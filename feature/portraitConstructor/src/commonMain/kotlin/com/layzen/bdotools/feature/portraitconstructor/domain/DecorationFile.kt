package com.layzen.bdotools.feature.portraitconstructor.domain

private const val CAPTION_KEY = "caption"
private const val BADGE_KEY = "badge"

/** Ключи мест из прошлых версий, где значков на портрете было несколько. */
private val LEGACY_BADGE_KEYS = setOf("top", "left", "right", "left-top", "left-bottom", "right-top", "right-bottom")

/**
 * Файл украшений — строки `ключ=значение`: `caption=Рыбак`, `badge=class-warrior`.
 * Простой формат, чтобы файл можно было открыть и поправить руками.
 */
fun encodeDecoration(decoration: PortraitDecoration): String = buildString {
    if (decoration.caption.isNotBlank()) appendLine("$CAPTION_KEY=${decoration.caption.trim()}")
    decoration.badge?.let { appendLine("$BADGE_KEY=${it.key}") }
}

/**
 * Читает файл украшений; неизвестные ключи и значки пропускаются.
 * Из старых файлов с несколькими местами остаётся один значок: класс, а если его нет — первый найденный.
 */
fun decodeDecoration(text: String): PortraitDecoration =
    text.lineSequence()
        .map { it.trimEnd('\r') }
        .filter { '=' in it }
        .fold(PortraitDecoration()) { decoration, line ->
            val key = line.substringBefore('=').trim()
            val value = line.substringAfter('=')
            val icon = SlotIcon.fromKey(value.trim())
            when {
                key == CAPTION_KEY -> decoration.withCaption(value)
                icon == null -> decoration
                key == BADGE_KEY -> decoration.withBadge(icon)
                key in LEGACY_BADGE_KEYS -> decoration.withLegacyBadge(icon)
                else -> decoration
            }
        }

private fun PortraitDecoration.withLegacyBadge(icon: SlotIcon): PortraitDecoration {
    val current = badge
    val keepCurrent = current is SlotIcon.Class || (current != null && icon !is SlotIcon.Class)
    return if (keepCurrent) this else withBadge(icon)
}
