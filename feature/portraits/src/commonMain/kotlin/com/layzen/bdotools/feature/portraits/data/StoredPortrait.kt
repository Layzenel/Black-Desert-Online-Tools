package com.layzen.bdotools.feature.portraits.data

import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.domain.decodeDecoration
import com.layzen.bdotools.feature.portraits.domain.encodeDecoration

private const val BMP_EXTENSION = ".bmp"
private const val DECORATION_EXTENSION = ".txt"

/**
 * Портрет так, как он хранится в `BDO Tools\portraits`, избранном и резервных копиях: чистая основа `<ID>.bmp`
 * и рядом украшение `<ID>.txt`, если оно есть. Готовый портрет с надписью и значком рисуется из них заново.
 */
class StoredPortrait(val clean: ByteArray, val decoration: PortraitDecoration) {

    /** Быстрое сравнение: разные отпечатки — точно разные портреты, одинаковые проверяются [sameAs]. */
    val fingerprint: Int get() = clean.contentHashCode() * 31 + decoration.hashCode()

    fun sameAs(other: StoredPortrait): Boolean = decoration == other.decoration && clean.contentEquals(other.clean)

    /** Пишет основу и украшение персонажа [characterId] через [write] (имя файла, байты). */
    suspend fun writeTo(characterId: String, write: suspend (name: String, bytes: ByteArray) -> Unit) {
        write(characterId + BMP_EXTENSION, clean)
        if (!decoration.isEmpty) write(characterId + DECORATION_EXTENSION, encodeDecoration(decoration).encodeToByteArray())
    }

    companion object {
        /** Читает портрет персонажа [characterId] через [read] (имя файла → байты или `null`). Без основы — ошибка. */
        suspend fun read(characterId: String, read: suspend (name: String) -> ByteArray?): StoredPortrait {
            val clean = read(characterId + BMP_EXTENSION) ?: throw PortraitsException(PortraitsError.Unknown)
            val decoration = read(characterId + DECORATION_EXTENSION)?.let { decodeDecoration(it.decodeToString()) }
            return StoredPortrait(clean, decoration ?: PortraitDecoration())
        }
    }
}
