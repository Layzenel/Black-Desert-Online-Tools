package com.layzen.bdotools.feature.comboassist.data.rage

import com.layzen.bdotools.core.xml.attributesOf
import com.layzen.bdotools.feature.comboassist.data.slots.parseResolutionHeight
import com.layzen.bdotools.feature.comboassist.data.slots.parseUiScale

private val BUFF_LIST = Regex("""<UIData\s+Index="119"[^>]*>""")

/**
 * Где «Список усилений» (`UIData Index="119"` файла аккаунта): центр в долях экрана и масштаб интерфейса.
 *
 * @property centerX центр по горизонтали, доля ширины экрана
 * @property centerY центр по вертикали, доля высоты экрана
 * @property uiScale «Размер интерфейса» игры (100 % → 1)
 * @property resolutionHeight высота разрешения игры в пикселях
 */
internal data class BuffListPlace(val centerX: Double, val centerY: Double, val uiScale: Double, val resolutionHeight: Int) {
    companion object {
        /** Место списка из текста файла аккаунта [accountText] или `null`, если записи или разрешения нет. */
        fun parse(accountText: String): BuffListPlace? {
            val attributes = BUFF_LIST.find(accountText)?.value?.let(::attributesOf) ?: return null
            val x = attributes["RelativePosX"]?.toDoubleOrNull() ?: return null
            val y = attributes["RelativePosY"]?.toDoubleOrNull() ?: return null
            val height = parseResolutionHeight(accountText) ?: return null
            return BuffListPlace(x, y, parseUiScale(accountText) ?: 1.0, height)
        }
    }
}
