package com.layzen.bdotools.feature.comboassist.data.slots

/**
 * Видны ли слоты перезарядок на экране. Слот узнан, если его картинка заметно ближе к иконке своего умения, чем к
 * иконкам умений других слотов ([recognized]): когда слоты закрыты окном игры, картами или скрытым интерфейсом, под
 * ними фон, и своё умение ничем не лучше чужих. Абсолютный порог не годится: иконки на перезарядке темнее, а фон
 * бывает похож на любую иконку. Слоты видны, если узнано хотя бы [MIN_RECOGNIZED]; пропадают через [HIDE_CHECKS]
 * проверок подряд без узнанных (анимация окна их не прячет), появляются — с первой удачной.
 */
internal class SlotVisibility {
    private var hiddenChecks = 0

    /** Слоты сейчас видны. */
    val shown: Boolean get() = hiddenChecks < HIDE_CHECKS

    /** Новая проверка: узнано [count] слотов. `true`, если видимость сменилась. */
    fun onCheck(count: Int): Boolean {
        val before = shown
        hiddenChecks = if (count >= MIN_RECOGNIZED) 0 else hiddenChecks + 1
        return shown != before
    }

    companion object {
        /** Насколько своя иконка должна быть ближе лучшей чужой, чтобы слот считался узнанным. */
        private const val GAP = 8.0

        /** Сколько слотов должно узнаться, чтобы слоты считались видимыми. */
        private const val MIN_RECOGNIZED = 2

        /** Сколько проверок подряд без узнанных слотов — и слоты считаются скрытыми. */
        private const val HIDE_CHECKS = 4

        /** Слот узнан: разница [own] со своей иконкой меньше лучшей разницы [others] с чужими на [GAP]. */
        fun recognized(own: Double?, others: List<Double>): Boolean {
            val best = others.minOrNull() ?: return false
            return own != null && own + GAP <= best
        }
    }
}
