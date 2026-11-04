package com.layzen.bdotools.feature.slots.data

import com.layzen.bdotools.core.folder.FolderBridge

/** Мост слотов: папку игры узнаёт по `UserCache`, ошибки бросает как [SlotsException]. */
internal val slotsBridge = FolderBridge(
    name = "__bdoSlots",
    requiredFolder = "UserCache",
    script = SLOTS_BRIDGE_SOURCE,
    failure = ::slotsException,
)

private fun slotsException(code: String) = SlotsException(
    error = when (code.substringBefore(':')) {
        "CANCELLED" -> SlotsError.Cancelled
        "NOT_GAME_FOLDER" -> SlotsError.NotGameFolder
        "PERMISSION" -> SlotsError.Permission
        "FILE_CHANGED" -> SlotsError.FileChanged
        else -> SlotsError.Unknown
    },
    details = code,
)
