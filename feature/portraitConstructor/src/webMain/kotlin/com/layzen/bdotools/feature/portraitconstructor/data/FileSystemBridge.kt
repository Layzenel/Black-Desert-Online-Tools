package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.FolderBridge

/** Мост портретов: папку игры узнаёт по `FaceTexture`, ошибки бросает как [PortraitsException]. */
internal val fileSystemBridge = FolderBridge(
    name = "__bdoPortraits",
    requiredFolder = "FaceTexture",
    script = BRIDGE_SOURCE,
    failure = ::portraitsException,
)

private fun portraitsException(code: String) = PortraitsException(
    error = when (code.substringBefore(':')) {
        "CANCELLED" -> PortraitsError.Cancelled
        "NOT_GAME_FOLDER" -> PortraitsError.NotGameFolder
        "PERMISSION" -> PortraitsError.Permission
        "BLOCKED" -> PortraitsError.Blocked
        "NETWORK" -> PortraitsError.Network
        "NOT_IMAGE" -> PortraitsError.NotImage
        else -> PortraitsError.Unknown
    },
    details = code,
)
