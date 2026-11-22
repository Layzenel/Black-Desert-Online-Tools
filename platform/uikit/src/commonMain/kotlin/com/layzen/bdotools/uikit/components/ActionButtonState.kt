package com.layzen.bdotools.uikit.components

/**
 * Состояние [ActionButton]: подпись, доступность, действие и вид кнопки по смыслу действия — сам подкласс.
 * Новый вид кнопки — новый подкласс и его ветка в [ActionButton].
 */
sealed interface ActionButtonState {
    /** Подпись кнопки, в одну строку. */
    val text: String

    /** Кнопка нажимается; недоступная — серая, с приглушённым текстом. */
    val enabled: Boolean

    /** Что делать по нажатию. */
    val onClick: () -> Unit

    /** Обычное действие (отмена, сброс, удаление, вспомогательные): серая кнопка с лёгким градиентом. */
    data class Neutral(
        override val text: String,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
    ) : ActionButtonState

    /**
     * Положительное действие (применить, сохранить, OK, установить, далее), как «Выставить лот» в игре: бронзовая
     * кнопка. Недоступная — как [Neutral].
     */
    data class Positive(
        override val text: String,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
    ) : ActionButtonState

    /** Опасное действие (удалить): тёмно-красная кнопка, как корзина в игре. Недоступная — как [Neutral]. */
    data class Negative(
        override val text: String,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
    ) : ActionButtonState
}
