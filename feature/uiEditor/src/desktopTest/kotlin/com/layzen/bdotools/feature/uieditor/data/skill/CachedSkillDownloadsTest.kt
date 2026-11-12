package com.layzen.bdotools.feature.uieditor.data.skill

import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Дисковый кэш загрузок: после удачного ответа сеть можно выключить, без папки кэша — просто сеть. */
class CachedSkillDownloadsTest {

    private val cache = Files.createTempDirectory("bdo-skill-cache")

    /** Отвечает [text] и [bytes] один раз, дальше — `null`, как при потере сети. */
    private class OnceDownloads(private var text: String?, private var bytes: ByteArray?) : SkillDownloads {
        override suspend fun text(url: String): String? = text.also { text = null }
        override suspend fun bytes(url: String): ByteArray? = bytes.also { bytes = null }
    }

    @Test
    fun `returns saved copy when network fails`() = runBlocking {
        val downloads = CachedSkillDownloads(OnceDownloads("{\"a\":1}", byteArrayOf(1, 2, 3))) { cache }
        val url = "https://site/skills/1.json?x=y"

        assertEquals("{\"a\":1}", downloads.text(url))
        assertEquals("{\"a\":1}", downloads.text(url))
        assertContentEquals(byteArrayOf(1, 2, 3), downloads.bytes("https://site/icon.png"))
        assertContentEquals(byteArrayOf(1, 2, 3), downloads.bytes("https://site/icon.png"))
    }

    @Test
    fun `without cache folder passes network result through`() = runBlocking {
        val downloads = CachedSkillDownloads(OnceDownloads("x", null)) { null }

        assertEquals("x", downloads.text("https://site/a"))
        assertNull(downloads.text("https://site/a"))
    }
}
