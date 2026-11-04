package com.layzen.bdotools.feature.slots.data

import com.layzen.bdotools.core.folder.GameFolderSettings

/** Файлы игры и файлы компьютера настольной версии в одном объекте, как того ждёт Koin-модуль фичи. */
internal class DesktopSlotsFiles(storage: SlotsStorage, device: DeviceFiles) :
    SlotsFiles,
    SlotsStorage by storage,
    DeviceFiles by device

internal actual fun createSlotsFiles(): SlotsFiles {
    val dialogs = SystemDialogs()
    return DesktopSlotsFiles(
        storage = DesktopSlotsStorage(GameFolderSettings(), dialogs::pickFolder),
        device = DesktopDeviceFiles(dialogs),
    )
}
