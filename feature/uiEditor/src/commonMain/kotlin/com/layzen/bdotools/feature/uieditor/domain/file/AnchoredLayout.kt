package com.layzen.bdotools.feature.uieditor.domain.file

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.layout.hiddenWithCustomQuickSlots
import com.layzen.bdotools.feature.uieditor.domain.layout.hiddenWithSplitHp
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import kotlin.math.roundToInt

/** Самый мелкий масштаб интерфейса, который предлагаем при импорте: меньше в игре не бывает. */
private const val MIN_UI_SCALE = 0.5

/** Шаг подбора масштаба интерфейса, как у ползунка в настройках игры. */
private const val UI_SCALE_STEP = 0.05

/**
 * Раскладка, не привязанная к монитору: каждый элемент — смещение от своего якоря.
 * Так хранятся пресеты и файлы экспорта; на конкретный экран её ставит [placeOn].
 */
data class AnchoredLayout(
    val positions: Map<SlotId, AnchoredPosition> = emptyMap(),
    val hidden: Set<SlotId> = emptySet(),
    /** Ширина заглушек надписей в символах, см. [SlotLayout.labels]. */
    val labels: Map<SlotId, Int> = emptyMap(),
    /** «Настройка быстрых слотов», см. [SlotLayout.customQuickSlots]. */
    val customQuickSlots: Boolean? = null,
    /** Окна чата по `WindowIndex`, см. [SlotLayout.chats]. */
    val chats: Map<Int, AnchoredChat> = emptyMap(),
    /** «Разделение HP», см. [SlotLayout.splitHp]. */
    val splitHp: Boolean? = null,
) {

    /** Ставит раскладку на экран [screen]: каждое смещение от якоря превращается в позицию от центра экрана. */
    fun placeOn(screen: ScreenSpace) = SlotLayout(
        positions.mapValues { it.value.placeOn(screen) },
        hidden,
        labels,
        customQuickSlots,
        chats.mapValues { it.value.placeOn(screen) },
        splitHp,
    )

    /**
     * Только элементы [ids] и окна чата [chatWindows]: так импортируется и экспортируется часть раскладки.
     * Настройки быстрых слотов и разделения HP остаются.
     */
    fun only(ids: Set<SlotId>, chatWindows: Set<Int>) = AnchoredLayout(
        positions.filterKeys { it in ids },
        hidden intersect ids,
        labels.filterKeys { it in ids },
        customQuickSlots,
        chats.filterKeys { it in chatWindows },
        splitHp,
    )

    /** Переключает «Настройку быстрых слотов» вместе с видимостью хотбара и «Быстрого слота», см. [hiddenWithCustomQuickSlots]. */
    fun withCustomQuickSlots(enabled: Boolean) =
        copy(customQuickSlots = enabled, hidden = hiddenWithCustomQuickSlots(hidden, enabled))

    /** Переключает «Разделение HP» вместе с видимостью здоровья, PVP и гнева, см. [hiddenWithSplitHp]. */
    fun withSplitHp(enabled: Boolean) = copy(splitHp = enabled, hidden = hiddenWithSplitHp(hidden, enabled))

    /** Есть ли элемент [id] на экране игры при настройках этой раскладки, см. [SlotId.isActiveWith]. */
    fun isActive(id: SlotId): Boolean = id.isActiveWith(customQuickSlots, splitHp)

    /** Сколько элементов не помещается на экран [screen]. */
    fun offScreenCount(screen: ScreenSpace): Int =
        positions.count { (id, position) -> !screen.fits(id, position.placeOn(screen)) }

    /**
     * Наибольший масштаб интерфейса меньше текущего, при котором на этом мониторе помещается всё.
     * `null` — если уже помещается или не помещается даже при самом мелком масштабе.
     */
    fun suggestedUiScale(screen: ScreenSpace): Double? {
        if (offScreenCount(screen) == 0) return null
        var steps = (screen.uiScale / UI_SCALE_STEP).roundToInt() - 1
        while (steps * UI_SCALE_STEP >= MIN_UI_SCALE - 1e-9) {
            val scale = steps * UI_SCALE_STEP
            if (offScreenCount(screen.copy(uiScale = scale)) == 0) return (scale * 100).roundToInt() / 100.0
            steps--
        }
        return null
    }

    companion object {
        /** Запоминает раскладку экрана [screen] от ближайших якорей. */
        fun of(layout: SlotLayout, screen: ScreenSpace) = AnchoredLayout(
            positions = layout.positions.mapValues { AnchoredPosition.of(it.value, screen) },
            hidden = layout.hidden,
            labels = layout.labels,
            customQuickSlots = layout.customQuickSlots,
            chats = layout.chats.mapValues { AnchoredChat.of(it.value, screen) },
            splitHp = layout.splitHp,
        )
    }
}
