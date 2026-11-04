package com.layzen.bdotools.update

/**
 * Версия настольной программы вида `1.0.57`: CI ставит `1.0.<номер сборки>`.
 * Сравнивается по числам слева направо, недостающие части считаются нулями.
 */
data class AppVersion(val parts: List<Int>) : Comparable<AppVersion> {

    override fun compareTo(other: AppVersion): Int {
        for (i in 0 until maxOf(parts.size, other.parts.size)) {
            val diff = parts.getOrElse(i) { 0 } - other.parts.getOrElse(i) { 0 }
            if (diff != 0) return diff
        }
        return 0
    }

    override fun toString(): String = parts.joinToString(".")

    companion object {
        /** `null`, если строка не похожа на версию. */
        fun parse(text: String): AppVersion? {
            val parts = text.trim().split('.').map { it.toIntOrNull() ?: return null }
            return if (parts.isEmpty()) null else AppVersion(parts)
        }
    }
}
