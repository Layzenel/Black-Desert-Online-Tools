package com.layzen.bdotools.feature.uieditor.domain.file

/** Почему не получилось прочитать файл раскладки. */
enum class LayoutError {
    /** Текст — не JSON или поля не того типа. */
    NotJson,

    /** JSON, но не раскладка BDO Tools: другое поле `format`. */
    WrongFormat,

    /** Раскладка из более новой версии сайта. */
    UnsupportedVersion,
}
