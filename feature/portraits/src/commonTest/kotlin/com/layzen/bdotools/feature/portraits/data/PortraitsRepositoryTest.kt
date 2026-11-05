package com.layzen.bdotools.feature.portraits.data

import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.core.game.LifeSkill
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.domain.SlotIcon
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class PortraitsRepositoryTest {

    private val decorated = PortraitDecoration().withCaption("Рыбак").withBadge(SlotIcon.Skill(LifeSkill.Barter))

    @Test
    fun cleanBaseFallsBackToCurrentPortrait() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(1) }
        val repository = PortraitsRepository(storage)

        assertContentEquals(byteArrayOf(1), repository.readCleanBase("1"))

        storage.tools["1.bmp"] = byteArrayOf(9)
        assertContentEquals(byteArrayOf(9), repository.readCleanBase("1"))
    }

    @Test
    fun decoratedPortraitKeepsCleanBaseAndDecoration() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(1) }
        val repository = PortraitsRepository(storage)

        val backup = repository.writePortraits(listOf(PortraitWrite("1", face = byteArrayOf(2), cleanBase = byteArrayOf(1), decoration = decorated)))

        assertEquals("FaceTexture_backup\\test", backup)
        assertEquals(listOf("backup:1", "face:1"), storage.log)
        assertContentEquals(byteArrayOf(2), storage.faces["1"])
        assertContentEquals(byteArrayOf(1), storage.tools["1.bmp"])
        assertEquals(mapOf("1" to decorated), repository.loadDecorations())
    }

    @Test
    fun emptyDecorationRemovesStoredFiles() = runTest {
        val storage = MemoryStorage().apply {
            faces["1"] = byteArrayOf(2)
            tools["1.bmp"] = byteArrayOf(1)
            tools["1.txt"] = "caption=Рыбак\n".encodeToByteArray()
        }
        val repository = PortraitsRepository(storage)

        repository.writePortraits(listOf(PortraitWrite("1", face = byteArrayOf(1), cleanBase = byteArrayOf(1), decoration = PortraitDecoration())))

        assertNull(storage.tools["1.bmp"])
        assertNull(storage.tools["1.txt"])
        assertContentEquals(byteArrayOf(1), storage.faces["1"])
        assertEquals(emptyMap(), repository.loadDecorations())
    }

    @Test
    fun deletesBackupsAndReturnsRest() = runTest {
        val storage = MemoryStorage().apply {
            backups["2026-11-01_10-00-00"] = listOf("1", "2")
            backups["2026-11-02_10-00-00"] = listOf("1")
            backups["2026-11-03_10-00-00"] = listOf("2")
        }
        val progress = mutableListOf<Int>()

        val left = PortraitsRepository(storage).deleteBackups(listOf("2026-11-01_10-00-00", "2026-11-03_10-00-00")) { done, _ -> progress += done }

        assertEquals(listOf("2026-11-02_10-00-00"), left.map { it.name })
        assertEquals(listOf(1, 2), progress)
    }

    @Test
    fun swapExchangesPortraitsWithCleanBaseAndDecoration() = runTest {
        val storage = MemoryStorage().apply {
            faces["1"] = byteArrayOf(2)
            faces["2"] = byteArrayOf(5)
            tools["1.bmp"] = byteArrayOf(1)
            tools["1.txt"] = "caption=Рыбак\n".encodeToByteArray()
        }
        val repository = PortraitsRepository(storage)

        val writes = repository.swapWrites("1", "2", mapOf("1" to decorated))
        repository.writePortraits(writes)

        assertEquals(listOf("backup:1, 2", "face:1", "face:2"), storage.log)
        assertContentEquals(byteArrayOf(5), storage.faces["1"])
        assertContentEquals(byteArrayOf(2), storage.faces["2"])
        assertContentEquals(byteArrayOf(1), storage.tools["2.bmp"])
        assertNull(storage.tools["1.bmp"])
        assertEquals(mapOf("2" to decorated), repository.loadDecorations())
    }

    @Test
    fun portraitOrderKeepsOnlyCharactersOfAccount() = runTest {
        val storage = MemoryStorage().apply { listOf("10", "20", "30", "40").forEach { faces[it] = byteArrayOf(1) } }
        val account = GameAccount("3537922", mapOf("30" to 0, "10" to 1, "50" to 2), lastPlayed = 0)

        assertEquals(listOf("30", "10"), PortraitsRepository(storage).portraitOrder(account))
        assertEquals(listOf("10", "20", "30", "40"), PortraitsRepository(storage).portraitOrder(null))
    }

    @Test
    fun favoriteSetsCanBeRenamedAndDeleted() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(2) }
        val repository = PortraitsRepository(storage)
        val first = repository.saveFavoriteSet(listOf("1"), emptyMap())
        val second = repository.saveFavoriteSet(listOf("1"), emptyMap())

        repository.renameFavoriteSet(first, "  Летний \n набор ")
        assertEquals("Летний набор", repository.favoriteSets().first { it.name == first }.label)

        repository.renameFavoriteSet(first, "   ")
        assertNull(repository.favoriteSets().first { it.name == first }.title)

        assertEquals(listOf(second), repository.deleteFavoriteSets(listOf(first)).map { it.name })
    }

    @Test
    fun replacedFavoriteSetKeepsTitleAndTakesCurrentPortraits() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(2) }
        val repository = PortraitsRepository(storage)
        val old = repository.saveFavoriteSet(listOf("1"), emptyMap())
        repository.renameFavoriteSet(old, "Летний набор")
        storage.faces["1"] = byteArrayOf(7)

        val replacement = repository.replaceFavoriteSet(old, listOf("1"), emptyMap())

        val sets = repository.favoriteSets()
        assertEquals(listOf(replacement), sets.map { it.name })
        assertEquals("Летний набор", sets.single().title)
        assertContentEquals(byteArrayOf(7), repository.readFavoritePortrait(replacement, "1"))
    }

    @Test
    fun favoriteSetKeepsAllPortraitsWithDecorations() = runTest {
        val storage = MemoryStorage().apply {
            faces["1"] = byteArrayOf(2)
            faces["2"] = byteArrayOf(3)
            tools["1.bmp"] = byteArrayOf(1)
        }
        val repository = PortraitsRepository(storage)

        val set = repository.saveFavoriteSet(listOf("1", "2"), mapOf("1" to decorated))
        storage.faces["1"] = byteArrayOf(9)
        val restored = repository.favoriteWrite(set, "1")
        val plain = repository.favoriteWrite(set, "2")

        assertEquals(listOf(setOf("1", "2")), repository.favoriteSets().map { it.characterIds })
        assertContentEquals(byteArrayOf(2), restored.face)
        assertContentEquals(byteArrayOf(1), restored.cleanBase)
        assertEquals(decorated, restored.decoration)
        assertContentEquals(byteArrayOf(3), plain.cleanBase)
        assertEquals(PortraitDecoration(), plain.decoration)
    }
}
