package com.layzen.bdotools.feature.uieditor.data.skill

import com.layzen.bdotools.feature.uieditor.domain.skill.SkillDetails
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Текст `skills/<SkillNo>.json` на одном языке — поля `database.getSkill` bdolytics; нулевые и пустые не пишутся. */
@Serializable
private class SkillTextDto(
    val name: String = "",
    val description: String = "",
    val requiredSkillPoints: Int = 0,
    val requiredLevel: Int = 0,
    val requiredMp: Int = 0,
    val requiredSp: Int = 0,
    val cooldown: Long = 0,
    val command: String = "",
    val tooltip: List<List<String>> = emptyList(),
)

/** Файл `skills/<SkillNo>.json` сайта данных: текст на каждом языке. */
@Serializable
private class SkillDetailsDto(val ru: SkillTextDto, val en: SkillTextDto)

private val json = Json { ignoreUnknownKeys = true }

/** Читает файл подробностей навыка с сайта данных. */
internal fun parseSkillDetails(text: String): SkillDetails {
    val file = json.decodeFromString<SkillDetailsDto>(text)
    return SkillDetails(mapOf("ru" to file.ru.toText(), "en" to file.en.toText()))
}

private fun SkillTextDto.toText() = SkillText(
    name = name,
    description = description,
    requiredSkillPoints = requiredSkillPoints,
    requiredLevel = requiredLevel,
    requiredMp = requiredMp,
    requiredSp = requiredSp,
    cooldownMs = cooldown,
    command = command,
    effects = tooltip,
)
