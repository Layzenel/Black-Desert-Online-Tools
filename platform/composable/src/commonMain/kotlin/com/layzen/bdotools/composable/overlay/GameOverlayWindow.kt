package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize

/**
 * Окно поверх игры: прозрачное, без рамки и фокуса, поверх всех окон, размером с содержимое. Видно, только пока
 * игра на переднем плане; клики сквозь него уходят в игру. [movable] — режим перемещения: окно обведено рамкой
 * и тянется мышью. Место запоминается по [id]; пока окно не двигали — [anchor]. [size] — размер до первого замера
 * содержимого. Есть только в настольной версии; в браузере ничего не показывает.
 */
@Composable
expect fun GameOverlayWindow(id: String, anchor: OverlayAnchor, size: DpSize, movable: Boolean, content: @Composable () -> Unit)
