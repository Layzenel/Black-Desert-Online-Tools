package com.layzen.bdotools.feature.screenshoteditor.domain.file

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.isGameScreenshotName

/** Сценарии галереи скриншотов. Методы — см. одноимённые у [ScreenshotGallery]. */
class GalleryInteractor(private val gallery: ScreenshotGallery) {
    /** См. [ScreenshotGallery.canManageFiles]. */
    val canManageFiles: Boolean get() = gallery.canManageFiles

    /** См. [ScreenshotGallery.access]. */
    suspend fun access(): FolderAccess = gallery.access()

    /** См. [ScreenshotGallery.selectFolder]. */
    suspend fun selectFolder(): FolderAccess = gallery.selectFolder()

    /** См. [ScreenshotGallery.requestPermission]. */
    suspend fun requestPermission(): FolderAccess = gallery.requestPermission()

    /** Скриншоты игры, новые первыми; копии и чужие файлы в папке не показываются. */
    suspend fun list(): List<ScreenshotEntry> =
        gallery.list().filter { isGameScreenshotName(it.name) }
            .sortedWith(compareByDescending<ScreenshotEntry> { it.modified }.thenByDescending { it.name })

    /** См. [ScreenshotGallery.thumbnail]. */
    suspend fun thumbnail(entry: ScreenshotEntry): ByteArray = gallery.thumbnail(entry)

    /** См. [ScreenshotGallery.reveal]. */
    suspend fun reveal(entry: ScreenshotEntry) = gallery.reveal(entry)

    /** См. [ScreenshotGallery.moveToTrash]. */
    suspend fun moveToTrash(entry: ScreenshotEntry) = gallery.moveToTrash(entry)
}
