package com.layzen.bdotools.uikit.components

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Сколько после отпускания полосы прокрутки всплывающий список ещё не закрывается: отпускание — тот же жест. */
private val RELEASE_GRACE = 300.milliseconds

/**
 * Идёт ли перетаскивание [verticalScrollbar]. Курсор, уехавший с полосы за край всплывающего списка, система
 * принимает за клик мимо него и закрывает список; [MenuPopup] спрашивает здесь, не жест ли это полосы.
 */
internal object ScrollbarDrag {
    private var active = false
    private var endedAt: TimeMark? = null

    /** Полосу схватили. */
    fun start() {
        active = true
    }

    /** Полосу отпустили. */
    fun end() {
        active = false
        endedAt = TimeSource.Monotonic.markNow()
    }

    /** Закрытие списка сейчас — след перетаскивания полосы, а не клик мимо. */
    val blocksDismiss: Boolean
        get() = active || endedAt?.let { it.elapsedNow() < RELEASE_GRACE } == true
}
