package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.gameinfo.domain.grid.GridStep

/**
 * Сетка поверх игры: прозрачное окно на весь экран, сквозь которое проходят клики; видно, только пока игра на
 * переднем плане и оверлей включён. [uiHeight] — высота экрана игры в UI-px. Есть только в настольной версии;
 * в браузере ничего не показывает.
 */
@Composable
internal expect fun GameGridWindow(step: GridStep, uiHeight: Double)
