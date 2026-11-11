package com.layzen.bdotools.feature.portraitconstructor.presentation

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.backup.BackupsInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.favorite.FavoritesInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitWrite
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitsInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.StoredPortrait
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitImages
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.toBmp
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.Busy
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.BusyKind
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.Notice
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitsState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Пауза между тяжёлыми шагами. В браузере всё выполняется в одном потоке: без паузы страница
 * не успевает перерисоваться, и индикатор ожидания замирает вместе с интерфейсом.
 */
private const val FRAME_PAUSE_MS = 16L

/**
 * Общее для всех частей страницы портретов: состояние, ожидание с прогрессом, обработка ошибок
 * и запись портретов. Контроллеры работают только через него и не знают друг о друге.
 */
internal class PortraitsContext(
    private val scope: CoroutineScope,
    val portraits: PortraitsInteractor,
    val backups: BackupsInteractor,
    val favorites: FavoritesInteractor,
    val images: PortraitImages,
) {
    private val _state = MutableStateFlow(PortraitsState())
    /** Состояние страницы для интерфейса; меняется только через [update]. */
    val state: StateFlow<PortraitsState> = _state.asStateFlow()

    /** Текущее значение [state] для чтения внутри контроллеров. */
    val current: PortraitsState get() = _state.value

    /** Атомарно заменяет состояние результатом [transform]. */
    fun update(transform: (PortraitsState) -> PortraitsState) = _state.update(transform)

    /** Показывает сообщение; `null` игнорируется, убирает сообщение только событие закрытия. */
    fun showNotice(notice: Notice?) {
        if (notice != null) update { it.copy(notice = notice) }
    }

    /** Запускает работу с файлами; ошибка показывается сообщением, отмена выбора — молча. */
    fun launch(onError: (Exception) -> Unit = { showNotice(it.toNotice()) }, block: suspend () -> Unit): Job =
        scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError(e)
            }
        }

    /** Показывает окно ожидания [kind] на время [block]. */
    suspend fun <T> withBusy(kind: BusyKind, block: suspend () -> T): T {
        update { it.copy(busy = Busy(kind)) }
        breathe()
        try {
            return block()
        } finally {
            update { if (it.busy?.kind == kind) it.copy(busy = null) else it }
        }
    }

    /** Обновляет прогресс окна ожидания: сделано [done] из [total]. */
    suspend fun setProgress(done: Int, total: Int) {
        update { it.copy(busy = it.busy?.copy(done = done, total = total)) }
        breathe()
    }

    /** Даёт браузеру перерисовать страницу между тяжёлыми шагами. */
    suspend fun breathe() = delay(FRAME_PAUSE_MS)

    /** Портрет персонажа [characterId] из сохранённых основы и украшения: картинка для сетки и запись для файлов. */
    suspend fun compose(characterId: String, stored: StoredPortrait): Pair<ImageBitmap, PortraitWrite> {
        val face = images.render(stored)
        val faceBytes = if (stored.decoration.isEmpty) stored.clean else face.toBmp()
        return face to PortraitWrite(characterId, faceBytes, stored.clean, stored.decoration)
    }

    /** Записывает портреты с прогрессом и обновляет украшения и список копий. Возвращает путь к копии. */
    suspend fun save(writes: List<PortraitWrite>): String = withBusy(BusyKind.Saving) { write(writes) }

    /** То же без окна ожидания — для коротких записей, которые идут вместе с анимацией. */
    suspend fun saveQuietly(writes: List<PortraitWrite>): String = write(writes)

    private suspend fun write(writes: List<PortraitWrite>): String {
        setProgress(0, writes.size)
        val backup = portraits.writePortraits(writes) { done, total -> setProgress(done, total) }
        val decorations = current.decorations.toMutableMap()
        writes.forEach {
            if (it.decoration.isEmpty) decorations -= it.characterId else decorations[it.characterId] = it.decoration
        }
        val backups = backups.backups()
        update { it.copy(decorations = decorations, backups = backups) }
        return backup
    }
}
