package com.layzen.bdotools.composable.overlay

/** Где окно поверх игры встаёт, пока его ни разу не двигали. */
enum class OverlayAnchor {
    /** По центру над полоской здоровья и панелью быстрого доступа. */
    BottomCenter,

    /** У правого верхнего угла экрана. */
    TopRight,
}
