package com.layzen.bdotools.feature.uieditor.domain.error

/** Что пошло не так при работе с файлами. */
enum class SlotsError {
    Cancelled,
    NotGameFolder,
    Permission,

    /** В `UserCache` нет ни одного `gamevariable.xml` со слотами. */
    NoGameVariable,

    /** Файл изменился после того, как мы его прочитали: скорее всего, игра запущена. */
    FileChanged,
    NotImage,
    Unknown,
}
