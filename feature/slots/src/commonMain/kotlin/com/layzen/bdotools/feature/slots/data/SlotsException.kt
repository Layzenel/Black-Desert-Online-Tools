package com.layzen.bdotools.feature.slots.data

/** Ошибка работы с файлами; [error] определяет сообщение пользователю, details — для отладки. */
class SlotsException(val error: SlotsError, details: String? = null) : Exception(details ?: error.name)
