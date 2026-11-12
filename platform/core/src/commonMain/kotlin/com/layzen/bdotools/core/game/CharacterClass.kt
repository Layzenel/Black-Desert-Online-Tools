package com.layzen.bdotools.core.game

/**
 * Класс персонажа Black Desert Online. [key] попадает в файлы тулз, поэтому после выпуска его не меняют.
 * Иконки и названия классов — в дизайн-системе: `CharacterClass.icon` и `CharacterClass.title`.
 * [ascension] — класс «Освобождения»: без стоек наследия и пробуждения, все навыки в одном наборе.
 */
enum class CharacterClass(val key: String, val ascension: Boolean = false) {
    Agent("agent"),
    Valkyrie("valkyrie"),
    Berserker("berserker"),
    Warrior("warrior"),
    Wizard("wizard"),
    Sorceress("sorceress"),
    Wukong("wukong", ascension = true),
    Drakania("drakania"),
    Witch("witch"),
    Corsair("corsair"),
    Kunoichi("kunoichi"),
    Lahn("lahn"),
    Archer("archer", ascension = true),
    Ranger("ranger"),
    Musa("musa"),
    Maehwa("maehwa"),
    Deadeye("deadeye", ascension = true),
    Mystic("mystic"),
    Sage("sage"),
    Maegu("maegu"),
    Ninja("ninja"),
    Nova("nova"),
    Seraph("seraph", ascension = true),
    Scholar("scholar", ascension = true),
    Guardian("guardian"),
    Striker("striker"),
    DarkKnight("dark-knight"),
    Dosa("dosa"),
    Woosa("woosa"),
    Fury("fury"),
    Hashashin("hashashin"),
    Shai("shai", ascension = true),
    ;

    companion object {
        fun fromKey(key: String): CharacterClass? = entries.firstOrNull { it.key == key }
    }
}
