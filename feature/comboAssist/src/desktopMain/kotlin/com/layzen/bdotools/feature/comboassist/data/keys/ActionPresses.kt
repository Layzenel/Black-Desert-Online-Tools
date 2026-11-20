package com.layzen.bdotools.feature.comboassist.data.keys

import com.layzen.bdotools.core.skill.domain.KeyBindings

/**
 * Переводит клавиши в действия игры по раскладке персонажа: какое действие нажали только что и какие зажаты
 * вместе с ним. Повтор зажатой клавиши (автоповтор Windows) — не новое нажатие.
 */
internal class ActionPresses {
    /** Зажатые клавиши: код → когда зажали. */
    private val held = mutableMapOf<Int, Long>()

    /**
     * Клавиша [key] в момент [nowMs] при раскладке [keys] → нажатые только что действия, у каждого — все зажатые
     * действия (вместе с ним). Отпускание и клавиши без действия — пусто.
     */
    fun onKey(key: RawKey, keys: KeyBindings, nowMs: Long = 0): List<Pair<String, Set<String>>> {
        if (!key.down) { held -= key.keyCode; return emptyList() }
        if (key.keyCode in held) return emptyList()
        held[key.keyCode] = nowMs
        val heldActions = heldActions(keys)
        return keys.keyCodes.filterValues { it == key.keyCode }.keys.map { it to heldActions }
    }

    /** Действия игры, чьи клавиши сейчас зажаты, при раскладке [keys]; [downBefore] — только зажатые не позже этого момента (нажатие, засчитавшее прошлый шаг, тоже). */
    fun heldActions(keys: KeyBindings, downBefore: Long = Long.MAX_VALUE): Set<String> =
        keys.keyCodes.filterValues { code -> held[code]?.let { it <= downBefore } == true }.keys
}
