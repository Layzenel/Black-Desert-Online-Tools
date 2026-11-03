package com.layzen.bdotools.feature.portraits.presentation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.feature.portraits.data.FolderAccess
import com.layzen.bdotools.feature.portraits.data.ImageSource
import com.layzen.bdotools.feature.portraits.data.PortraitWrite
import com.layzen.bdotools.feature.portraits.data.PortraitsError
import com.layzen.bdotools.feature.portraits.data.PortraitsException
import com.layzen.bdotools.feature.portraits.data.PortraitsRepository
import com.layzen.bdotools.feature.portraits.data.PortraitsStorage
import com.layzen.bdotools.feature.portraits.domain.BackupInfo
import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.domain.BadgeSlot
import com.layzen.bdotools.feature.portraits.domain.ImagePlacement
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.domain.PortraitGrid
import com.layzen.bdotools.feature.portraits.domain.containing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Пауза между тяжёлыми шагами. В браузере всё выполняется в одном потоке: без паузы страница
 * не успевает перерисоваться, и индикатор ожидания замирает вместе с интерфейсом.
 */
private const val FRAME_PAUSE_MS = 16L

class PortraitItem(val characterId: String, val image: ImageBitmap)

/** Что заменяем: все портреты сразу или один. */
sealed interface ReplaceTarget {
    data object All : ReplaceTarget
    data class One(val characterId: String) : ReplaceTarget
}

data class SourceDialogState(
    val target: ReplaceTarget,
    val url: String = "",
    val isLoading: Boolean = false,
    val error: Notice? = null,
)

data class EditorState(
    val target: ReplaceTarget,
    val image: ImageBitmap,
    val grid: PortraitGrid,
    val initialPlacement: ImagePlacement,
    val placement: ImagePlacement = initialPlacement,
)

enum class DecorationMode { Caption, Badges }

/** Окно надписи или значков. [base] — портрет без украшений, `null` пока загружается. */
data class DecorationDialogState(
    val characterId: String,
    val mode: DecorationMode,
    val draft: PortraitDecoration,
    val base: ImageBitmap? = null,
)

/**
 * Окно восстановления из резервной копии.
 * Для всех портретов: [selected] — выбранная копия, [checked] — какие портреты из неё вернуть.
 * Для одного портрета: [backups] — только копии с этим персонажем, [selected] — выбранная версия.
 * [previews] — уменьшенные портреты по ключу [previewKey].
 * [marked] — копии, отмеченные галочкой для удаления (только в режиме всех портретов).
 */
data class RestoreDialogState(
    val target: ReplaceTarget,
    val backups: List<BackupInfo>,
    val selected: String?,
    val checked: Set<String> = emptySet(),
    val previews: Map<String, ImageBitmap> = emptyMap(),
    val isLoadingPreviews: Boolean = false,
    /** Копии, отмеченные в списке для удаления. */
    val marked: Set<String> = emptySet(),
    val confirmDelete: Boolean = false,
) {
    val selectedBackup: BackupInfo? get() = backups.firstOrNull { it.name == selected }
}

fun previewKey(backup: String, characterId: String) = "$backup/$characterId"

enum class BusyKind { Loading, Rendering, Saving, Restoring, Deleting }

/** Долгая операция: показывается окном ожидания с прогрессом [done] из [total]. */
data class Busy(val kind: BusyKind, val done: Int = 0, val total: Int = 0) {
    val progress: Float? get() = if (total > 0) done.toFloat() / total else null
}

enum class NoticeKind {
    Saved,
    DecorationSaved,
    Restored,
    BackupsDeleted,
    NotGameFolder,
    Permission,
    Blocked,
    Network,
    NotImage,
    Unknown,
}

/** Сообщение пользователю. [argument] подставляется в текст (путь к копии, описание ошибки). */
data class Notice(val kind: NoticeKind, val argument: String = "") {
    val isError: Boolean get() = kind !in setOf(NoticeKind.Saved, NoticeKind.DecorationSaved, NoticeKind.Restored, NoticeKind.BackupsDeleted)
}

