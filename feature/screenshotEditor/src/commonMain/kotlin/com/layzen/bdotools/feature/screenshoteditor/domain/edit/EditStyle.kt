package com.layzen.bdotools.feature.screenshoteditor.domain.edit

/** Цвет пометки (ARGB); `null` — у правки нет цвета: обрезка, скрытая область, ретушь, поворот. */
val Edit.color: Long?
    get() = when (this) {
        is Edit.Arrow -> color
        is Edit.Frame -> color
        is Edit.Stroke -> color
        is Edit.Label -> color
        is Edit.Counter -> color
        is Edit.Crop, is Edit.Pixelate, is Edit.Adjust, is Edit.Orient -> null
    }

/** Та же пометка цвета [color]; правка без цвета не меняется. */
fun Edit.recolored(color: Long): Edit = when (this) {
    is Edit.Arrow -> copy(color = color)
    is Edit.Frame -> copy(color = color)
    is Edit.Stroke -> copy(color = color)
    is Edit.Label -> copy(color = color)
    is Edit.Counter -> copy(color = color)
    is Edit.Crop, is Edit.Pixelate, is Edit.Adjust, is Edit.Orient -> this
}

/** Та же правка по целым пикселям картинки [imageWidth]×[imageHeight]: скрытая область пикселизуется по целым. */
fun Edit.snappedInto(imageWidth: Int, imageHeight: Int): Edit = when (this) {
    is Edit.Pixelate -> copy(rect = rect.snappedInto(imageWidth, imageHeight))
    else -> this
}

/** Та же пометка вида [style], если он к ней подходит: вид стрелки — стрелке, фигура — рамке, вид надписи — надписи. */
fun Edit.styled(style: MarkStyle): Edit = when {
    this is Edit.Arrow && style is ArrowStyle -> copy(style = style)
    this is Edit.Frame && style is FrameShape -> copy(shape = style)
    this is Edit.Label && style is LabelStyle -> copy(style = style)
    else -> this
}
