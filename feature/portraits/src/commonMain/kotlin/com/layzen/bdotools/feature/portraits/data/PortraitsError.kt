package com.layzen.bdotools.feature.portraits.data

/** Что пошло не так при работе с файлами игры или загрузке картинки. */
enum class PortraitsError {
    Cancelled,
    NotGameFolder,
    Permission,
    Blocked,
    Network,
    NotImage,
    /** В папке аккаунта нет `gameVariable_lobby.xml` с заметками персонажей. */
    LobbyNotFound,
    /** Игра запущена: записанные файлы настроек она перезапишет при смене персонажа, телепорте или выходе. */
    GameRunning,
    Unknown,
}
