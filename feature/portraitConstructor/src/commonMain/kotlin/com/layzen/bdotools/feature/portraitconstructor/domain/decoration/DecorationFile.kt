package com.layzen.bdotools.feature.portraitconstructor.domain.decoration

private const val CAPTION_KEY = "caption"
private const val BADGE_KEY = "badge"
private const val CAPTION_COLOR_KEY = "caption-color"
private const val BADGE_COLOR_KEY = "badge-color"
private const val SHADE_COLOR_KEY = "shade-color"
private const val SHADE_STRENGTH_KEY = "shade-strength"
private const val SHADE_SIZE_KEY = "shade-size"
private const val HEX_RADIX = 16
private const val RGB_MASK = 0xFFFFFF
private const val RGB_DIGITS = 6

/** Ключи мест из прошлых версий, где значков на портрете было несколько. */
private val LEGACY_BADGE_KEYS = setOf("top", "left", "right", "left-top", "left-bottom", "right-top", "right-bottom")

/**
 * Файл украшений — строки `ключ=значение`: `caption=Рыбак`, `badge=class-warrior`, цвета `caption-color=#F8C038`,
 * `badge-color=#E0783C`, `shade-color=#101010`, сила `shade-strength=60` и область `shade-size=150` затенения
 * (только если не по умолчанию).
 * Простой формат, чтобы файл можно было открыть и поправить руками.
 */
fun encodeDecoration(decoration: PortraitDecoration): String = buildString {
    if (decoration.caption.isNotBlank()) appendLine("$CAPTION_KEY=${decoration.caption.trim()}")
    decoration.badge?.let { appendLine("$BADGE_KEY=${it.key}") }
    val style = decoration.style
    style.captionColor?.let { appendLine("$CAPTION_COLOR_KEY=${hexColor(it)}") }
    style.badgeColor?.let { appendLine("$BADGE_COLOR_KEY=${hexColor(it)}") }
    style.shadeColor?.let { appendLine("$SHADE_COLOR_KEY=${hexColor(it)}") }
    style.shadeStrength?.let { appendLine("$SHADE_STRENGTH_KEY=$it") }
    style.shadeSize?.let { appendLine("$SHADE_SIZE_KEY=$it") }
}

/**
 * Читает файл украшений; неизвестные ключи, значки и неверные значения пропускаются.
 * Из старых файлов с несколькими местами остаётся один значок: класс, а если его нет — первый найденный.
 */
fun decodeDecoration(text: String): PortraitDecoration =
    text.lineSequence()
        .map { it.trimEnd('\r') }
        .filter { '=' in it }
        .fold(PortraitDecoration()) { decoration, line ->
            val key = line.substringBefore('=').trim()
            val value = line.substringAfter('=')
            val style = decoration.style
            val icon = SlotIcon.fromKey(value.trim())
            when {
                key == CAPTION_KEY -> decoration.withCaption(value)
                key == CAPTION_COLOR_KEY -> decoration.copy(style = style.copy(captionColor = parseColor(value) ?: style.captionColor))
                key == BADGE_COLOR_KEY -> decoration.copy(style = style.copy(badgeColor = parseColor(value) ?: style.badgeColor))
                key == SHADE_COLOR_KEY -> decoration.copy(style = style.copy(shadeColor = parseColor(value) ?: style.shadeColor))
                key == SHADE_STRENGTH_KEY -> decoration.copy(style = style.copy(shadeStrength = parsePercent(value, DecorationStyle.SHADE_RANGE) ?: style.shadeStrength))
                key == SHADE_SIZE_KEY -> decoration.copy(style = style.copy(shadeSize = parsePercent(value, DecorationStyle.SHADE_SIZE_RANGE) ?: style.shadeSize))
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

/** Цвет `0xRRGGBB` строкой `#RRGGBB`. */
private fun hexColor(rgb: Int): String = "#" + (rgb and RGB_MASK).toString(HEX_RADIX).uppercase().padStart(RGB_DIGITS, '0')

/** `#RRGGBB` → `0xRRGGBB`; другой текст — `null`. */
private fun parseColor(text: String): Int? =
    text.trim().removePrefix("#").takeIf { it.length == RGB_DIGITS }?.toIntOrNull(HEX_RADIX)

/** Проценты в пределах [range]; другой текст — `null`. */
private fun parsePercent(text: String, range: IntRange): Int? = text.trim().toIntOrNull()?.takeIf { it in range }
