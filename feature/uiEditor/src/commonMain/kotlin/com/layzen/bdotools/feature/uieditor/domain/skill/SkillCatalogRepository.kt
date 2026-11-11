package com.layzen.bdotools.feature.uieditor.domain.skill

/**
 * Справочник навыков и их иконки с сайта данных `bdo-tools-data`. Сеть бывает недоступна: тогда методы
 * возвращают `null`, а редактор работает без названий и иконок.
 */
interface SkillCatalogRepository {

    /** Справочник навыков или `null`, если его не удалось скачать. Скачивается один раз за запуск. */
    suspend fun catalog(): SkillCatalog?

    /** Байты иконки по пути [Skill.icon] или `null`, если её не удалось скачать. */
    suspend fun icon(path: String): ByteArray?

    /** Подробности навыка [skillNo] для тултипа или `null`, если их нет или не удалось скачать. */
    suspend fun details(skillNo: Int): SkillDetails?
}
