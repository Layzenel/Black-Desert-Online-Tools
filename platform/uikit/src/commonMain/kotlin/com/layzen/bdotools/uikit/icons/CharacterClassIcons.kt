package com.layzen.bdotools.uikit.icons

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.character_class_agent
import com.layzen.bdotools.uikit.resources.character_class_archer
import com.layzen.bdotools.uikit.resources.character_class_berserker
import com.layzen.bdotools.uikit.resources.character_class_corsair
import com.layzen.bdotools.uikit.resources.character_class_dark_knight
import com.layzen.bdotools.uikit.resources.character_class_deadeye
import com.layzen.bdotools.uikit.resources.character_class_dosa
import com.layzen.bdotools.uikit.resources.character_class_drakania
import com.layzen.bdotools.uikit.resources.character_class_fury
import com.layzen.bdotools.uikit.resources.character_class_guardian
import com.layzen.bdotools.uikit.resources.character_class_hashashin
import com.layzen.bdotools.uikit.resources.character_class_kunoichi
import com.layzen.bdotools.uikit.resources.character_class_lahn
import com.layzen.bdotools.uikit.resources.character_class_maegu
import com.layzen.bdotools.uikit.resources.character_class_maehwa
import com.layzen.bdotools.uikit.resources.character_class_musa
import com.layzen.bdotools.uikit.resources.character_class_mystic
import com.layzen.bdotools.uikit.resources.character_class_ninja
import com.layzen.bdotools.uikit.resources.character_class_nova
import com.layzen.bdotools.uikit.resources.character_class_ranger
import com.layzen.bdotools.uikit.resources.character_class_sage
import com.layzen.bdotools.uikit.resources.character_class_scholar
import com.layzen.bdotools.uikit.resources.character_class_seraph
import com.layzen.bdotools.uikit.resources.character_class_shai
import com.layzen.bdotools.uikit.resources.character_class_sorceress
import com.layzen.bdotools.uikit.resources.character_class_striker
import com.layzen.bdotools.uikit.resources.character_class_valkyrie
import com.layzen.bdotools.uikit.resources.character_class_warrior
import com.layzen.bdotools.uikit.resources.character_class_witch
import com.layzen.bdotools.uikit.resources.character_class_wizard
import com.layzen.bdotools.uikit.resources.character_class_woosa
import com.layzen.bdotools.uikit.resources.character_class_wukong
import com.layzen.bdotools.uikit.resources.class_agent
import com.layzen.bdotools.uikit.resources.class_archer
import com.layzen.bdotools.uikit.resources.class_berserker
import com.layzen.bdotools.uikit.resources.class_corsair
import com.layzen.bdotools.uikit.resources.class_dark_knight
import com.layzen.bdotools.uikit.resources.class_deadeye
import com.layzen.bdotools.uikit.resources.class_dosa
import com.layzen.bdotools.uikit.resources.class_drakania
import com.layzen.bdotools.uikit.resources.class_fury
import com.layzen.bdotools.uikit.resources.class_guardian
import com.layzen.bdotools.uikit.resources.class_hashashin
import com.layzen.bdotools.uikit.resources.class_kunoichi
import com.layzen.bdotools.uikit.resources.class_lahn
import com.layzen.bdotools.uikit.resources.class_maegu
import com.layzen.bdotools.uikit.resources.class_maehwa
import com.layzen.bdotools.uikit.resources.class_musa
import com.layzen.bdotools.uikit.resources.class_mystic
import com.layzen.bdotools.uikit.resources.class_ninja
import com.layzen.bdotools.uikit.resources.class_nova
import com.layzen.bdotools.uikit.resources.class_ranger
import com.layzen.bdotools.uikit.resources.class_sage
import com.layzen.bdotools.uikit.resources.class_scholar
import com.layzen.bdotools.uikit.resources.class_seraph
import com.layzen.bdotools.uikit.resources.class_shai
import com.layzen.bdotools.uikit.resources.class_sorceress
import com.layzen.bdotools.uikit.resources.class_striker
import com.layzen.bdotools.uikit.resources.class_valkyrie
import com.layzen.bdotools.uikit.resources.class_warrior
import com.layzen.bdotools.uikit.resources.class_witch
import com.layzen.bdotools.uikit.resources.class_wizard
import com.layzen.bdotools.uikit.resources.class_woosa
import com.layzen.bdotools.uikit.resources.class_wukong
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * Иконка класса: белый силуэт, `viewBox 0 0 64 64`. Цвет задаёт тот, кто рисует (`ColorFilter.tint`).
 * Новый класс: SVG в `composeResources/drawable`, строка в `strings.xml` и ветки здесь.
 */
