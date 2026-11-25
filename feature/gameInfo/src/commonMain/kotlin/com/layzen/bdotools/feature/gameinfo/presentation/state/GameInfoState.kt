package com.layzen.bdotools.feature.gameinfo.presentation.state

import com.layzen.bdotools.feature.gameinfo.domain.fps.FrameStats
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettings

/**
 * Что показывать поверх игры.
 *
 * @property ping пинг в миллисекундах; `null` — не узнать или пинг не показывают
 * @property needsAdmin пинг не собрать без прав администратора — подсказка во вкладке
 * @property fps кадры игры за последние секунды; `null` — не узнать или FPS не показывают
 * @property fpsAvailable FPS можно включить: программа запущена от администратора в настольной версии
 * @property uiHeight высота экрана игры в UI-px для сетки; `null` — ещё не прочитана или прочитать не удалось
 * @property gridUnreadable сетка включена, но размер экрана игры прочитать не удалось — подсказка в настройках
 * @property settings как показывать пинг, FPS и сетку
 */
data class GameInfoState(
    val ping: Int? = null,
    val needsAdmin: Boolean = false,
    val fps: FrameStats? = null,
    val fpsAvailable: Boolean = false,
    val uiHeight: Double? = null,
    val gridUnreadable: Boolean = false,
    val settings: PingSettings = PingSettings(),
) {
    /** FPS включён и его можно мерить: сохранённая настройка без прав администратора не действует. */
    val fpsShown: Boolean get() = settings.fpsShown && fpsAvailable

    /** Окно пинга видно: показывают пинг или FPS. */
    val windowShown: Boolean get() = settings.shown || fpsShown
}
