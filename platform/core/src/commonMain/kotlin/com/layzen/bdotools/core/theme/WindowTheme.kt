package com.layzen.bdotools.core.theme

/**
 * Тема окон, как «Настройки темы интерфейса» в игре: меняет шапку окон, украшение над ней и крестик закрытия.
 * Как она выглядит — в дизайн-системе (`platform/uikit`).
 *
 * @property id код темы в сохранённых настройках; не меняется, иначе выбор пользователя забудется
 */
enum class WindowTheme(val id: String) {
    Classic("classic"),
    Summer("summer"),
    Winter("winter"),
    Spring("spring"),
    MorningLand("morning"),
    ;

    companion object {
        /** Тема по умолчанию — как в игре без выбора. */
        val Default = Classic

        /** Тема по коду [id] или `null`, если такой нет (код из старой версии или испорченный). */
        fun fromId(id: String?): WindowTheme? = entries.firstOrNull { it.id == id }
    }
}
