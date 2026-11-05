package com.layzen.bdotools.uikit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Вид курсора при сдвиге рабочей области: открытая рука, когда сдвиг возможен, и сжатая, пока область «схвачена». */
enum class PanCursor { None, Open, Grabbing }

/**
 * Курсор «рука» ([PanCursor.Open]) и «схватил» ([PanCursor.Grabbing]) над элементом; [PanCursor.None] курсор не меняет.
 * На Desktop руки нарисованы в коде (в Compose нет курсоров grab). В браузере включается CSS-курсор `grab`
 * или `grabbing` для всего холста Compose, поэтому ставь не-`None` только пока курсор мыши над элементом.
 */
@Composable
expect fun Modifier.panCursor(cursor: PanCursor): Modifier
