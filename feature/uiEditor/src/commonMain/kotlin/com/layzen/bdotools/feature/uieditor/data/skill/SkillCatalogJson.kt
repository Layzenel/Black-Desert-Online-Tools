package com.layzen.bdotools.feature.uieditor.data.skill

import com.layzen.bdotools.feature.uieditor.domain.skill.Skill
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillCatalog
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillStance
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Запись `skills.json`: формат описан в README репозитория `Layzenel/bdo-tools-data`. Поля со значением по
 * умолчанию (`stance` = `both`, `max` = `false`) в файл не пишутся.
 */
@Serializable
private class SkillEntryDto(
    val classes: List<String>,
    val stance: String = "both",
    val max: Boolean = false,
    val en: String,
    val ru: String,
    val icon: String,
    val primeVariant: Boolean = false,
)

private val json = Json { ignoreUnknownKeys = true }

/** Читает `skills.json`; записи с нечисловым ключом пропускаются. */
internal fun parseSkillCatalog(text: String): SkillCatalog {
    val entries = json.decodeFromString<Map<String, SkillEntryDto>>(text)
    return SkillCatalog(
        entries.mapNotNull { (id, entry) ->
            val skillNo = id.toIntOrNull() ?: return@mapNotNull null
            skillNo to Skill(
                classes = entry.classes.toSet(),
                names = mapOf("en" to entry.en, "ru" to entry.ru),
                icon = entry.icon,
                stance = stanceOf(entry.stance),
                max = entry.max,
                hasPrimeVariant = entry.primeVariant,
            )
        }.toMap(),
    )
}

private fun stanceOf(value: String): SkillStance? = when (value) {
    "succession" -> SkillStance.Succession
    "awakening" -> SkillStance.Awakening
    else -> null
}
