package com.layzen.bdotools.feature.comboassist.data.slots

/**
 * Замечает, когда слот перезарядки уходит на перезарядку: цветная иконка за кадр сменяется тёмным квадратом
 * с белыми секундами, и средняя цветность слота резко падает (замер 2026-11-18: ~55 → ~13), а потом возвращается.
 * Возвращается она неровно (2026-11-20: слот срабатывал по 5–7 раз за одну перезарядку), поэтому сработавший слот
 * снова следится, только когда цветность вернулась почти к той, что была до перезарядки.
 *
 * Если за кадр потемнели сразу несколько слотов, это не применение умения: интерфейс скрыли, открыли меню
 * или у умений общая перезарядка, и какое из них применено, по слотам не понять.
 *
 * @property count сколько слотов
 */
internal class SlotDarkening(private val count: Int) {
    private val recent = List(count) { ArrayDeque<Int>() }

    /** Цветность слота до перезарядки, пока он не вернулся к ней; `null` — слот следится. */
    private val waitingFor = arrayOfNulls<Int>(count)

    /** Цветность слотов [saturations] на новом кадре → номера слотов, которые только что ушли на перезарядку. */
    fun onFrame(saturations: IntArray): List<Int> {
        for (slot in 0 until count) {
            val before = waitingFor[slot] ?: continue
            if (saturations[slot] >= before * REARM_SHARE) waitingFor[slot] = null
        }
        val dark = (0 until count).filter { slot ->
            val peak = recent[slot].maxOrNull() ?: return@filter false
            waitingFor[slot] == null && peak >= READY_SATURATION && saturations[slot] <= peak * DARK_SHARE
        }
        for (slot in 0 until count) {
            if (slot in dark) {
                waitingFor[slot] = recent[slot].max()
                recent[slot].clear()
            }
            recent[slot].addLast(saturations[slot])
            if (recent[slot].size > WINDOW) recent[slot].removeFirst()
        }
        return if (dark.size == 1) dark else emptyList()
    }

    private companion object {
        /** Сколько последних кадров помнить: падение бывает растянуто на пару кадров. */
        const val WINDOW = 3

        /** Цветность готового слота не ниже этой; серые и пустые слоты не следим. */
        const val READY_SATURATION = 30

        /** Слот потемнел, если цветность упала до этой доли от недавней. */
        const val DARK_SHARE = 0.45

        /**
         * Сработавший слот снова следится, когда цветность вернулась к этой доле от прежней.
         * shortcut: доля — догадка, уточнить по `watch.log` («slot … on cooldown»), если слот пропускает повторные применения.
         */
        const val REARM_SHARE = 0.85
    }
}
