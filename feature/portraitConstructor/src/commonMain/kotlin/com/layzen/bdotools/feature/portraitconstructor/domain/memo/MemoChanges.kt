package com.layzen.bdotools.feature.portraitconstructor.domain.memo

/**
 * Что изменится в заметках, если записать в них надписи [captions] (ID → надпись, в порядке страницы).
 * Персонажи без надписи и с уже совпадающей заметкой не попадают в список.
 */
fun memoChanges(captions: List<Pair<String, String>>, memos: Map<String, String>): List<MemoChange> =
    captions.mapNotNull { (id, caption) ->
        val new = caption.trim()
        val old = memos[id].orEmpty()
        if (new.isEmpty() || new == old) null else MemoChange(id, old, new)
    }
