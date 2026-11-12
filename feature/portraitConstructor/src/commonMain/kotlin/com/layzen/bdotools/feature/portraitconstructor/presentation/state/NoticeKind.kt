package com.layzen.bdotools.feature.portraitconstructor.presentation.state

/** Вид сообщения пользователю: успешные действия и ошибки. */
enum class NoticeKind {
    Saved,
    DecorationSaved,
    Restored,
    BackupsDeleted,
    FavoriteAdded,
    FavoriteApplied,
    Swapped,
    FavoritesDeleted,
    FavoriteReplaced,
    ReorderHint,
    NotesSynced,
    LobbyNotFound,
    GameRunning,
    NotGameFolder,
    Permission,
    Blocked,
    Network,
    NotImage,
    NoClipboardImage,
    Unknown,
}
