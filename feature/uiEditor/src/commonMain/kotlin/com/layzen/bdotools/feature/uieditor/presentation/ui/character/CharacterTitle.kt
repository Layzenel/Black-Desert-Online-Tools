package com.layzen.bdotools.feature.uieditor.presentation.ui.character

import androidx.compose.runtime.Composable
import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.feature.uieditor.domain.skill.CharacterSkills
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_character_class_unknown
import com.layzen.bdotools.feature.uieditor.resources.slots_character_item
import com.layzen.bdotools.feature.uieditor.resources.slots_character_item_memo
import com.layzen.bdotools.uikit.icons.title
import org.jetbrains.compose.resources.stringResource

/**
 * Подпись персонажа в две строки: имён в файлах игры нет, поэтому сверху класс и в скобках подпись с его портрета
 * в «Конструкторе портретов» (если есть), снизу — сколько навыков в слотах перезарядки.
 */
@Composable
internal fun characterTitle(skills: SkillsState, character: CharacterSkills): String {
    val className = className(skills.classOf(character))
    val caption = skills.labels[character.characterNo]?.caption
        ?: return stringResource(Res.string.slots_character_item, className, character.slots.size)
    return stringResource(Res.string.slots_character_item_memo, className, caption, character.slots.size)
}

/** Название класса по ключу `CharacterClass.key`; неизвестный или новый класс — «класс не определён». */
@Composable
internal fun className(classKey: String?): String =
    classKey?.let(CharacterClass::fromKey)?.let { stringResource(it.title) }
        ?: stringResource(Res.string.slots_character_class_unknown)
