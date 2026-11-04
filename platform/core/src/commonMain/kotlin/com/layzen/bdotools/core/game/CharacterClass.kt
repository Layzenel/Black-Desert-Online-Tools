package com.layzen.bdotools.core.game

/**
 * Класс персонажа Black Desert Online. [key] попадает в файлы тулз, поэтому после выпуска его не меняют.
 * Иконки и названия классов — в дизайн-системе: `CharacterClass.icon` и `CharacterClass.title`.
 */
enum class CharacterClass(val key: String) {
    Agent("agent"),
    Valkyrie("valkyrie"),
    Berserker("berserker"),
    Warrior("warrior"),
    Wizard("wizard"),
    Sorceress("sorceress"),
    Wukong("wukong"),
    Drakania("drakania"),
    Witch("witch"),
    Corsair("corsair"),
    Kunoichi("kunoichi"),
    Lahn("lahn"),
    Archer("archer"),
    Ranger("ranger"),
    Musa("musa"),
    Maehwa("maehwa"),
    Deadeye("deadeye"),
    Mystic("mystic"),
    Sage("sage"),
    Maegu("maegu"),
    Ninja("ninja"),
    Nova("nova"),
    Seraph("seraph"),
    Scholar("scholar"),
    Guardian("guardian"),
    Striker("striker"),
    DarkKnight("dark-knight"),
    Dosa("dosa"),
    Woosa("woosa"),
    Fury("fury"),
    Hashashin("hashashin"),
    Shai("shai"),
    ;

    companion object {
        fun fromKey(key: String): CharacterClass? = entries.firstOrNull { it.key == key }
    }
}
