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

    /** Пикселизация области [rect] — скрыть ник, семью, гильдию или чат. */
    data class Pixelate(val rect: ImageRect) : Edit

    /** Стрелка из [from] в [to], наконечник у [to]. */
    data class Arrow(val from: ImagePoint, val to: ImagePoint, val color: Long, val width: Float) : Edit

    /** Рамка по краю [rect]. */
    data class Frame(val rect: ImageRect, val color: Long, val width: Float) : Edit

    /** Линия кистью через [points]. */
    data class Stroke(val points: List<ImagePoint>, val color: Long, val width: Float) : Edit

    /** Ретушь цвета всей картинки. Действует последняя в истории: так каждое движение ползунка отменяется. */
    data class Adjust(val adjustments: Adjustments) : Edit

    /** Поворот и отражение готовой картинки. Действует последний в истории. */
    data class Orient(val orientation: Orientation) : Edit

    /** Надпись [text], левый верхний угол — [at], высота букв — [size]. */
    data class Label(val at: ImagePoint, val text: String, val color: Long, val size: Float) : Edit
}
