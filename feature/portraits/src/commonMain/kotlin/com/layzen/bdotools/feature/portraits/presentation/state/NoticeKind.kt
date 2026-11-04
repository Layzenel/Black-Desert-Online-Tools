package com.layzen.bdotools.feature.portraits.presentation.state

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
    NotGameFolder,
    Permission,
    Blocked,
    Network,
    NotImage,
    Unknown,
}
