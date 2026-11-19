package com.layzen.bdotools.core.skill.domain

/**
 * Контроль, который умение накладывает при попадании. [tooltipKey] — с чего начинается его строка в английском
 * тултипе («Knockdown on hits»): английский текст одинаков для игроков на любом языке.
 */
enum class CrowdControl(val tooltipKey: String) {
    Stun("Stun"),
    Knockdown("Knockdown"),
    Bound("Bound"),
    Stiffness("Stiffness"),
    Floating("Floating"),
    Knockback("Knockback"),
    Freezing("Freezing"),
    ;

    companion object {
        private const val RESISTANCE = "Resist"
        private const val PVE_ONLY = "PvE only"

        /** Контроль строки эффекта английского тултипа без разметки, или `null`; «Stun Resistance» — не контроль умения. */
        fun ofLine(line: String): CrowdControl? =
            if (line.contains(RESISTANCE, ignoreCase = true)) null else entries.firstOrNull { line.startsWith(it.tooltipKey + " ") }

        /** Строка действует только на монстров: пометка «(PvE only)». */
        fun isPveOnly(line: String): Boolean = line.contains(PVE_ONLY, ignoreCase = true)

        /** Контроли, которые умение накладывает в PvP, по строкам эффектов английского тултипа [lines] (с разметкой). */
        fun pvpOf(lines: List<String>): Set<CrowdControl> =
            lines.map { it.replace(SKILL_TAG, "").trim() }.filterNot(::isPveOnly).mapNotNullTo(mutableSetOf(), ::ofLine)

        private val SKILL_TAG = Regex("<[^>]*>")
    }
}
