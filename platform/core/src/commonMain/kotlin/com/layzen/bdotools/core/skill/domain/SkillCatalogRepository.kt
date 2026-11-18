package com.layzen.bdotools.core.skill.domain

/**
 * Справочник навыков и их иконки с сайта данных `bdo-tools-data`. Сеть бывает недоступна: тогда методы
 * возвращают `null`, а редактор работает без названий и иконок.
 */
interface SkillCatalogRepository {

    /** Справочник навыков или `null`, если его не удалось скачать. Скачивается один раз за запуск. */
    suspend fun catalog(): SkillCatalog?

    /**
     * Все активные навыки: [catalog] вместе с навыками без перезарядки («Flow:», удары с движением) — их тоже пишет
     * история умений игры, поэтому они нужны комбо. Навыков без перезарядки может не быть, если их файл не
     * скачался; `null` — не скачался и основной справочник.
     */
    suspend fun activeSkills(): SkillCatalog?

    /** Байты иконки по пути [Skill.icon] или `null`, если её не удалось скачать. */
    suspend fun icon(path: String): ByteArray?

    /** Подробности навыка [skillNo] для тултипа или `null`, если их нет или не удалось скачать. */
    suspend fun details(skillNo: Int): SkillDetails?
}
