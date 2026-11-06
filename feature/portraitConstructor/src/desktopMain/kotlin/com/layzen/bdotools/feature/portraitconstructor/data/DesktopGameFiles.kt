package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.GameFolderSettings

/** Файлы игры и источник картинок настольной версии в одном объекте, как того ждёт Koin-модуль фичи. */
internal class DesktopGameFiles(storage: PortraitsStorage, images: ImageSource) :
    GameFiles,
    PortraitsStorage by storage,
    ImageSource by images

internal actual fun createGameFiles(): GameFiles {
    val dialogs = SystemDialogs()
    return DesktopGameFiles(
        storage = DesktopPortraitsStorage(GameFolderSettings(), dialogs::pickFolder),
        images = DesktopImageSource(dialogs),
    )
}
