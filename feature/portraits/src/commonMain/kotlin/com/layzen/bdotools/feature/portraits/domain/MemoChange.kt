package com.layzen.bdotools.feature.portraits.domain

/** Заметка персонажа, которая изменится: [old] — сейчас в игре (пустая, если заметки нет), [new] — надпись с портрета. */
data class MemoChange(val characterId: String, val old: String, val new: String)
