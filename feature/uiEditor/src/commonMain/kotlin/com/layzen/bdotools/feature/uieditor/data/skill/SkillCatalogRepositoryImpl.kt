package com.layzen.bdotools.feature.uieditor.data.skill

import com.layzen.bdotools.feature.uieditor.domain.skill.SkillCatalog
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillCatalogRepository
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillDetails
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException

/** Сайт данных: справочник и иконки собирает и выкладывает репозиторий `Layzenel/bdo-tools-data` каждый день. */
private const val DATA_SITE = "https://layzenel.github.io/bdo-tools-data"

/**
 * Справочник навыков и иконки с сайта данных. Справочник скачивается один раз за запуск, иконки запоминаются
 * в памяти; между запусками их кэширует браузер (HTTP-кэш), а на Desktop — [downloads] с копиями на диске.
 *
 * @property downloads HTTP своей платформы
 */
class SkillCatalogRepositoryImpl(private val downloads: SkillDownloads) : SkillCatalogRepository {

    private val catalogLock = Mutex()
    private var cachedCatalog: SkillCatalog? = null
    private val icons = mutableMapOf<String, ByteArray>()
    private val details = mutableMapOf<Int, SkillDetails>()

    override suspend fun catalog(): SkillCatalog? = catalogLock.withLock {
        cachedCatalog ?: downloads.text("$DATA_SITE/skills.json")?.let { parseOrNull(it, ::parseSkillCatalog) }?.also { cachedCatalog = it }
    }

    override suspend fun icon(path: String): ByteArray? =
        icons[path] ?: downloads.bytes(DATA_SITE + path)?.also { icons[path] = it }

    override suspend fun details(skillNo: Int): SkillDetails? =
        details[skillNo] ?: downloads.text("$DATA_SITE/skills/$skillNo.json")
            ?.let { parseOrNull(it, ::parseSkillDetails) }
            ?.also { details[skillNo] = it }

    private fun <T> parseOrNull(text: String, parse: (String) -> T): T? = try {
        parse(text)
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}