val CharacterClass.icon: DrawableResource
    get() = when (this) {
        CharacterClass.Agent -> Res.drawable.class_agent
        CharacterClass.Valkyrie -> Res.drawable.class_valkyrie
        CharacterClass.Berserker -> Res.drawable.class_berserker
        CharacterClass.Warrior -> Res.drawable.class_warrior
        CharacterClass.Wizard -> Res.drawable.class_wizard
        CharacterClass.Sorceress -> Res.drawable.class_sorceress
        CharacterClass.Wukong -> Res.drawable.class_wukong
        CharacterClass.Drakania -> Res.drawable.class_drakania
        CharacterClass.Witch -> Res.drawable.class_witch
        CharacterClass.Corsair -> Res.drawable.class_corsair
        CharacterClass.Kunoichi -> Res.drawable.class_kunoichi
        CharacterClass.Lahn -> Res.drawable.class_lahn
        CharacterClass.Archer -> Res.drawable.class_archer
        CharacterClass.Ranger -> Res.drawable.class_ranger
        CharacterClass.Musa -> Res.drawable.class_musa
        CharacterClass.Maehwa -> Res.drawable.class_maehwa
        CharacterClass.Deadeye -> Res.drawable.class_deadeye
        CharacterClass.Mystic -> Res.drawable.class_mystic
        CharacterClass.Sage -> Res.drawable.class_sage
        CharacterClass.Maegu -> Res.drawable.class_maegu
        CharacterClass.Ninja -> Res.drawable.class_ninja
        CharacterClass.Nova -> Res.drawable.class_nova
        CharacterClass.Seraph -> Res.drawable.class_seraph
        CharacterClass.Scholar -> Res.drawable.class_scholar
        CharacterClass.Guardian -> Res.drawable.class_guardian
        CharacterClass.Striker -> Res.drawable.class_striker
        CharacterClass.DarkKnight -> Res.drawable.class_dark_knight
        CharacterClass.Dosa -> Res.drawable.class_dosa
        CharacterClass.Woosa -> Res.drawable.class_woosa
        CharacterClass.Fury -> Res.drawable.class_fury
        CharacterClass.Hashashin -> Res.drawable.class_hashashin
        CharacterClass.Shai -> Res.drawable.class_shai
    }

/** Название класса для интерфейса. */
val CharacterClass.title: StringResource
    get() = when (this) {
        CharacterClass.Agent -> Res.string.character_class_agent
        CharacterClass.Valkyrie -> Res.string.character_class_valkyrie
        CharacterClass.Berserker -> Res.string.character_class_berserker
        CharacterClass.Warrior -> Res.string.character_class_warrior
        CharacterClass.Wizard -> Res.string.character_class_wizard
        CharacterClass.Sorceress -> Res.string.character_class_sorceress
        CharacterClass.Wukong -> Res.string.character_class_wukong
        CharacterClass.Drakania -> Res.string.character_class_drakania
        CharacterClass.Witch -> Res.string.character_class_witch
        CharacterClass.Corsair -> Res.string.character_class_corsair
        CharacterClass.Kunoichi -> Res.string.character_class_kunoichi
        CharacterClass.Lahn -> Res.string.character_class_lahn
        CharacterClass.Archer -> Res.string.character_class_archer
        CharacterClass.Ranger -> Res.string.character_class_ranger
        CharacterClass.Musa -> Res.string.character_class_musa
        CharacterClass.Maehwa -> Res.string.character_class_maehwa
        CharacterClass.Deadeye -> Res.string.character_class_deadeye
        CharacterClass.Mystic -> Res.string.character_class_mystic
        CharacterClass.Sage -> Res.string.character_class_sage
        CharacterClass.Maegu -> Res.string.character_class_maegu
        CharacterClass.Ninja -> Res.string.character_class_ninja
        CharacterClass.Nova -> Res.string.character_class_nova
        CharacterClass.Seraph -> Res.string.character_class_seraph
        CharacterClass.Scholar -> Res.string.character_class_scholar
        CharacterClass.Guardian -> Res.string.character_class_guardian
        CharacterClass.Striker -> Res.string.character_class_striker
        CharacterClass.DarkKnight -> Res.string.character_class_dark_knight
        CharacterClass.Dosa -> Res.string.character_class_dosa
        CharacterClass.Woosa -> Res.string.character_class_woosa
        CharacterClass.Fury -> Res.string.character_class_fury
        CharacterClass.Hashashin -> Res.string.character_class_hashashin
        CharacterClass.Shai -> Res.string.character_class_shai
    }
