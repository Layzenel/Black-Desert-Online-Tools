package com.layzen.bdotools.uikit.components

import androidx.compose.ui.graphics.painter.Painter

/**
 * Состояние [ActionButton]: подпись, доступность, действие и вид кнопки по смыслу действия — сам подкласс.
 * Новый вид кнопки — новый подкласс и его ветка в [ActionButton].
 */
sealed interface ActionButtonState {
    /** Подпись кнопки, в одну строку; длинная обрезается многоточием. Пустая при [icon] — квадратная кнопка-иконка. */
    val text: String

    /** Иконка слева от подписи — белый силуэт, цвет ей задаёт кнопка. */
    val icon: Painter?

    /** Горячая клавиша или подсказка справа от подписи, приглушённым текстом (`Esc` у включённого режима). */
    val hotkey: String?

    /** Кнопка нажимается; недоступная — серая, с приглушённым текстом. */
    val enabled: Boolean

    /** Что делать по нажатию. */
    val onClick: () -> Unit

    /** Что делает кнопка — коротко, подсказкой при наведении. Есть у каждой кнопки приложения. */
    val tooltip: String?

    /**
     * Обычное действие (отмена, сброс, открыть, вспомогательные): серая кнопка с лёгким градиентом, как «Назад»
     * в настройках игры; под курсором — коричневая.
     */
    data class Neutral(
        override val text: String,
        override val icon: Painter? = null,
        override val hotkey: String? = null,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
        override val tooltip: String? = null,
    ) : ActionButtonState

    /**
     * Включатель режима («Порядок», «Растянуть»): выключенный — как [Neutral], включённый — коричневый, как кнопка под
     * курсором, с золотой подписью. Нажатие переключает режим.
     *
     * @property checked режим включён
     */
    data class Toggle(
        override val text: String,
        val checked: Boolean,
        override val icon: Painter? = null,
        override val hotkey: String? = null,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
        override val tooltip: String? = null,
    ) : ActionButtonState

    /**
     * Положительное действие (применить, сохранить, OK, установить, далее), как «Выставить лот» в игре: бронзовая
     * кнопка. Недоступная — как [Neutral].
     */
    data class Positive(
        override val text: String,
        override val icon: Painter? = null,
        override val hotkey: String? = null,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
        override val tooltip: String? = null,
    ) : ActionButtonState

    /** Опасное действие (удалить): тёмно-красная кнопка, как корзина в игре. Недоступная — как [Neutral]. */
    data class Negative(
        override val text: String,
        override val icon: Painter? = null,
        override val hotkey: String? = null,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
        override val tooltip: String? = null,
    ) : ActionButtonState
}
