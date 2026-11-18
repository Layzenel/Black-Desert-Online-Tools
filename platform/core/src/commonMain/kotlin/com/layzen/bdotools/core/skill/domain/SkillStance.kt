package com.layzen.bdotools.core.skill.domain

/** Стойка персонажа в игре: от неё зависит, какие навыки ему доступны. */
enum class SkillStance {
    /** Наследие: навыки «Prime:» основного оружия. */
    Succession,

    /** Пробуждение: навыки оружия пробуждения. */
    Awakening,
}
