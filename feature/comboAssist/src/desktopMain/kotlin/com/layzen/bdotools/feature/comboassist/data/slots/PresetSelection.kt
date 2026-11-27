package com.layzen.bdotools.feature.comboassist.data.slots

import java.time.LocalDateTime
import java.time.ZoneId

/** Строка лога клиента о сохранении пресета интерфейса: дата, время и номер пресета. */
private val PRESET_LINE = Regex(""""Date":"(\d{4}-\d\d-\d\d) (\d\d:\d\d:\d\d)".*save UISetting PresetInfo preset No = (\d+)""")

/**
 * Последний пресет интерфейса, выбранный игроком, по строкам лога клиента.
 *
 * При загрузке (вход, смена персонажа, телепорт) игра пишет `save UISetting PresetInfo preset No = 0..3` для всех
 * пресетов в одну секунду, а при выборе пресета в «Изменении интерфейса» — только номер нового (с нуля). Поэтому выбор —
 * группа строк одной секунды с одним номером. Группа со всеми номерами — загрузка: она выбор не отменяет, но игра
 * тогда же пишет файл аккаунта, и его время становится позже выбора.
 */
internal class PresetSelection {
    private var groupTime: Long? = null
    private val groupPresets = mutableSetOf<Int>()
    private var finished: Pair<Long, Int>? = null

    /** Последний выбранный пресет: время выбора (мс, местное время лога) → номер, или `null`, если выбора не было. */
    val selected: Pair<Long, Int>?
        get() = groupPresets.singleOrNull()?.let { preset -> groupTime?.let { it to preset } } ?: finished

    /** Учитывает строку лога [line]; строки не о пресетах пропускаются. */
    fun feed(line: String) {
        val (date, time, preset) = PRESET_LINE.find(line)?.destructured ?: return
        val millis = LocalDateTime.parse("${date}T$time").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (millis != groupTime) {
            finished = selected
            groupTime = millis
            groupPresets.clear()
        }
        groupPresets += preset.toInt()
    }
}
