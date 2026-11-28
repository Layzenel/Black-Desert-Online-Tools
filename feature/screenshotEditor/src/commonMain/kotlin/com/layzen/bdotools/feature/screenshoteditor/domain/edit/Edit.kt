package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.Adjustments

/**
 * Одна правка скриншота. Исходная картинка не меняется: экран и сохранение рисуют её заново с правками
 * поверх, поэтому любую правку можно отменить. Координаты и толщина — в пикселях исходной картинки,
 * цвет — ARGB (`0xFFRRGGBB`).
 */
sealed interface Edit {
    /** Обрезка: при сохранении остаётся только [rect]. Действует последняя обрезка в истории. */
    data class Crop(val rect: ImageRect) : Edit

    /**
     * Скрытие области [rect] — ника, семьи, гильдии или чата: пикселями или размытием ([mode]), сила [strength]
     * от 1 до [MAX_HIDE_STRENGTH] — во сколько крупнее пиксели или сильнее размытие.
     */
    data class Pixelate(val rect: ImageRect, val mode: HideMode = HideMode.Pixelate, val strength: Int = DEFAULT_HIDE_STRENGTH) : Edit

    /** Стрелка из [from] в [to], наконечник у [to]; вид — [style]. */
    data class Arrow(
        val from: ImagePoint,
        val to: ImagePoint,
        val color: Long,
        val width: Float,
        val style: ArrowStyle = ArrowStyle.Single,
    ) : Edit

    /** Фигура [shape], вписанная в [rect]: прямоугольник, круг, ромб… */
    data class Frame(val rect: ImageRect, val color: Long, val width: Float, val shape: FrameShape = FrameShape.Rectangle) : Edit

    /** Кружок с номером [number] — порядок действий на скриншоте; середина — [center], радиус — [radius]. */
    data class Counter(val center: ImagePoint, val number: Int, val color: Long, val radius: Float) : Edit

    /** Линия кистью через [points]. */
    data class Stroke(val points: List<ImagePoint>, val color: Long, val width: Float) : Edit

    /** Ретушь цвета всей картинки. Действует последняя в истории: так каждое движение ползунка отменяется. */
    data class Adjust(val adjustments: Adjustments) : Edit

    /** Поворот и отражение готовой картинки. Действует последний в истории. */
    data class Orient(val orientation: Orientation) : Edit

    /** Надпись [text], левый верхний угол — [at], высота букв — [size], вид — [style]. */
    data class Label(val at: ImagePoint, val text: String, val color: Long, val size: Float, val style: LabelStyle = LabelStyle.Shadow) : Edit
}

/** Сила скрытия области по умолчанию — та, что была до настройки. */
const val DEFAULT_HIDE_STRENGTH = 5

/** Наибольшая сила скрытия области. */
const val MAX_HIDE_STRENGTH = 10
