package com.layzen.bdotools.feature.comboassist.data.resource

import com.layzen.bdotools.core.xml.attributesOf
import com.layzen.bdotools.feature.comboassist.data.slots.parseResolutionHeight
import com.layzen.bdotools.feature.comboassist.data.slots.parseUiScale

private val HEALTH = Regex("""<UIData\s+Index="13"[^>]*>""")
private val JOINED_HEALTH = Regex("""<Bool\s+Index="72"\s+Value="(true|false)"""")

/**
 * Где полоски HP и ресурса: при «Разделении HP» — элемент здоровья (`UIData Index="13"`, центр в долях экрана), без
 * него — неподвижная панель слева вверху (`Index="138"`, позиции нет). Значение «Разделения HP» обратное:
 * `Bool Index="72"` = `true` — одна панель (скилл `game-files`).
 *
 * @property centerX центр элемента здоровья, доля ширины экрана; `null` — панель слева вверху
 * @property centerY центр элемента здоровья, доля высоты экрана; `null` — панель слева вверху
 * @property uiScale «Размер интерфейса» игры (100 % → 1)
 * @property resolutionHeight высота разрешения игры в пикселях
 */
internal data class ResourceBarPlace(val centerX: Double?, val centerY: Double?, val uiScale: Double, val resolutionHeight: Int) {
    companion object {
        /** Место из текста файла аккаунта [accountText] или `null`, если разрешения нет. */
        fun parse(accountText: String): ResourceBarPlace? {
            val height = parseResolutionHeight(accountText) ?: return null
            val scale = parseUiScale(accountText) ?: 1.0
            val joined = JOINED_HEALTH.find(accountText)?.groupValues?.get(1) == "true"
            val health = HEALTH.find(accountText)?.value?.let(::attributesOf)
            val x = health?.get("RelativePosX")?.toDoubleOrNull()
            val y = health?.get("RelativePosY")?.toDoubleOrNull()
            return if (joined || x == null || y == null) ResourceBarPlace(null, null, scale, height) else ResourceBarPlace(x, y, scale, height)
        }
    }
}
