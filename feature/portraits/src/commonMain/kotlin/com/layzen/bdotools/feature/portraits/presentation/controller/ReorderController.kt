package com.layzen.bdotools.feature.portraits.presentation.controller

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraits.data.PortraitWrite
import com.layzen.bdotools.feature.portraits.data.StoredPortrait
import com.layzen.bdotools.feature.portraits.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraits.presentation.state.Notice
import com.layzen.bdotools.feature.portraits.presentation.state.NoticeKind
import com.layzen.bdotools.feature.portraits.presentation.state.ReorderState
import com.layzen.bdotools.feature.portraits.presentation.state.SwapAnimation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Изменение порядка прямо на сетке: портреты затемняются, пользователь выбирает два — и они сразу
 * обмениваются картинками, а надпись и значок остаются на своих местах. Нужно, когда в окне выбора персонажа игры
 * персонажей переставили и общая картинка на портретах разъехалась. Старые портреты уходят в резервную копию.
 * Запись идёт без окна ожидания сразу после анимации: файлов всего два.
 */
internal class ReorderController(private val context: PortraitsContext) {

    fun start() = context.update { it.copy(reorder = ReorderState(), notice = Notice(NoticeKind.ReorderHint)) }

    fun cancel() = context.update { it.copy(reorder = null) }

    fun pick(characterId: String) {
        val mode = context.current.reorder ?: return
        val pair = mode.pairWith(characterId)
        if (pair == null) context.update { it.copy(reorder = mode.toggled(characterId)) } else swap(pair.first, pair.second)
    }

    private fun swap(first: String, second: String) {
        cancel()
        val previous = context.current.portraits
            .filter { it.characterId == first || it.characterId == second }
            .mapNotNull { item -> item.image?.let { item.characterId to it } }
            .toMap()
        context.launch {
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
    }

    /** Персонажи обмениваются чистыми основами, а надпись и значок каждого остаются на его портрете. */
    private suspend fun swappedPortraits(first: String, second: String): Pair<Map<String, ImageBitmap>, List<PortraitWrite>> {
        val a = context.repository.currentPortrait(first)
        val b = context.repository.currentPortrait(second)
        val (faceA, writeA) = context.compose(first, StoredPortrait(b.clean, a.decoration))
        val (faceB, writeB) = context.compose(second, StoredPortrait(a.clean, b.decoration))
        return mapOf(first to faceA, second to faceB) to listOf(writeA, writeB)
    }

    private fun setPhase(animation: SwapAnimation?) = context.update { it.copy(swapAnimation = animation) }
}
