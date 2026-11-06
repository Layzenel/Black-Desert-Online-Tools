package com.layzen.bdotools.uikit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Курсор «диагональные стрелки» (↗↙) над ручкой, за которую растягивают окно за правый верхний угол.
 * [dragging] держит курсор, пока ручку тянут, даже если мышь ушла с неё. На Desktop — системный курсор,
 * в браузере — CSS `nesw-resize` для всего холста Compose, пока курсор над ручкой или её тянут.
 */
@Composable
expect fun Modifier.resizeCursor(dragging: Boolean = false): Modifier
