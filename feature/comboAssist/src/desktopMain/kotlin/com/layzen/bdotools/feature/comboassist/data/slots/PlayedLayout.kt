package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.feature.comboassist.data.history.IconMatcher
import java.awt.Robot

/**
 * Раскладка персонажа, которым сейчас играют. Сначала — по журналу игры ([CharacterJournal]): так персонаж известен,
 * даже если все его слоты скрыты (тогда слотов в раскладке нет). Журнала нет или персонаж не из выбранного аккаунта —
 * по слотам на экране ([PlayedCharacterDetector]).
 */
internal object PlayedLayout {
    /** Раскладка из [layouts] (по `CharacterNo`) персонажа в игре; иконки слотов для узнавания по экрану — [matcher]. */
    fun find(robot: Robot, layouts: Map<String, SlotLayout>, matcher: IconMatcher): SlotLayout? =
        CharacterJournal.current()?.let(layouts::get) ?: PlayedCharacterDetector.detect(robot, layouts.values.toList(), matcher)
}
