package com.layzen.bdotools.feature.portraitconstructor.data

/** Ошибка работы с файлами; [error] определяет сообщение пользователю, details — для отладки. */
class PortraitsException(val error: PortraitsError, details: String? = null) :
    Exception(details ?: error.name)
