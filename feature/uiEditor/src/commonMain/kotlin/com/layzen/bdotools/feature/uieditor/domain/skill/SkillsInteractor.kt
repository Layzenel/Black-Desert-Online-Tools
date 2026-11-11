package com.layzen.bdotools.feature.uieditor.domain.skill

import com.layzen.bdotools.feature.uieditor.domain.game.SlotsRepository
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId

/**
 * Навыки персонажей в редакторе: что стоит в слотах перезарядки у каждого персонажа аккаунта, справочник навыков
 * и их иконки.
 *
 * @property slots файлы игры
 * @property catalog справочник навыков с сайта данных
 */
class SkillsInteractor(
    private val slots: SlotsRepository,
    private val catalog: SkillCatalogRepository,
) {
    /**
     * Персонажи аккаунта [account] с файлом, в порядке окна выбора персонажа [order] (`CharacterOrderList`).
     * Папки персонажей не из этого списка — удалённые персонажи и служебные папки игры — пропускаются;
     * если списка нет, берутся все папки по номеру.
     */
    suspend fun characters(account: String, order: List<String>): List<CharacterSkills> {
        val all = slots.characterSkills(account)
        if (order.isEmpty()) return all.sortedBy { it.characterNo }
        val byNumber = all.associateBy { it.characterNo }
        return order.mapNotNull { byNumber[it] }
    }

    /**
     * Записывает персонажу [characterNo] навыки слотов перезарядки [skills] (слоты редактора → `SkillNo`) и все его
     * надписи [memos]. Писать можно только при закрытой игре, как и остальные настройки.
     */
    suspend fun write(account: String, characterNo: String, skills: Map<SlotId, Int>, memos: Map<Int, String>) =
        slots.writeCharacterSkills(account, characterNo, slotNumbersOf(skills), memos)

    /** Справочник навыков или `null`, если сеть недоступна. */
    suspend fun catalog(): SkillCatalog? = catalog.catalog()

    /** Подробности навыка [skillNo] для тултипа или `null`, если их не удалось скачать. */
    suspend fun details(skillNo: Int): SkillDetails? = catalog.details(skillNo)

    /** Байты иконки навыка или `null`, если она не скачалась. */
    suspend fun icon(skill: Skill): ByteArray? = catalog.icon(skill.icon)
}
