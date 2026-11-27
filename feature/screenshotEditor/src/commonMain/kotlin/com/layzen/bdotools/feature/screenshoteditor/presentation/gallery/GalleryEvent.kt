package com.layzen.bdotools.feature.screenshoteditor.presentation.gallery

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry

/** Действия в галерее. Открыть скриншот — дело редактора, а не галереи. */
sealed interface GalleryEvent {
    data object Refresh : GalleryEvent
    data object SelectFolder : GalleryEvent
    data object RequestPermission : GalleryEvent
    data class LoadThumbnail(val entry: ScreenshotEntry) : GalleryEvent
    data class Reveal(val entry: ScreenshotEntry) : GalleryEvent
    data class Trash(val entry: ScreenshotEntry) : GalleryEvent
    data object DismissNotice : GalleryEvent
}
