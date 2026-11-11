package com.layzen.bdotools.feature.uieditor.domain.screen

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import kotlin.math.roundToInt

/**
 * Точка экрана, от которой раскладка отсчитывает элемент. Так раскладка переносится на другой монитор:
 * элементы у персонажа остаются у прицела, элементы у края — у своего угла. Похоже на `PendingType` в игре.
 * [id] — как якорь записан в файле раскладки.
 */
enum class SlotAnchor(val id: String) {
    Crosshair("crosshair"),
    TopLeft("top-left"),
    TopRight("top-right"),
    BottomLeft("bottom-left"),
    BottomRight("bottom-right"),

    /** Центр экрана — так считали раскладки первой версии формата; новые его не используют. */
    ScreenCenter("screen-center"),
    ;

    /** Точка якоря на экране [screen] — от центра экрана, в UI-px. */
    fun point(screen: ScreenSpace): SlotPosition {
        val halfWidth = (screen.width / 2).roundToInt()
        val halfHeight = (screen.height / 2).roundToInt()
        return when (this) {
            Crosshair -> screen.crosshair
            TopLeft -> SlotPosition(-halfWidth, -halfHeight)
            TopRight -> SlotPosition(halfWidth, -halfHeight)
            BottomLeft -> SlotPosition(-halfWidth, halfHeight)
            BottomRight -> SlotPosition(halfWidth, halfHeight)
            ScreenCenter -> SlotPosition(0, 0)
        }
    }

    companion object {
        /** Якоря, из которых выбирается ближайший при сохранении раскладки. */
        private val choices = listOf(Crosshair, TopLeft, TopRight, BottomLeft, BottomRight)

        fun byId(id: String): SlotAnchor? = entries.firstOrNull { it.id == id }

        /** Ближайший к [position] якорь: прицел для элементов у персонажа, угол — для элементов у края. */
        fun nearest(position: SlotPosition, screen: ScreenSpace): SlotAnchor = choices.minBy { anchor ->
            val point = anchor.point(screen)
            val dx = (position.x - point.x).toLong()
            val dy = (position.y - point.y).toLong()
            dx * dx + dy * dy
        }
    }
}