data class PortraitsState(
    /** `null`, пока проверяется доступ к папке. */
    val access: FolderAccess? = null,
    val portraits: List<PortraitItem> = emptyList(),
    val decorations: Map<String, PortraitDecoration> = emptyMap(),
    val backups: List<BackupInfo> = emptyList(),
    val sourceDialog: SourceDialogState? = null,
    val editor: EditorState? = null,
    val decorationDialog: DecorationDialogState? = null,
    val restoreDialog: RestoreDialogState? = null,
    val busy: Busy? = null,
    val notice: Notice? = null,
) {
    fun hasBackups(characterId: String) = backups.any { characterId in it.characterIds }
}

sealed interface PortraitsEvent {
    data object SelectFolder : PortraitsEvent
    data object RequestPermission : PortraitsEvent
    data object Reload : PortraitsEvent
    data object DismissNotice : PortraitsEvent

    /** Отрисовщик украшений живёт в композиции (ему нужны шрифты и SVG) и передаётся сюда при каждом изменении. */
    data class DecoratorChanged(val decorator: PortraitDecorator) : PortraitsEvent

    data class OpenSource(val target: ReplaceTarget) : PortraitsEvent
    data object CloseSource : PortraitsEvent
    data class UrlChanged(val url: String) : PortraitsEvent
    data object PickFromDevice : PortraitsEvent
    data object LoadFromUrl : PortraitsEvent

    /** Сдвиг в пикселях маски. */
    data class MoveImage(val dx: Float, val dy: Float) : PortraitsEvent
    data class ZoomImage(val scale: Float) : PortraitsEvent
    data class RotateImage(val degrees: Float) : PortraitsEvent
    data object ResetPlacement : PortraitsEvent
    data object CloseEditor : PortraitsEvent
    data object Apply : PortraitsEvent

    data class OpenDecoration(val characterId: String, val mode: DecorationMode) : PortraitsEvent
    data class CaptionChanged(val text: String) : PortraitsEvent
    data class BadgeChanged(val slot: BadgeSlot, val icon: BadgeIcon?) : PortraitsEvent
    data object CloseDecoration : PortraitsEvent
    data object ApplyDecoration : PortraitsEvent

    data class OpenRestore(val target: ReplaceTarget) : PortraitsEvent
    data class SelectBackup(val name: String) : PortraitsEvent
    data class ToggleRestorePortrait(val characterId: String) : PortraitsEvent
    data class CheckAllRestorePortraits(val checked: Boolean) : PortraitsEvent
    data object CloseRestore : PortraitsEvent
    data object ApplyRestore : PortraitsEvent

    data class ToggleBackupMark(val name: String) : PortraitsEvent
    data object RequestDeleteBackups : PortraitsEvent
    data object CancelDeleteBackups : PortraitsEvent
    data object ConfirmDeleteBackups : PortraitsEvent
}

