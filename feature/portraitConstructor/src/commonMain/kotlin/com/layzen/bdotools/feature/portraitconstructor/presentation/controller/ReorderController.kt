package com.layzen.bdotools.feature.portraitconstructor.presentation.controller

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitWrite
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.StoredPortrait
import com.layzen.bdotools.feature.portraitconstructor.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.Notice
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.NoticeKind
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReorderState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.SwapAnimation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Изменение порядка прямо на сетке: портреты затемняются, пользователь выбирает два — и они сразу
 * обмениваются картинками, а заметка и значок остаются на своих местах. Режим не выключается после обмена:
 * можно переставлять дальше, пока не нажмут «Отменить» или Esc. Нужно, когда в окне выбора персонажа игры
 * персонажей переставили и общая картинка на портретах разъехалась. Старые портреты уходят в резервную копию.
 * Запись идёт без окна ожидания сразу после анимации: файлов всего два.
 */
internal class ReorderController(private val context: PortraitsContext) {

    /** Идёт обмен: анимация или запись файлов. */
    private var swapping = false

    /** Включает режим перестановки и показывает подсказку. */
    fun start() = context.update { it.copy(reorder = ReorderState(), notice = Notice(NoticeKind.ReorderHint)) }

    /** Выключает режим перестановки. */
    fun cancel() = context.update { it.copy(reorder = null) }

    /** Выбирает портрет [characterId]: первым в паре или вторым — тогда запускает обмен. */
    fun pick(characterId: String) {
        val mode = context.current.reorder ?: return
        // пока идёт обмен, второй не начинается: он прочитал бы ещё не записанные портреты
        if (swapping) return
        val pair = mode.pairWith(characterId)
        if (pair == null) context.update { it.copy(reorder = mode.toggled(characterId)) } else swap(pair.first, pair.second)
    }

    private fun swap(first: String, second: String) {
        // режим остаётся включённым: можно сразу выбирать следующую пару
        context.update { state -> state.copy(reorder = state.reorder?.let { ReorderState() }) }
        val previous = context.current.portraits
            .filter { it.characterId == first || it.characterId == second }
            .mapNotNull { item -> item.image?.let { item.characterId to it } }
            .toMap()
        swapping = true
        val job = context.launch {
            val (faces, writes) = swappedPortraits(first, second)
            try {
                setPhase(SwapAnimation(first, second, SwapAnimation.Phase.Out))
                delay(SwapAnimation.OUT_MS.toLong())
                context.update { it.withPortraitImages(faces) }
                setPhase(SwapAnimation(first, second, SwapAnimation.Phase.In))
                delay(SwapAnimation.IN_MS.toLong())
                setPhase(null)
                // файлы пишутся только после анимации: на сайте всё идёт в одном потоке, и чтение с записью
                // портретов по полтора мегабайта отнимали бы у неё кадры
                context.showNotice(Notice(NoticeKind.Swapped, context.saveQuietly(writes)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // файлы не записаны — картинки на странице возвращаются на свои места
                context.update { it.withPortraitImages(previous) }
                throw e
            } finally {
                setPhase(null)
            }
        }
        job.invokeOnCompletion { swapping = false }
    }

    /** Персонажи обмениваются чистыми основами, а надпись и значок каждого остаются на его портрете. */
    private suspend fun swappedPortraits(first: String, second: String): Pair<Map<String, ImageBitmap>, List<PortraitWrite>> {
        val a = context.portraits.currentPortrait(first)
        val b = context.portraits.currentPortrait(second)
        val (faceA, writeA) = context.compose(first, StoredPortrait(b.clean, a.decoration))
        val (faceB, writeB) = context.compose(second, StoredPortrait(a.clean, b.decoration))
        return mapOf(first to faceA, second to faceB) to listOf(writeA, writeB)
    }

    private fun setPhase(animation: SwapAnimation?) = context.update { it.copy(swapAnimation = animation) }
}
