package com.layzen.bdotools.feature.uieditor.domain.skill

/**
 * Эффекты защиты и контроли навыка — то, что видно на плашке у навыка. Каждый тип — один раз и в порядке своего
 * `enum`, даже если в тултипе он встречается в нескольких строках («Устойчивость → Невредимость → Устойчивость»).
 *
 * @property defenses эффекты защиты во время навыка
 * @property controls контроли при попадании
 */
data class SkillEffects(val defenses: List<SkillDefense>, val controls: List<SkillControl>) {

    /** Контроли, которые действуют и в PvP. */
    val pvpControls: List<SkillControl> get() = controls.filterNot { it.pveOnly }

    companion object {
        private const val PVE_ONLY_MARK = "PvE only"
        private const val RESISTANCE = "Resist"
        private val tags = Regex("<[^>]*>")

        /**
         * Эффекты из английского тултипа [details]. Строка контроля начинается с его названия («Bound on hits»),
         * строка защиты — с эффекта защиты, в ней их бывает несколько («Invincible -> Super Armor during the skill»).
         * Нет английского текста — эффектов нет.
         */
        fun of(details: SkillDetails): SkillEffects {
            val lines = details.texts[TOOLTIP_LANGUAGE]?.effects.orEmpty()
                .map { parts -> parts.joinToString("").replace(tags, "").trim() }
            return SkillEffects(defensesOf(lines), controlsOf(lines))
        }

        private fun defensesOf(lines: List<String>): List<SkillDefense> {
            val defenseLines = lines.indices.filter { i -> DefenseEffect.entries.any { lines[i].startsWith(it.tooltipKey, ignoreCase = true) } }
            return DefenseEffect.entries.mapNotNull { effect ->
                val effectLines = defenseLines.filter { lines[it].contains(effect.tooltipKey, ignoreCase = true) }
                if (effectLines.isEmpty()) null else SkillDefense(effect, effectLines)
            }
        }

        private fun controlsOf(lines: List<String>): List<SkillControl> = ControlType.entries.mapNotNull { type ->
            // «Stun Resistance» и подобное — не контроль навыка
            val typeLines = lines.indices.filter { lines[it].startsWith(type.tooltipKey + " ") && !lines[it].contains(RESISTANCE, ignoreCase = true) }
            if (typeLines.isEmpty()) return@mapNotNull null
            SkillControl(type, pveOnly = typeLines.all { lines[it].contains(PVE_ONLY_MARK, ignoreCase = true) }, typeLines)
        }

        /** Названия эффектов в тултипе ищутся на английском: так они пишутся одинаково у всех навыков. */
        private const val TOOLTIP_LANGUAGE = "en"
    }
}
