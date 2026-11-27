package com.layzen.bdotools.core.handoff

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Картинка, переданная из одной тулзы в другую, — например, «Применить как фон портретов» в редакторе скриншотов.
 * Фичи друг от друга не зависят, поэтому встречаются здесь: отправитель кладёт картинку, приложение открывает вкладку
 * [ImageTransfer.target], а её получатель забирает картинку через [take].
 */
class ImageHandoff {
    private val _pending = MutableStateFlow<ImageTransfer?>(null)

    /** Картинка, которую ещё не забрали; `null` — нечего передавать. */
    val pending: StateFlow<ImageTransfer?> = _pending.asStateFlow()

    /** Передаёт картинку [bytes] получателю [target]; прежняя незабранная картинка заменяется. */
    fun send(target: HandoffTarget, bytes: ByteArray) {
        _pending.value = ImageTransfer(target, bytes)
    }

    /** Забирает картинку для [target]; `null` — для него ничего нет. */
    fun take(target: HandoffTarget): ByteArray? {
        val transfer = _pending.value?.takeIf { it.target == target } ?: return null
        _pending.compareAndSet(transfer, null)
        return transfer.bytes
    }
}
