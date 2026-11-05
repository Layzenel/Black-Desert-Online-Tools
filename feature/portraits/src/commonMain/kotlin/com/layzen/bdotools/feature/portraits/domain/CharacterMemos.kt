package com.layzen.bdotools.feature.portraits.domain

// Заметки персонажей из окна «Просмотр персонажей аккаунта» (карта → кнопка персонажей) в `gameVariable_lobby.xml`:
// <CharacterMemo Version="1">
// 	<CharacterMemo CharacterNo="40000000006383529" Memo="Коневод"/>
// </CharacterMemo>

private const val BLOCK_TAG = "<CharacterMemo Version="
private const val BLOCK_CLOSE = "</CharacterMemo>"
private const val LINE_BREAK = "\r\n"
private val ENTRY = Regex("""<CharacterMemo CharacterNo="(\d+)" Memo="([^"]*)"\s*/>""")
private val VERSION = Regex("""Version="([^"]*)"""")

/** Заметки из файла: ID персонажа → заметка, в порядке файла. Нет блока — пусто. */
fun parseCharacterMemos(file: ByteArray): Map<String, String> {
    val block = file.memoBlock() ?: return emptyMap()
    return ENTRY.findAll(file.decodeToString(block.first, block.last + 1))
        .associate { it.groupValues[1] to unescapeXml(it.groupValues[2]) }
}

/**
 * Файл, в котором блок заметок заменён на [memos]. Остальные байты не трогаются: файлы игры бывают не в UTF-8,
 * и перекодирование всего файла их испортит. Если блока не было, он дописывается в конец.
 */
fun withCharacterMemos(file: ByteArray, memos: Map<String, String>): ByteArray {
    val block = file.memoBlock()
    val version = block?.let { VERSION.find(file.decodeToString(it.first, it.last + 1))?.groupValues?.get(1) } ?: "1"
    val text = memoBlockText(version, memos).encodeToByteArray()
    if (block == null) return file + text + LINE_BREAK.encodeToByteArray()
    return file.copyOfRange(0, block.first) + text + file.copyOfRange(block.last + 1, file.size)
}

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

private fun memoBlockText(version: String, memos: Map<String, String>): String {
    if (memos.isEmpty()) return "$BLOCK_TAG\"$version\"/>"
    return buildString {
        append("$BLOCK_TAG\"$version\">$LINE_BREAK")
        memos.forEach { (id, memo) -> append("\t<CharacterMemo CharacterNo=\"$id\" Memo=\"${escapeXml(memo)}\"/>$LINE_BREAK") }
        append(BLOCK_CLOSE)
    }
}

/** Байты блока заметок от `<CharacterMemo Version=` до его закрывающего тега включительно. */
private fun ByteArray.memoBlock(): IntRange? {
    val start = indexOf(BLOCK_TAG.encodeToByteArray(), 0)
    if (start < 0) return null
    val tagEnd = indexOf(">".encodeToByteArray(), start)
    if (tagEnd < 0) return null
    if (this[tagEnd - 1] == '/'.code.toByte()) return start..tagEnd
    val close = BLOCK_CLOSE.encodeToByteArray()
    val end = indexOf(close, tagEnd)
    return if (end < 0) null else start until end + close.size
}

private fun ByteArray.indexOf(pattern: ByteArray, from: Int): Int {
    for (i in from..size - pattern.size) {
        if (pattern.indices.all { this[i + it] == pattern[it] }) return i
    }
    return -1
}

private fun escapeXml(text: String) =
    text.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")

private fun unescapeXml(text: String) =
    text.replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">").replace("&apos;", "'").replace("&amp;", "&")
