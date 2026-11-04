package com.layzen.bdotools.feature.portraits.presentation.controller

import com.layzen.bdotools.feature.portraits.presentation.PortraitsContext
import com.layzen.bdotools.feature.portraits.presentation.state.Notice
import com.layzen.bdotools.feature.portraits.presentation.state.NoticeKind
import com.layzen.bdotools.feature.portraits.presentation.state.ReorderState
import com.layzen.bdotools.feature.portraits.presentation.state.SwapAnimation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * Изменение порядка прямо на сетке: портреты затемняются, пользователь выбирает два — и они сразу
 * обмениваются картинками вместе с надписью и значками. Нужно, когда в окне выбора персонажа игры
 * персонажей переставили и общая картинка на портретах разъехалась. Старые портреты уходят в резервную копию.
 * Запись идёт без окна ожидания, одновременно с анимацией: файлов всего два.
 */
internal class ReorderController(private val context: PortraitsContext) {

    fun start() = context.update { it.copy(reorder = ReorderState()) }

    fun cancel() = context.update { it.copy(reorder = null) }

    fun pick(characterId: String) {
        val mode = context.current.reorder ?: return
        val pair = mode.pairWith(characterId)
        if (pair == null) context.update { it.copy(reorder = mode.toggled(characterId)) } else swap(pair.first, pair.second)
    }

    private fun swap(first: String, second: String) {
        cancel()
        context.launch {
            var imagesSwapped = false
            try {
                coroutineScope {
                    val backup = async {
                        context.saveQuietly(context.repository.swapWrites(first, second, context.current.decorations))
                    }
                    setPhase(SwapAnimation(first, second, SwapAnimation.Phase.Out))
                    delay(SwapAnimation.OUT_MS.toLong())
                    swapImages(first, second)
                    imagesSwapped = true
                    setPhase(SwapAnimation(first, second, SwapAnimation.Phase.In))
                    delay(SwapAnimation.IN_MS.toLong())
                    setPhase(null)
                    context.showNotice(Notice(NoticeKind.Swapped, backup.await()))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // файлы не записаны — картинки на странице возвращаются на свои места
                if (imagesSwapped) swapImages(first, second)
                throw e
            } finally {
                setPhase(null)
            }
        }
    }

    private fun setPhase(animation: SwapAnimation?) = context.update { it.copy(swapAnimation = animation) }

    private fun swapImages(first: String, second: String) = context.update { state ->
        val images = state.portraits.associate { it.characterId to it.image }
        val a = images[first]
        val b = images[second]
        // обмен доступен только после загрузки всех портретов, так что обе картинки уже есть
        if (a == null || b == null) state else state.withPortraitImages(mapOf(first to b, second to a))
    }
}
