package com.layzen.bdotools.feature.uieditor.domain

/**
 * Набор позиций интерфейса в `gamevariable.xml`. [General] — то, что игра показывает сейчас (`<UIData>`),
 * остальные — «Пресет 1–3» из окна «Редактирование интерфейса» (`<UISettingPreset0..2>`).
 *
 * [tag] — имя тега записей; [relativeDigits] — сколько знаков после точки игра пишет в `RelativePosX/Y` этого набора.
 */
enum class GamePreset(val tag: String, val relativeDigits: Int) {
    General(tag = "UIData", relativeDigits = 10),
    Preset1(tag = "UISettingPreset0", relativeDigits = 6),
    Preset2(tag = "UISettingPreset1", relativeDigits = 6),
    Preset3(tag = "UISettingPreset2", relativeDigits = 6),
}
