package com.layzen.bdotools.core.handoff

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Картинка, переданная из одной тулзы в другую, — например, «Применить как фон портретов» в редакторе скриншотов.
 * Фичи друг от друга не зависят, поэтому встречаются здесь: отправитель кладёт картинку, приложение по [sent]
 * открывает вкладку получателя, а получатель забирает картинку через [take].
 */
class ImageHandoff {
    private val _pending = MutableStateFlow<ImageTransfer?>(null)
    private val _sent = MutableSharedFlow<HandoffTarget>(extraBufferCapacity = 1)

    /** Картинка, которую ещё не забрали; `null` — нечего передавать. */
    val pending: StateFlow<ImageTransfer?> = _pending.asStateFlow()

    /**
     * Кому только что передали картинку — по нему открывается вкладка получателя. Отдельно от [pending]: получатель,
     * который уже готов, забирает картинку сразу, и [pending] пустеет раньше, чем экран успел его увидеть.
     */
    val sent: SharedFlow<HandoffTarget> = _sent.asSharedFlow()

    /** Передаёт картинку [bytes] получателю [target]; прежняя незабранная картинка заменяется. */
    fun send(target: HandoffTarget, bytes: ByteArray) {
        _pending.value = ImageTransfer(target, bytes)
        _sent.tryEmit(target)
    }

    /** Забирает картинку для [target]; `null` — для него ничего нет. */
    fun take(target: HandoffTarget): ByteArray? {
        val transfer = _pending.value?.takeIf { it.target == target } ?: return null
        _pending.compareAndSet(transfer, null)
        return transfer.bytes
    }
}
