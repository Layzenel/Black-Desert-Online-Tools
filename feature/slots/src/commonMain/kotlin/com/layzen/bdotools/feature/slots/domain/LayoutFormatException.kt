package com.layzen.bdotools.feature.slots.domain

/** Файл раскладки не прочитан; [error] определяет сообщение пользователю. */
class LayoutFormatException(val error: LayoutError, details: String? = null) : Exception(details ?: error.name)