class PortraitsViewModel(
    private val storage: PortraitsStorage,
    private val imageSource: ImageSource,
    private val repository: PortraitsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PortraitsState())
    val state: StateFlow<PortraitsState> = _state.asStateFlow()

    private val decorator = MutableStateFlow<PortraitDecorator?>(null)

    /** Байты чистого портрета для открытого окна украшений: их сохраняем как основу без перекодирования. */
    private var dialogBaseBytes: ByteArray? = null

    private var previewJob: Job? = null

    init {
        launchSafely {
            val access = storage.access()
            _state.update { it.copy(access = access) }
            if (access is FolderAccess.Granted) loadPortraits()
        }
    }

    fun onEvent(event: PortraitsEvent) {
        when (event) {
            PortraitsEvent.SelectFolder -> launchSafely { onAccessGranted(storage.selectFolder()) }
            PortraitsEvent.RequestPermission -> launchSafely { onAccessGranted(storage.requestPermission()) }
            PortraitsEvent.Reload -> launchSafely { loadPortraits() }
            PortraitsEvent.DismissNotice -> _state.update { it.copy(notice = null) }
            is PortraitsEvent.DecoratorChanged -> decorator.value = event.decorator

            is PortraitsEvent.OpenSource -> _state.update { it.copy(sourceDialog = SourceDialogState(event.target)) }
            PortraitsEvent.CloseSource -> _state.update { it.copy(sourceDialog = null) }
            is PortraitsEvent.UrlChanged -> updateSource { it.copy(url = event.url, error = null) }
            PortraitsEvent.PickFromDevice -> loadImage { imageSource.pickFromDevice() }
            PortraitsEvent.LoadFromUrl -> {
                val url = _state.value.sourceDialog?.url?.trim().orEmpty()
                if (url.isNotEmpty()) loadImage { imageSource.download(url) }
            }

            is PortraitsEvent.MoveImage -> updatePlacement { it.movedBy(event.dx, event.dy) }
            is PortraitsEvent.ZoomImage -> updatePlacement { it.zoomedTo(event.scale) }
            is PortraitsEvent.RotateImage -> updatePlacement { it.rotatedTo(event.degrees) }
            PortraitsEvent.ResetPlacement -> updateEditor { it.copy(placement = it.initialPlacement) }
            PortraitsEvent.CloseEditor -> _state.update { it.copy(editor = null) }
            PortraitsEvent.Apply -> applyReplacement()

            is PortraitsEvent.OpenDecoration -> openDecoration(event.characterId, event.mode)
            is PortraitsEvent.CaptionChanged -> updateDraft { it.withCaption(event.text) }
            is PortraitsEvent.BadgeChanged -> updateDraft { it.withBadge(event.slot, event.icon) }
            PortraitsEvent.CloseDecoration -> closeDecoration()
            PortraitsEvent.ApplyDecoration -> applyDecoration()

            is PortraitsEvent.OpenRestore -> openRestore(event.target)
            is PortraitsEvent.SelectBackup -> selectBackup(event.name)
            is PortraitsEvent.ToggleRestorePortrait -> updateRestore {
                it.copy(checked = if (event.characterId in it.checked) it.checked - event.characterId else it.checked + event.characterId)
            }
            is PortraitsEvent.CheckAllRestorePortraits -> updateRestore {
                it.copy(checked = if (event.checked) it.selectedBackup?.characterIds.orEmpty() else emptySet())
            }
            PortraitsEvent.CloseRestore -> closeRestore()
            PortraitsEvent.ApplyRestore -> applyRestore()
            is PortraitsEvent.ToggleBackupMark -> updateRestore {
                it.copy(marked = if (event.name in it.marked) it.marked - event.name else it.marked + event.name)
            }
            PortraitsEvent.RequestDeleteBackups -> updateRestore { if (it.marked.isEmpty()) it else it.copy(confirmDelete = true) }
            PortraitsEvent.CancelDeleteBackups -> updateRestore { it.copy(confirmDelete = false) }
            PortraitsEvent.ConfirmDeleteBackups -> deleteMarkedBackups()
        }
    }

    private suspend fun onAccessGranted(access: FolderAccess.Granted) {
        _state.update { it.copy(access = access, notice = null) }
        loadPortraits()
    }

    private suspend fun loadPortraits() = withBusy(BusyKind.Loading) {
        val ids = repository.portraitOrder()
        setProgress(0, ids.size)
        val portraits = ids.mapIndexed { index, id ->
            val image = repository.readPortrait(id).decodeToImageBitmap()
            setProgress(index + 1, ids.size)
            PortraitItem(id, image)
        }
        val decorations = repository.loadDecorations()
        val backups = repository.backups()
        _state.update { it.copy(portraits = portraits, decorations = decorations, backups = backups) }
    }

    private fun loadImage(load: suspend () -> ByteArray?) {
        val dialog = _state.value.sourceDialog ?: return
        viewModelScope.launch {
            updateSource { it.copy(isLoading = true, error = null) }
            try {
                val bytes = load() ?: return@launch updateSource { it.copy(isLoading = false) }
                breathe()
                val image = decodeImage(bytes)
                val grid = when (dialog.target) {
                    ReplaceTarget.All -> PortraitGrid(count = _state.value.portraits.size)
                    is ReplaceTarget.One -> PortraitGrid.single()
                }
                val placement = ImagePlacement.cover(image.width, image.height, grid)
                _state.update {
                    it.copy(sourceDialog = null, editor = EditorState(dialog.target, image, grid, placement))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateSource { it.copy(isLoading = false, error = e.toNotice()) }
            }
        }
    }

    /** Новые портреты становятся чистой основой; надпись и значки персонажа рисуются поверх них заново. */
    private fun applyReplacement() {
        val editor = _state.value.editor ?: return
        val ids = when (editor.target) {
            ReplaceTarget.All -> _state.value.portraits.map { it.characterId }
            is ReplaceTarget.One -> listOf(editor.target.characterId)
        }
        launchSafely {
            _state.update { it.copy(editor = null) }
            val faces = mutableMapOf<String, ImageBitmap>()
            val writes = withBusy(BusyKind.Rendering) {
                ids.mapIndexed { index, id ->
                    setProgress(index, ids.size)
                    val clean = PortraitRenderer.renderCell(editor.image, editor.placement, editor.grid.cell(index))
                    val face = decorated(clean, decorationOf(id))
                    faces[id] = face
                    PortraitWrite(id, face = face.toBmp(), cleanBase = clean.toBmp(), decoration = decorationOf(id))
                }
            }
            val backup = save(writes)
            _state.update { state ->
                state.copy(portraits = state.portraits.withImages(faces), notice = Notice(NoticeKind.Saved, backup))
            }
        }
    }

    private fun openDecoration(characterId: String, mode: DecorationMode) {
        _state.update {
            it.copy(decorationDialog = DecorationDialogState(characterId, mode, draft = decorationOf(characterId)))
        }
        viewModelScope.launch {
            try {
                val bytes = repository.readCleanBase(characterId)
                breathe()
                val base = decodeImage(bytes)
                dialogBaseBytes = bytes
                updateDialog { if (it.characterId == characterId) it.copy(base = base) else it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                closeDecoration()
                e.toNotice()?.let { notice -> _state.update { it.copy(notice = notice) } }
            }
        }
    }

    private fun closeDecoration() {
        dialogBaseBytes = null
        _state.update { it.copy(decorationDialog = null) }
    }

    private fun applyDecoration() {
        val dialog = _state.value.decorationDialog ?: return
        val base = dialog.base ?: return
        val baseBytes = dialogBaseBytes ?: return
        val decoration = dialog.draft.withCaption(dialog.draft.caption.trim())
        launchSafely {
            closeDecoration()
            val face = withBusy(BusyKind.Rendering) { decorated(base, decoration) }
            val faceBytes = if (decoration.isEmpty) baseBytes else face.toBmp()
            val backup = save(listOf(PortraitWrite(dialog.characterId, faceBytes, baseBytes, decoration)))
            _state.update { state ->
                state.copy(
                    portraits = state.portraits.withImages(mapOf(dialog.characterId to face)),
                    notice = Notice(NoticeKind.DecorationSaved, backup),
                )
            }
        }
    }

    private fun openRestore(target: ReplaceTarget) {
        val all = _state.value.backups
        when (target) {
            ReplaceTarget.All -> {
                _state.update { it.copy(restoreDialog = RestoreDialogState(target, all, selected = null)) }
                all.firstOrNull()?.let { selectBackup(it.name) }
            }
            is ReplaceTarget.One -> {
                val backups = all.containing(target.characterId)
                _state.update {
                    it.copy(restoreDialog = RestoreDialogState(target, backups, selected = backups.firstOrNull()?.name))
                }
                loadPreviews(backups.map { it.name to target.characterId })
            }
        }
    }

    /** В режиме всех портретов выбирает копию, отмечает её портреты текущих персонажей и грузит превью. */
    private fun selectBackup(name: String) {
        val dialog = _state.value.restoreDialog ?: return
        val backup = dialog.backups.firstOrNull { it.name == name } ?: return
        if (dialog.target is ReplaceTarget.One) {
            updateRestore { it.copy(selected = name) }
            return
        }
        val current = _state.value.portraits.map { it.characterId }
        val ids = current.filter { it in backup.characterIds } + backup.characterIds.filterNot { it in current }.sorted()
        updateRestore { it.copy(selected = name, checked = backup.characterIds.intersect(current.toSet())) }
        loadPreviews(ids.map { name to it })
    }

    /** Превью грузятся по одному в фоне: окно открывается сразу, портреты появляются по мере чтения. */
    private fun loadPreviews(items: List<Pair<String, String>>) {
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            updateRestore { it.copy(isLoadingPreviews = true) }
            try {
                items.forEach { (backup, id) ->
                    val key = previewKey(backup, id)
                    if (_state.value.restoreDialog?.previews?.containsKey(key) == true) return@forEach
                    val image = repository.readBackupPortrait(backup, id).decodeToImageBitmap()
                    updateRestore { it.copy(previews = it.previews + (key to image)) }
                    breathe()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.toNotice()?.let { notice -> _state.update { it.copy(notice = notice) } }
            } finally {
                updateRestore { it.copy(isLoadingPreviews = false) }
            }
        }
    }

    private fun closeRestore() {
        previewJob?.cancel()
        _state.update { it.copy(restoreDialog = null) }
    }

    /** Портрет из копии становится новой чистой основой: надпись и значки персонажа сбрасываются. */
    private fun applyRestore() {
        val dialog = _state.value.restoreDialog ?: return
        val backup = dialog.selected ?: return
        val ids = when (val target = dialog.target) {
            ReplaceTarget.All -> dialog.checked.toList()
            is ReplaceTarget.One -> listOf(target.characterId)
        }
        if (ids.isEmpty()) return
        launchSafely {
            closeRestore()
            val faces = mutableMapOf<String, ImageBitmap>()
            val writes = withBusy(BusyKind.Restoring) {
                ids.mapIndexed { index, id ->
                    setProgress(index, ids.size)
                    val bytes = repository.readBackupPortrait(backup, id)
                    faces[id] = bytes.decodeToImageBitmap()
                    PortraitWrite(id, face = bytes, cleanBase = bytes, decoration = PortraitDecoration())
                }
            }
            val copy = save(writes)
            _state.update { state ->
                state.copy(
                    portraits = state.portraits.withImages(faces),
                    notice = Notice(NoticeKind.Restored, copy),
                )
            }
        }
    }

    /**
     * Удаляет отмеченные копии. Окно копий остаётся открытым со свежим списком; если выбранная копия
     * удалена, выбирается самая новая из оставшихся.
     */
    private fun deleteMarkedBackups() {
        val dialog = _state.value.restoreDialog ?: return
        val names = dialog.marked.toList()
        if (names.isEmpty()) return
        updateRestore { it.copy(confirmDelete = false) }
        launchSafely {
            val left = withBusy(BusyKind.Deleting) {
                setProgress(0, names.size)
                repository.deleteBackups(names) { done, total -> setProgress(done, total) }
            }
            val selectedLeft = dialog.selected?.takeIf { name -> left.any { it.name == name } }
            _state.update { state ->
                state.copy(
                    backups = left,
                    restoreDialog = state.restoreDialog?.copy(
                        backups = left,
                        marked = emptySet(),
                        selected = selectedLeft,
                        checked = if (selectedLeft == null) emptySet() else state.restoreDialog.checked,
                        previews = state.restoreDialog.previews.filterKeys { key -> key.substringBefore('/') !in names },
                    ),
                    notice = Notice(NoticeKind.BackupsDeleted, names.size.toString()),
                )
            }
            if (selectedLeft == null) left.firstOrNull()?.let { selectBackup(it.name) }
        }
    }

    /** Записывает портреты с прогрессом, затем обновляет список украшений и резервных копий. */
    private suspend fun save(writes: List<PortraitWrite>): String = withBusy(BusyKind.Saving) {
        setProgress(0, writes.size)
        val backup = repository.writePortraits(writes) { done, total -> setProgress(done, total) }
        val decorations = _state.value.decorations.toMutableMap()
        writes.forEach { if (it.decoration.isEmpty) decorations -= it.characterId else decorations[it.characterId] = it.decoration }
        val backups = repository.backups()
        _state.update { it.copy(decorations = decorations, backups = backups) }
        backup
    }

    private suspend fun <T> withBusy(kind: BusyKind, block: suspend () -> T): T {
        _state.update { it.copy(busy = Busy(kind)) }
        breathe()
        try {
            return block()
        } finally {
            _state.update { if (it.busy?.kind == kind) it.copy(busy = null) else it }
        }
    }

    private suspend fun setProgress(done: Int, total: Int) {
        _state.update { state -> state.copy(busy = state.busy?.copy(done = done, total = total)) }
        breathe()
    }

    private suspend fun breathe() = delay(FRAME_PAUSE_MS)

    private suspend fun decorated(clean: ImageBitmap, decoration: PortraitDecoration): ImageBitmap {
        if (decoration.isEmpty) return clean
        val ready = decorator.filterNotNull().first { it.isReady }
        return ready.decorate(clean, decoration)
    }

    private fun decorationOf(characterId: String) = _state.value.decorations[characterId] ?: PortraitDecoration()

    private fun List<PortraitItem>.withImages(images: Map<String, ImageBitmap>) =
        map { item -> images[item.characterId]?.let { PortraitItem(item.characterId, it) } ?: item }

    private fun decodeImage(bytes: ByteArray): ImageBitmap =
        try {
            bytes.decodeToImageBitmap()
        } catch (e: Exception) {
            throw PortraitsException(PortraitsError.NotImage, e.message)
        }

    private fun updateSource(transform: (SourceDialogState) -> SourceDialogState) =
        _state.update { state -> state.copy(sourceDialog = state.sourceDialog?.let(transform)) }

    private fun updateEditor(transform: (EditorState) -> EditorState) =
        _state.update { state -> state.copy(editor = state.editor?.let(transform)) }

    private fun updateDialog(transform: (DecorationDialogState) -> DecorationDialogState) =
        _state.update { state -> state.copy(decorationDialog = state.decorationDialog?.let(transform)) }

    private fun updateRestore(transform: (RestoreDialogState) -> RestoreDialogState) =
        _state.update { state -> state.copy(restoreDialog = state.restoreDialog?.let(transform)) }

    private fun updateDraft(transform: (PortraitDecoration) -> PortraitDecoration) =
        updateDialog { it.copy(draft = transform(it.draft)) }

    private fun updatePlacement(transform: (ImagePlacement) -> ImagePlacement) =
        updateEditor { it.copy(placement = transform(it.placement)) }

    /** Запускает работу с файлами; ошибка показывается сообщением, а отмена выбора папки — молча. */
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val notice = e.toNotice()
                if (notice != null) _state.update { it.copy(notice = notice) }
            }
        }
    }

    private fun Exception.toNotice(): Notice? = when ((this as? PortraitsException)?.error) {
        PortraitsError.Cancelled -> null
        PortraitsError.NotGameFolder -> Notice(NoticeKind.NotGameFolder)
        PortraitsError.Permission -> Notice(NoticeKind.Permission)
        PortraitsError.Blocked -> Notice(NoticeKind.Blocked)
        PortraitsError.Network -> Notice(NoticeKind.Network)
        PortraitsError.NotImage -> Notice(NoticeKind.NotImage)
        PortraitsError.Unknown, null -> Notice(NoticeKind.Unknown, message.orEmpty())
    }
}
