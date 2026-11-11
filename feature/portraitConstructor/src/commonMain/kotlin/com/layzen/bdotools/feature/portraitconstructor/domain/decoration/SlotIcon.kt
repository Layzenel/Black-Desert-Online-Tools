package com.layzen.bdotools.feature.portraitconstructor.domain.decoration

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.game.LifeSkill

/**
 * Значок на портрете: мировой босс, жизненный навык или класс персонажа.
 * [key] пишется в файл украшений, поэтому после выпуска его не меняют, и ключи разных видов не пересекаются.
 */
sealed interface SlotIcon {
    /** Стабильный ключ значка для файла украшений. */
    val key: String

    /** Персонаж для мировых боссов. */
    data object WorldBoss : SlotIcon {
        override val key: String = "world-boss"
    }

    data class Skill(val skill: LifeSkill) : SlotIcon {
        override val key: String get() = skill.key
    }

    data class Class(val characterClass: CharacterClass) : SlotIcon {
        override val key: String get() = CLASS_PREFIX + characterClass.key
    }

    companion object {
        private const val CLASS_PREFIX = "class-"

        /** Ключи значков из прошлых версий: им соответствуют навыки. */
        private val LEGACY_SKILL_KEYS = mapOf(
            "fisher" to LifeSkill.Fishing,
            "gatherer" to LifeSkill.Gathering,
            "sailor" to LifeSkill.Sailing,
            "horse-breeder" to LifeSkill.Training,
        )

        fun fromKey(key: String): SlotIcon? =
            when {
                key == WorldBoss.key -> WorldBoss
                key.startsWith(CLASS_PREFIX) -> CharacterClass.fromKey(key.removePrefix(CLASS_PREFIX))?.let(::Class)
                else -> (LifeSkill.fromKey(key) ?: LEGACY_SKILL_KEYS[key])?.let(::Skill)
            }
    }
}
