package com.layzen.bdotools.uikit.icons

import com.layzen.bdotools.core.game.LifeSkill
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.life_skill_alchemy
import com.layzen.bdotools.uikit.resources.life_skill_barter
import com.layzen.bdotools.uikit.resources.life_skill_cooking
import com.layzen.bdotools.uikit.resources.life_skill_farming
import com.layzen.bdotools.uikit.resources.life_skill_fishing
import com.layzen.bdotools.uikit.resources.life_skill_gathering
import com.layzen.bdotools.uikit.resources.life_skill_hunting
import com.layzen.bdotools.uikit.resources.life_skill_processing
import com.layzen.bdotools.uikit.resources.life_skill_sailing
import com.layzen.bdotools.uikit.resources.life_skill_title_alchemy
import com.layzen.bdotools.uikit.resources.life_skill_title_barter
import com.layzen.bdotools.uikit.resources.life_skill_title_cooking
import com.layzen.bdotools.uikit.resources.life_skill_title_farming
import com.layzen.bdotools.uikit.resources.life_skill_title_fishing
import com.layzen.bdotools.uikit.resources.life_skill_title_gathering
import com.layzen.bdotools.uikit.resources.life_skill_title_hunting
import com.layzen.bdotools.uikit.resources.life_skill_title_processing
import com.layzen.bdotools.uikit.resources.life_skill_title_sailing
import com.layzen.bdotools.uikit.resources.life_skill_title_trading
import com.layzen.bdotools.uikit.resources.life_skill_title_training
import com.layzen.bdotools.uikit.resources.life_skill_trading
import com.layzen.bdotools.uikit.resources.life_skill_training
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * Иконка жизненного навыка: белый силуэт, `viewBox 0 0 64 64`. Цвет задаёт тот, кто рисует (`ColorFilter.tint`).
 * Новый навык: SVG в `composeResources/drawable`, строка в `strings.xml` и ветки здесь.
 */
val LifeSkill.icon: DrawableResource
    get() = when (this) {
        LifeSkill.Alchemy -> Res.drawable.life_skill_alchemy
        LifeSkill.Barter -> Res.drawable.life_skill_barter
        LifeSkill.Processing -> Res.drawable.life_skill_processing
        LifeSkill.Cooking -> Res.drawable.life_skill_cooking
        LifeSkill.Hunting -> Res.drawable.life_skill_hunting
        LifeSkill.Fishing -> Res.drawable.life_skill_fishing
        LifeSkill.Gathering -> Res.drawable.life_skill_gathering
        LifeSkill.Sailing -> Res.drawable.life_skill_sailing
        LifeSkill.Trading -> Res.drawable.life_skill_trading
        LifeSkill.Training -> Res.drawable.life_skill_training
        LifeSkill.Farming -> Res.drawable.life_skill_farming
    }

/** Название навыка для интерфейса. */
val LifeSkill.title: StringResource
    get() = when (this) {
        LifeSkill.Alchemy -> Res.string.life_skill_title_alchemy
        LifeSkill.Barter -> Res.string.life_skill_title_barter
        LifeSkill.Processing -> Res.string.life_skill_title_processing
        LifeSkill.Cooking -> Res.string.life_skill_title_cooking
        LifeSkill.Hunting -> Res.string.life_skill_title_hunting
        LifeSkill.Fishing -> Res.string.life_skill_title_fishing
        LifeSkill.Gathering -> Res.string.life_skill_title_gathering
        LifeSkill.Sailing -> Res.string.life_skill_title_sailing
        LifeSkill.Trading -> Res.string.life_skill_title_trading
        LifeSkill.Training -> Res.string.life_skill_title_training
        LifeSkill.Farming -> Res.string.life_skill_title_farming
    }
