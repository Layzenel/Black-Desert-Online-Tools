package com.layzen.bdotools.feature.comboassist.data.slots

/**
 * Какие слоты перезарядок не видны на экране. Слот узнан, если его картинка заметно ближе к иконке своего умения, чем
 * к иконкам умений других слотов ([recognized]): когда слот закрыт окном игры, картами или скрытым интерфейсом, под
 * ним фон, и своё умение ничем не лучше чужих. Абсолютный порог не годится: иконки на перезарядке темнее, а фон бывает
 * похож на любую иконку.
 *
 * По своей иконке узнаётся меньшинство слотов (2026-11-20: 5 из 20, а пара — лишь раз в 0,6 с, на перезарядке),
 * поэтому решают два правила:
 * - **все слоты** закрыты, если [GROUP_HIDE_CHECKS] проверок подряд узнано меньше [MIN_RECOGNIZED] — окно умений,
 *   настроек, скрытый интерфейс; появляются с первой удачной;
 * - **отдельный слот** закрыт, только если он узнаётся стабильно ([reliable]), а теперь не узнаётся
 *   [SLOT_HIDE_CHECKS] проверок подряд — окно закрыло часть слотов. Слот, который узнаётся редко (2026-11-20: раз
 *   в 2 с) или ни разу, видим вместе со всеми: о нём самом ничего не известно, а прятать его — моргать значками.
 *
 * @property count сколько слотов
 */
internal class SlotVisibility(private val count: Int) {
    private val missed = IntArray(count)

    /** Доля проверок, где слот узнан (скользящее среднее); пока слот закрыт, не меняется. */
    private val share = DoubleArray(count)
    private var groupMissed = 0

    /** Номера (по порядку раскладки) слотов, которые сейчас не видны. */
    val hidden: Set<Int>
        get() = if (groupMissed >= GROUP_HIDE_CHECKS) (0 until count).toSet()
        else (0 until count).filter { reliable(it) && missed[it] >= SLOT_HIDE_CHECKS }.toSet()

    /** Слот узнаётся стабильно: по нему одному можно понять, закрыт ли он. */
    private fun reliable(slot: Int): Boolean = share[slot] >= RELIABLE_SHARE

    /** Новая проверка: узнан ли каждый слот ([recognized] по порядку раскладки). */
    fun onCheck(recognized: List<Boolean>) {
        recognized.forEachIndexed { slot, ok ->
            if (slot >= count) return@forEachIndexed
            // пока слот закрыт, промахи не портят его долю: открытый — снова узнается
            if (ok || missed[slot] < SLOT_HIDE_CHECKS) share[slot] = share[slot] * (1 - SHARE_STEP) + if (ok) SHARE_STEP else 0.0
            missed[slot] = if (ok) 0 else missed[slot] + 1
        }
        groupMissed = if (recognized.count { it } >= MIN_RECOGNIZED) 0 else groupMissed + 1
    }

    companion object {
        /** Насколько своя иконка должна быть ближе лучшей чужой, чтобы слот считался узнанным. */
        private const val GAP = 8.0

        /** Сколько слотов должно узнаться, чтобы слоты в целом считались видимыми. */
        private const val MIN_RECOGNIZED = 2

        /** Сколько проверок подряд почти без узнанных слотов — и скрыты все (анимация окна их не прячет). */
        private const val GROUP_HIDE_CHECKS = 4

        /** Сколько проверок подряд без узнавания — и скрыт отдельный стабильный слот. */
        private const val SLOT_HIDE_CHECKS = 15

        /** Вес новой проверки в доле узнаваний. */
        private const val SHARE_STEP = 0.05

        /**
         * Доля узнаваний стабильного слота: после 14 промахов подряд у узнаваемого всегда остаётся ≈0,49, а у слота,
         * узнаваемого раз в 2 с, она не поднимается выше ≈0,1.
         */
        private const val RELIABLE_SHARE = 0.3

        /** Слот узнан: разница [own] со своей иконкой меньше лучшей разницы [others] с чужими на [GAP]. */
        fun recognized(own: Double?, others: List<Double>): Boolean {
            val best = others.minOrNull() ?: return false
            return own != null && own + GAP <= best
        }
    }
}
