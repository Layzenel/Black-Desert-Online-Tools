package com.layzen.bdotools.feature.portraits.data

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
