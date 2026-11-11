package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.account.GameAccount
import com.layzen.bdotools.core.game.LifeSkill
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.SlotIcon
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.encodeDecoration
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitWrite
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class PortraitsRepositoryTest {

    private val decorated = PortraitDecoration().withCaption("Рыбак").withBadge(SlotIcon.Skill(LifeSkill.Barter))

    /** Персонаж «1» с чистой основой 1, готовым портретом 2 и украшением [decorated]. */
    private fun decoratedStorage() = MemoryStorage().apply {
        faces["1"] = byteArrayOf(2)
        tools["1.bmp"] = byteArrayOf(1)
        tools["1.txt"] = encodeDecoration(decorated).encodeToByteArray()
    }

    private fun plainWrite(id: String, bytes: ByteArray) = PortraitWrite(id, bytes, bytes, PortraitDecoration())

    @Test
    fun cleanBaseFallsBackToCurrentPortrait() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(1) }
        val repository = PortraitsRepositoryImpl(storage)

        assertContentEquals(byteArrayOf(1), repository.readCleanBase("1"))

        storage.tools["1.bmp"] = byteArrayOf(9)
        assertContentEquals(byteArrayOf(9), repository.readCleanBase("1"))
    }

    @Test
    fun decoratedPortraitKeepsCleanBaseAndDecoration() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(1) }
        val repository = PortraitsRepositoryImpl(storage)

        val backup = repository.writePortraits(listOf(PortraitWrite("1", face = byteArrayOf(2), cleanBase = byteArrayOf(1), decoration = decorated)))

        assertEquals("BDO Tools\\portraitConstructor\\FaceTexture_backup\\2026-11-05_10-00-01", backup)
        assertEquals(listOf("backup:2026-11-05_10-00-01", "backupFile:2026-11-05_10-00-01/1.bmp", "face:1"), storage.log)
        assertContentEquals(byteArrayOf(2), storage.faces["1"])
        assertContentEquals(byteArrayOf(1), storage.tools["1.bmp"])
        assertEquals(mapOf("1" to decorated), repository.loadDecorations())
    }

    @Test
    fun emptyDecorationRemovesStoredFiles() = runTest {
        val storage = decoratedStorage()
        val repository = PortraitsRepositoryImpl(storage)

        repository.writePortraits(listOf(plainWrite("1", byteArrayOf(1))))

        assertNull(storage.tools["1.bmp"])
        assertNull(storage.tools["1.txt"])
        assertContentEquals(byteArrayOf(1), storage.faces["1"])
        assertEquals(emptyMap(), repository.loadDecorations())
    }

    @Test
    fun backupKeepsCleanBaseAndDecoration() = runTest {
        val storage = decoratedStorage()
        val repository = PortraitsRepositoryImpl(storage)

        repository.writePortraits(listOf(plainWrite("1", byteArrayOf(5))))
        val stored = repository.readBackupPortrait(repository.backups().single().name, "1")

        assertContentEquals(byteArrayOf(1), stored.clean)
        assertEquals(decorated, stored.decoration)
    }

    @Test
    fun backupSkipsPortraitsAlreadyBackedUp() = runTest {
        val storage = MemoryStorage().apply {
            faces["1"] = byteArrayOf(1)
            faces["2"] = byteArrayOf(2)
        }
        val repository = PortraitsRepositoryImpl(storage)

        val first = repository.writePortraits(listOf(plainWrite("1", byteArrayOf(7)), plainWrite("2", byteArrayOf(8))))
        // возвращаем прежние портреты: в копию идут только 7 и 8, которых там ещё нет
        repository.writePortraits(listOf(plainWrite("1", byteArrayOf(1)), plainWrite("2", byteArrayOf(2))))
        // и снова 7 и 8: текущие 1 и 2 уже лежат в первой копии, новая не нужна
        val third = repository.writePortraits(listOf(plainWrite("1", byteArrayOf(7)), plainWrite("2", byteArrayOf(8))))

        assertEquals(2, repository.backups().size)
        assertEquals(first, third)
    }

    @Test
    fun backupDistinguishesSameBaseWithOtherDecoration() = runTest {
        val storage = decoratedStorage()
        val repository = PortraitsRepositoryImpl(storage)

        repository.writePortraits(listOf(plainWrite("1", byteArrayOf(1))))
        repository.writePortraits(listOf(plainWrite("1", byteArrayOf(9))))

        assertEquals(2, repository.backups().size)
    }

    @Test
    fun deletesBackupsAndReturnsRest() = runTest {
        val storage = MemoryStorage().apply {
            backups["2026-11-01_10-00-00/1.bmp"] = byteArrayOf(1)
            backups["2026-11-01_10-00-00/2.bmp"] = byteArrayOf(1)
            backups["2026-11-02_10-00-00/1.bmp"] = byteArrayOf(1)
            backups["2026-11-03_10-00-00/2.bmp"] = byteArrayOf(1)
        }
        val progress = mutableListOf<Int>()

        val left = PortraitsRepositoryImpl(storage).deleteBackups(listOf("2026-11-01_10-00-00", "2026-11-03_10-00-00")) { done, _ -> progress += done }

        assertEquals(listOf("2026-11-02_10-00-00"), left.map { it.name })
        assertEquals(listOf(1, 2), progress)
    }

    @Test
    fun portraitOrderKeepsOnlyCharactersOfAccount() = runTest {
        val storage = MemoryStorage().apply { listOf("10", "20", "30", "40").forEach { faces[it] = byteArrayOf(1) } }
        val account = GameAccount("3537922", mapOf("30" to 0, "10" to 1, "50" to 2), lastPlayed = 0)

        assertEquals(listOf("30", "10"), PortraitsRepositoryImpl(storage).portraitOrder(account))
        assertEquals(listOf("10", "20", "30", "40"), PortraitsRepositoryImpl(storage).portraitOrder(null))
    }

    @Test
    fun favoriteSetsCanBeRenamedAndDeleted() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(2) }
        val repository = PortraitsRepositoryImpl(storage)
        val first = repository.saveFavoriteSet(listOf("1"))
        val second = repository.saveFavoriteSet(listOf("1"))

        repository.renameFavoriteSet(first, "  Летний \n набор ")
        assertEquals("Летний набор", repository.favoriteSets().first { it.name == first }.label)

        repository.renameFavoriteSet(first, "   ")
        assertNull(repository.favoriteSets().first { it.name == first }.title)

        assertEquals(listOf(second), repository.deleteFavoriteSets(listOf(first)).map { it.name })
    }

    @Test
    fun replacedFavoriteSetKeepsTitleAndTakesCurrentPortraits() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(2) }
        val repository = PortraitsRepositoryImpl(storage)
        val old = repository.saveFavoriteSet(listOf("1"))
        repository.renameFavoriteSet(old, "Летний набор")
        storage.faces["1"] = byteArrayOf(7)

        val replacement = repository.replaceFavoriteSet(old, listOf("1"))

        val sets = repository.favoriteSets()
        assertEquals(listOf(replacement), sets.map { it.name })
        assertEquals("Летний набор", sets.single().title)
        assertContentEquals(byteArrayOf(7), repository.readFavoritePortrait(replacement, "1").clean)
    }

    @Test
    fun favoriteSetKeepsCleanPortraitsAndDecorations() = runTest {
        val storage = decoratedStorage().apply { faces["2"] = byteArrayOf(3) }
        val repository = PortraitsRepositoryImpl(storage)

        val set = repository.saveFavoriteSet(listOf("1", "2"))
        val restored = repository.readFavoritePortrait(set, "1")
        val plain = repository.readFavoritePortrait(set, "2")

        assertEquals(listOf(setOf("1", "2")), repository.favoriteSets().map { it.characterIds })
        assertContentEquals(byteArrayOf(1), restored.clean)
        assertEquals(decorated, restored.decoration)
        assertContentEquals(byteArrayOf(3), plain.clean)
        assertEquals(PortraitDecoration(), plain.decoration)
        assertNull(storage.favorites["$set/2.txt"])
    }

    @Test
    fun legacyFavoriteSetReadsCleanBaseFromSeparateFile() = runTest {
        val storage = MemoryStorage().apply {
            favorites["old/1.bmp"] = byteArrayOf(2)
            favorites["old/1.base"] = byteArrayOf(1)
            favorites["old/1.txt"] = encodeDecoration(decorated).encodeToByteArray()
        }

        val stored = PortraitsRepositoryImpl(storage).readFavoritePortrait("old", "1")

        assertContentEquals(byteArrayOf(1), stored.clean)
        assertEquals(decorated, stored.decoration)
    }

    @Test
    fun backupsFavoritesAndCurrentPortraitsLiveInAccountFolder() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(1) }
        val repository = PortraitsRepositoryImpl(storage)
        repository.useAccount(account("111", "1"))

        val backup = repository.writePortraits(listOf(PortraitWrite("1", face = byteArrayOf(2), cleanBase = byteArrayOf(1), decoration = decorated)))
        val set = repository.saveFavoriteSet(listOf("1"))

        assertEquals("BDO Tools\\portraitConstructor\\111\\FaceTexture_backup\\2026-11-05_10-00-01", backup)
        assertEquals(listOf("2026-11-05_10-00-01"), repository.backups().map { it.name })
        assertEquals(listOf(set), repository.favoriteSets().map { it.name })
        assertEquals("111\\Favorite FaceTexture\\$set", repository.favoritePath(set))
        assertEquals(mapOf("1" to decorated), repository.loadDecorations())

        // у другого аккаунта своих копий, наборов и украшений нет
        repository.useAccount(account("222"))
        assertEquals(emptyList(), repository.backups())
        assertEquals(emptyList(), repository.favoriteSets())
        assertEquals(emptyMap(), repository.loadDecorations())
    }

    @Test
    fun legacyDataMovesToItsAccount() = runTest {
        val storage = MemoryStorage().apply {
            backups["2026-11-01_10-00-00/1.bmp"] = byteArrayOf(1)
            favorites["2026-11-02_10-00-00/1.bmp"] = byteArrayOf(2)
            tools["1.txt"] = encodeDecoration(decorated).encodeToByteArray()
            tools["2.txt"] = encodeDecoration(decorated).encodeToByteArray()
        }
        val repository = PortraitsRepositoryImpl(storage)

        repository.useAccount(account("111", "1"))
        repository.useAccount(account("222", "2"))

        // копии без аккаунта ушли к первому открытому, украшения — к аккаунту своего персонажа
        assertEquals(emptyList(), repository.backups())
        assertEquals(mapOf("2" to decorated), repository.loadDecorations())
        repository.useAccount(account("111", "1"))
        assertEquals(listOf("2026-11-01_10-00-00"), repository.backups().map { it.name })
        assertEquals(listOf("2026-11-02_10-00-00"), repository.favoriteSets().map { it.name })
        assertEquals(mapOf("1" to decorated), repository.loadDecorations())
    }

    private fun account(id: String, vararg characters: String) =
        GameAccount(id, characters.withIndex().associate { (index, character) -> character to index }, lastPlayed = 0)
}
