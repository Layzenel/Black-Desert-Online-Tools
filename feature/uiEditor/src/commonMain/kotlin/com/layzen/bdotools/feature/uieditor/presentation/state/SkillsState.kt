package com.layzen.bdotools.feature.uieditor.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.core.account.CharacterLabel
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.skill.CharacterSkills
import com.layzen.bdotools.feature.uieditor.domain.skill.KeyBindings
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillDetails
import com.layzen.bdotools.feature.uieditor.domain.skill.Skill
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillCatalog

/**
 * Персонажи для навыков в слотах перезарядки, справочник и картинки. Сами навыки и надписи редактора лежат
 * в раскладке ([SlotsState.layout]); здесь — их состояние в файле персонажа.
 *
 * @property characters персонажи открытого аккаунта в порядке окна выбора персонажа, как в их файлах
 * @property selected `CharacterNo` персонажа, чьи навыки в редакторе; `null` — «Не выбран»
 * @property catalog справочник навыков; `null`, пока не скачан или если сеть недоступна
 * @property catalogFailed справочник не скачался: навыки видны номерами, без названий и иконок
 * @property icons иконки по пути [Skill.icon]; иконки, которые ещё грузятся или не скачались, здесь нет
 * @property portraits уменьшенные портреты из `FaceTexture` по `CharacterNo`; у персонажа без портрета — нет
 * @property details подробности навыков для тултипа по `SkillNo`; скачиваются, когда тултип нужен
 * @property detailsFailed навыки, подробности которых не скачались
 * @property labels подписи персонажей из «Конструктора портретов» по `CharacterNo`: подпись и класс со значка
 * @property alsoApplyTo кому ещё «Применить» запишет навыки и надписи редактора, кроме [selected] (`CharacterNo`): импорт сразу
 *   нескольким персонажам; сбрасывается при выборе другого персонажа и после записи
 */
data class SkillsState(
    val characters: List<CharacterSkills> = emptyList(),
    val selected: String? = null,
    val catalog: SkillCatalog? = null,
    val catalogFailed: Boolean = false,
    val icons: Map<String, ImageBitmap> = emptyMap(),
    val portraits: Map<String, ImageBitmap> = emptyMap(),
    val details: Map<Int, SkillDetails> = emptyMap(),
    val detailsFailed: Set<Int> = emptySet(),
    val labels: Map<String, CharacterLabel> = emptyMap(),
    val alsoApplyTo: Set<String> = emptySet(),
) {
    /** Файл выбранного персонажа или `null`. */
    val selectedCharacter: CharacterSkills? get() = characters.firstOrNull { it.characterNo == selected }

    /** Клавиши для команд навыков: выбранного персонажа, а без него — клавиши игры по умолчанию. */
    val keyBindings: KeyBindings
        get() = selectedCharacter?.keys?.takeIf { it.keyCodes.isNotEmpty() } ?: KeyBindings.Default

    /**
     * Класс персонажа (ключ `CharacterClass.key`): по навыкам в его файле, а если по ним не понять (слоты пустые) —
     * по значку класса на его портрете в «Конструкторе портретов». `null` — не определить.
     */
    fun classOf(character: CharacterSkills): String? =
        catalog?.classOf(character.slots.values) ?: labels[character.characterNo]?.classKey

    /**
     * Подходит ли персонаж для навыков класса [classKey] из импорта: тот же класс или слоты пустые (класс по навыкам
     * не узнать, решает пользователь). Класс импорта не определить (`null`) — подходят все.
     */
    fun fitsClass(character: CharacterSkills, classKey: String?): Boolean =
        classKey == null || character.slots.isEmpty() || classOf(character) == classKey

    /**
     * Кому «Применить» может записать навыки и надписи раскладки [layout]: выбранному персонажу и всем из [alsoApplyTo],
     * если у них в файле не то же самое.
     */
    fun skillTargets(layout: SlotLayout): List<CharacterSkills> =
        characters.filter { (it.characterNo == selected || it.characterNo in alsoApplyTo) && layout.skillsDifferFrom(it) }

    /** Навык из справочника или `null`. */
    fun skill(skillNo: Int): Skill? = catalog?.get(skillNo)

    /** Иконка навыка [skillNo], если она уже скачана. */
    fun iconOf(skillNo: Int): ImageBitmap? = skill(skillNo)?.let { icons[it.icon] }
}
