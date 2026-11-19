package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Вид трекера комбо поверх игры. Схема комбо — отдельное окно, её показ —
 * [com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings.schemaShown].
 */
enum class OverlayStyle {
    /** Карточка «что жать сейчас» и два следующих умения. */
    Next,

    /** Крупная иконка текущего шага с клавишами, рядом два следующих, внизу полоска прогресса. */
    Focus,
}
