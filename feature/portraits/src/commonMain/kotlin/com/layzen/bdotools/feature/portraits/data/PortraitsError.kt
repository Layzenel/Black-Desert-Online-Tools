package com.layzen.bdotools.feature.portraits.data

/** Что пошло не так при работе с файлами игры или загрузке картинки. */
enum class PortraitsError {
    Cancelled,
    NotGameFolder,
    Permission,
    Blocked,
    Network,
    NotImage,
    Unknown,
}
