package com.layzen.bdotools.feature.comboassist.data.history

import com.layzen.bdotools.core.xml.attributesOf
import com.layzen.bdotools.feature.comboassist.data.slots.parseResolutionHeight
import com.layzen.bdotools.feature.comboassist.data.slots.parseUiScale

private val HISTORY = Regex("""<UIData\s+Index="158"[^>]*>""")

/**
 * Где «История испол. навыков» (`UIData Index="158"` файла аккаунта): центр в долях экрана и масштаб интерфейса.
 *
 * @property centerX центр по горизонтали, доля ширины экрана
 * @property centerY центр по вертикали, доля высоты экрана
 * @property uiScale «Размер интерфейса» игры (100 % → 1)
 * @property resolutionHeight высота разрешения игры в пикселях
 */
internal data class HistoryPlace(val centerX: Double, val centerY: Double, val uiScale: Double, val resolutionHeight: Int) {
    companion object {
        /** Место истории из текста файла аккаунта [accountText] или `null`, если записи с местом или разрешения нет. */
        fun parse(accountText: String): HistoryPlace? {
            val attributes = HISTORY.find(accountText)?.value?.let(::attributesOf) ?: return null
            val x = attributes["RelativePosX"]?.toDoubleOrNull() ?: return null
            val y = attributes["RelativePosY"]?.toDoubleOrNull() ?: return null
            val height = parseResolutionHeight(accountText) ?: return null
            return HistoryPlace(x, y, parseUiScale(accountText) ?: 1.0, height)
        }
    }
}
