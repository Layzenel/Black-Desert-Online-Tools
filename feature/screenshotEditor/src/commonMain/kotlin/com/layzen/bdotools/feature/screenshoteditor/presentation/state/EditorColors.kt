package com.layzen.bdotools.feature.screenshoteditor.presentation.state

/** Цвета пометок (ARGB): первый — по умолчанию (красный), за ним остальные — в палитре по порядку. */
internal object EditorColors {
    const val DEFAULT: Long = 0xFFE53935

    val choices: List<Long> = listOf(
        0xFFFFC107,
        0xFF43A047,
        0xFF1E88E5,
        0xFFFFFFFF,
        0xFF000000,
    )

    /** Сколько цветов пипетки помнит палитра. */
    const val MAX_RECENT = 5
}
