package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboRage
import com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Файл комбо: версия формата, список комбо и настройки оверлея. */
@Serializable
private class ComboFileDto(val version: Int = FORMAT_VERSION, val combos: List<ComboDto> = emptyList(), val settings: SettingsDto = SettingsDto())

/** Настройки оверлея в файле. */
@Serializable
private class SettingsDto(
    val slotTooltips: Boolean = true,
    val comboScale: Int = 100,
    val comboOpacity: Int = 100,
    val schemaShown: Boolean = false,
    val slotShields: Boolean = false,
    val slotControls: Boolean = false,
    val trackerNoticeSeen: Boolean = false,
    val resetSeconds: Int = 5,
)

/** Комбо в файле; колонка — либо `cell`, либо `lanes`. */
@Serializable
private class ComboDto(
    val id: String,
    val name: String,
    val classKey: String,
    val columns: List<ColumnDto> = emptyList(),
    val fillers: List<Int> = emptyList(),
    val pool: List<Int> = emptyList(),
    val keyTracked: List<Int> = emptyList(),
    val vertical: Boolean = false,
    val overlayStyle: String = OverlayStyle.Next.name,
    val rage: RageDto? = null,
)

/** Комбо под гневом Чёрного духа в файле. */
@Serializable
private class RageDto(val columns: List<ColumnDto> = emptyList(), val fillers: List<Int> = emptyList(), val pool: List<Int> = emptyList())

/** Колонка в файле: обычная — [cell], развилка — [lanes]. */
@Serializable
private class ColumnDto(val cell: CellDto? = null, val lanes: List<List<CellDto>>? = null)

/** Ячейка в файле; поля по умолчанию не пишутся. [optional] — только в старых файлах: такие ячейки переезжают в пул ([withoutOptional]). */
@Serializable
private class CellDto(val skill: Int? = null, val chain: Boolean = false, val optional: Boolean = false, val note: String = "", val keys: List<String>? = null)

private const val FORMAT_VERSION = 1
private val json = Json { ignoreUnknownKeys = true }

/** Читает файл комбо; неверный файл — исключение `SerializationException` или `IllegalArgumentException`. */
internal fun parseCombos(text: String): List<Combo> = json.decodeFromString<ComboFileDto>(text).combos.map { dto ->
    Combo(
        id = dto.id,
        name = dto.name,
        classKey = dto.classKey,
        columns = dto.columns.withoutOptional(),
        fillers = dto.fillers,
        pool = (dto.pool + dto.columns.optionalSkills()).distinct(),
        keyTracked = dto.keyTracked.toSet(),
        vertical = dto.vertical,
        overlayStyle = OverlayStyle.entries.firstOrNull { it.name == dto.overlayStyle } ?: OverlayStyle.Next,
        rage = dto.rage?.let { rage -> ComboRage(rage.columns.withoutOptional(), rage.fillers, (rage.pool + rage.columns.optionalSkills()).distinct()) },
    )
}

/** Настройки оверлея из файла комбо; неверный файл — исключение, как у [parseCombos]. */
internal fun parseSettings(text: String): OverlaySettings =
    json.decodeFromString<ComboFileDto>(text).settings.let { OverlaySettings(it.slotTooltips, it.comboScale, it.comboOpacity, it.schemaShown, it.slotShields, it.slotControls, it.trackerNoticeSeen, it.resetSeconds) }

/** Записывает комбо [combos] и настройки [settings] в формат файла. */
internal fun combosJson(combos: List<Combo>, settings: OverlaySettings = OverlaySettings()): String = json.encodeToString(
    ComboFileDto.serializer(),
    ComboFileDto(
        combos = combos.map { combo ->
            ComboDto(
                id = combo.id,
                name = combo.name,
                classKey = combo.classKey,
                columns = combo.columns.map { it.toDto() },
                fillers = combo.fillers,
                pool = combo.pool,
                keyTracked = combo.keyTracked.sorted(),
                vertical = combo.vertical,
                overlayStyle = combo.overlayStyle.name,
                rage = combo.rage?.let { rage -> RageDto(rage.columns.map { it.toDto() }, rage.fillers, rage.pool) },
            )
        },
        settings = settings.let { SettingsDto(it.slotTooltips, it.comboScale, it.comboOpacity, it.schemaShown, it.slotShields, it.slotControls, it.trackerNoticeSeen, it.resetSeconds) },
    ),
)

/**
 * Колонки без опциональных ячеек (их больше нет — вместо них пул): одиночная пропадает, из развилки ячейка уходит,
 * пустая ветка — тоже; от развилки с одной ячейкой остаётся обычная колонка.
 */
private fun List<ColumnDto>.withoutOptional(): List<ComboColumn> = mapNotNull { column ->
    val lanes = column.lanes
    if (lanes == null) {
        val cell = column.cell
        return@mapNotNull if (cell?.optional == true) null else ComboColumn.Single(cell?.toCell() ?: ComboCell())
    }
    val kept = lanes.map { lane -> lane.filter { !it.optional }.map { it.toCell() } }.filter { it.isNotEmpty() }
    when {
        kept.isEmpty() -> null
        kept.size == 1 && kept[0].size == 1 -> ComboColumn.Single(kept[0][0])
        else -> ComboColumn.Fork(kept)
    }
}

/** Умения опциональных ячеек старого файла: они переезжают в пул. */
private fun List<ColumnDto>.optionalSkills(): List<Int> =
    flatMap { column -> column.lanes?.flatten() ?: listOfNotNull(column.cell) }.filter { it.optional }.mapNotNull { it.skill }

private fun ComboColumn.toDto(): ColumnDto = when (this) {
    is ComboColumn.Single -> ColumnDto(cell = cell.toDto())
    is ComboColumn.Fork -> ColumnDto(lanes = lanes.map { lane -> lane.map { it.toDto() } })
}

private fun CellDto.toCell() = ComboCell(skill, chain, note, keys?.toSet())
private fun ComboCell.toDto() = CellDto(skillNo, chain, note = note, keys = keys?.toList())
