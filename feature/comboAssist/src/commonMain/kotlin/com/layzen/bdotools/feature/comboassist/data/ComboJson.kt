package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
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
)

/** Комбо в файле; колонка — либо `cell`, либо `lanes`. */
@Serializable
private class ComboDto(
    val id: String,
    val name: String,
    val classKey: String,
    val columns: List<ColumnDto> = emptyList(),
    val fillers: List<Int> = emptyList(),
    val vertical: Boolean = false,
    val overlayStyle: String = OverlayStyle.Next.name,
)

/** Колонка в файле: обычная — [cell], развилка — [lanes]; [linked] — ветка как в предыдущей развилке. */
@Serializable
private class ColumnDto(val cell: CellDto? = null, val lanes: List<List<CellDto>>? = null, val linked: Boolean = false)

/** Ячейка в файле; поля по умолчанию не пишутся. */
@Serializable
private class CellDto(val skill: Int? = null, val chain: Boolean = false, val optional: Boolean = false, val note: String = "")

private const val FORMAT_VERSION = 1
private val json = Json { ignoreUnknownKeys = true }

/** Читает файл комбо; неверный файл — исключение `SerializationException` или `IllegalArgumentException`. */
internal fun parseCombos(text: String): List<Combo> = json.decodeFromString<ComboFileDto>(text).combos.map { dto ->
    Combo(
        id = dto.id,
        name = dto.name,
        classKey = dto.classKey,
        columns = dto.columns.map { column ->
            column.lanes?.let { lanes -> ComboColumn.Fork(lanes.map { lane -> lane.map { it.toCell() } }, column.linked) }
                ?: ComboColumn.Single(column.cell?.toCell() ?: ComboCell())
        },
        fillers = dto.fillers,
        vertical = dto.vertical,
        overlayStyle = OverlayStyle.entries.firstOrNull { it.name == dto.overlayStyle } ?: OverlayStyle.Next,
    )
}

/** Настройки оверлея из файла комбо; неверный файл — исключение, как у [parseCombos]. */
internal fun parseSettings(text: String): OverlaySettings =
    json.decodeFromString<ComboFileDto>(text).settings.let { OverlaySettings(it.slotTooltips, it.comboScale, it.comboOpacity, it.schemaShown, it.slotShields) }

/** Записывает комбо [combos] и настройки [settings] в формат файла. */
internal fun combosJson(combos: List<Combo>, settings: OverlaySettings = OverlaySettings()): String = json.encodeToString(
    ComboFileDto.serializer(),
    ComboFileDto(
        combos = combos.map { combo ->
            ComboDto(
                id = combo.id,
                name = combo.name,
                classKey = combo.classKey,
                columns = combo.columns.map { column ->
                    when (column) {
                        is ComboColumn.Single -> ColumnDto(cell = column.cell.toDto())
                        is ComboColumn.Fork -> ColumnDto(lanes = column.lanes.map { lane -> lane.map { it.toDto() } }, linked = column.linked)
                    }
                },
                fillers = combo.fillers,
                vertical = combo.vertical,
                overlayStyle = combo.overlayStyle.name,
            )
        },
        settings = settings.let { SettingsDto(it.slotTooltips, it.comboScale, it.comboOpacity, it.schemaShown, it.slotShields) },
    ),
)

private fun CellDto.toCell() = ComboCell(skill, chain, optional, note)
private fun ComboCell.toDto() = CellDto(skillNo, chain, optional, note)
