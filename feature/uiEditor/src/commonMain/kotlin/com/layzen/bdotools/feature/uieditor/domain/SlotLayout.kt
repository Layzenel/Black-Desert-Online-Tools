package com.layzen.bdotools.feature.uieditor.domain

/**
 * Раскладка: позиция каждого элемента, какие из них скрыты и ширина заглушек надписей ([labels], в символах,
 * см. [SlotLabel]). Может содержать не все элементы — готовые пресеты, например, задают только слоты перезарядки
 * и не трогают хотбар. Надписи живут только в пресетах и файлах раскладки: в `gamevariable.xml` их нет.
 *
 * [customQuickSlots] — «Настройка быстрых слотов» игры: `true` — хотбар из 20 отдельных слотов, `false` — одна
 * панель «Быстрый слот» ([InterfaceElement.QuickSlot]); `null` — раскладка её не задаёт и не меняет.
 *
 * [chats] — окна чата по `WindowIndex`.
 */
data class SlotLayout(
    val positions: Map<SlotId, SlotPosition> = emptyMap(),
    val hidden: Set<SlotId> = emptySet(),
    val labels: Map<SlotId, Int> = emptyMap(),
    val customQuickSlots: Boolean? = null,
    val chats: Map<Int, ChatWindow> = emptyMap(),
) {

    operator fun get(id: SlotId): SlotPosition? = positions[id]

    /** Виден ли элемент; у элементов без своего глазика ([SlotId.hasVisibilityToggle]) — всегда. */
    fun isVisible(id: SlotId): Boolean = !id.hasVisibilityToggle || id !in hidden

    /**
     * Позиции, видимость и надписи из [other] поверх текущих. Видимость и надписи меняются только у элементов,
     * которые есть в [other]. Окна чата берутся только те, что уже есть здесь: у игрока свой набор чатов,
     * и чужие окна у него не появляются.
     */
    fun overlaidWith(other: SlotLayout) = SlotLayout(
        positions = positions + other.positions,
        hidden = (hidden - other.positions.keys) + other.hidden,
        labels = (labels - other.positions.keys) + other.labels,
        customQuickSlots = other.customQuickSlots ?: customQuickSlots,
        chats = chats + other.chats.filterKeys { it in chats },
    )

    fun withPosition(id: SlotId, position: SlotPosition) = copy(positions = positions + (id to position))

    /**
     * Видно ли окно чата [window] в игре отдельным окном: скрытый главный чат прячет все окна, а при видимом главном
     * у каждого побочного окна своя видимость. Вкладки главного окна ([ChatWindow.combined]) отдельно не видны.
     */
    fun isChatShown(window: Int): Boolean {
        val chat = chats[window]?.takeIf { !it.combined } ?: return false
        return chat.visible && (window == ChatWindow.MAIN || chats[ChatWindow.MAIN]?.visible != false)
    }

    /** Вкладку главного окна [window] — в отдельное окно над главным, см. [ChatWindow.detachedAbove]. */
    fun withDetachedChat(window: Int): SlotLayout {
        val chat = chats[window]?.takeIf { it.combined } ?: return this
        val main = chats[ChatWindow.MAIN] ?: return this
        return withChat(window, chat.detachedAbove(main))
    }

    /**
     * Отдельное окно [window] — обратно во вкладку главного окна. Размер окна сохраняется: с ним окно встанет
     * при следующем выносе. Скрыто, как игра хранит вкладки (`IsShow="false"`). Главное окно не вкладывается.
     */
    fun withCombinedChat(window: Int): SlotLayout {
        val chat = chats[window]?.takeIf { window != ChatWindow.MAIN && !it.combined } ?: return this
        return withChat(window, chat.copy(combined = true, visible = false))
    }

    /** Окно чата [window] заменено на [chat]; окна, которого нет в раскладке, не добавляет. */
    fun withChat(window: Int, chat: ChatWindow) = if (window in chats) copy(chats = chats + (window to chat)) else this

    fun withVisibility(id: SlotId, visible: Boolean) =
        if (!id.hasVisibilityToggle) this else copy(hidden = if (visible) hidden - id else hidden + id)

    /** Переключает «Настройку быстрых слотов» вместе с видимостью хотбара и «Быстрого слота», см. [hiddenWithCustomQuickSlots]. */
    fun withCustomQuickSlots(enabled: Boolean) =
        copy(customQuickSlots = enabled, hidden = hiddenWithCustomQuickSlots(hidden, enabled))

    /** Заглушка надписи шириной [chars] символов; `null` убирает надпись. */
    fun withLabel(id: SlotId, chars: Int?) = copy(labels = if (chars == null) labels - id else labels + (id to chars))

    /** Рамка, охватывающая элементы [ids]; `null`, если ни одного из них нет в раскладке. */
    fun boundsOf(ids: Set<SlotId>): SlotBounds? =
        SlotBounds.union(ids.mapNotNull { id -> this[id]?.let { SlotBounds.of(id, it) } })

    fun moved(ids: Set<SlotId>, dx: Int, dy: Int) = transform(ids) { it.movedBy(dx, dy) }

    /** Отдельные окна чата из [windows]: вкладки главного окна своего места на экране не имеют. */
    fun separateChats(windows: Set<Int>): Map<Int, ChatWindow> = chats.filter { (window, chat) -> window in windows && !chat.combined }

    /** Рамка, охватывающая элементы [ids] и отдельные окна чата [windows]; `null`, если ничего из них нет. */
    fun boundsOf(ids: Set<SlotId>, windows: Set<Int>): SlotBounds? = SlotBounds.union(
        ids.mapNotNull { id -> this[id]?.let { SlotBounds.of(id, it) } } + separateChats(windows).values.map { it.bounds },
    )

    /** Элементы [ids] и отдельные окна чата [windows] сдвинуты вместе на [dx], [dy] UI-px. */
    fun moved(ids: Set<SlotId>, windows: Set<Int>, dx: Int, dy: Int): SlotLayout {
        val movedChats = separateChats(windows).mapValues { (_, chat) -> chat.movedBy(dx, dy) }
        return moved(ids, dx, dy).copy(chats = chats + movedChats)
    }

    private fun transform(ids: Set<SlotId>, change: (SlotPosition) -> SlotPosition) =
        copy(positions = positions.mapValues { (id, position) -> if (id in ids) change(position) else position })
}
