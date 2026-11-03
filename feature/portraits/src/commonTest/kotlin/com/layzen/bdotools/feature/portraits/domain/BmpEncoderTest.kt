package com.layzen.bdotools.feature.portraits.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class BmpEncoderTest {

    private fun ByteArray.int(at: Int) =
        (this[at].toInt() and 0xFF) or
            ((this[at + 1].toInt() and 0xFF) shl 8) or
            ((this[at + 2].toInt() and 0xFF) shl 16) or
            ((this[at + 3].toInt() and 0xFF) shl 24)

    private fun ByteArray.short(at: Int) = (this[at].toInt() and 0xFF) or ((this[at + 1].toInt() and 0xFF) shl 8)

    @Test
    fun headerMatchesGameFiles() {
        val bmp = encodeBmp(PortraitTexture.WIDTH, PortraitTexture.HEIGHT, IntArray(PortraitTexture.WIDTH * PortraitTexture.HEIGHT))

        assertEquals('B'.code, bmp[0].toInt())
        assertEquals('M'.code, bmp[1].toInt())
        assertEquals(2_006_838, bmp.size) // размер файлов в FaceTexture
        assertEquals(bmp.size, bmp.int(2))
        assertEquals(54, bmp.int(10))
        assertEquals(40, bmp.int(14))
        assertEquals(624, bmp.int(18))
        assertEquals(804, bmp.int(22))
        assertEquals(1, bmp.short(26))
        assertEquals(32, bmp.short(28))
        assertEquals(0, bmp.int(30))
        assertEquals(2835, bmp.int(38))
    }

    @Test
    fun writesRowsBottomUpInBgraOrder() {
        val top = 0xFF112233.toInt()
        val bottom = 0x80445566.toInt()
        val bmp = encodeBmp(width = 1, height = 2, argb = intArrayOf(top, bottom))

        // первой в файле идёт нижняя строка
        assertEquals(listOf(0x66, 0x55, 0x44, 0x80), (54 until 58).map { bmp[it].toInt() and 0xFF })
        assertEquals(listOf(0x33, 0x22, 0x11, 0xFF), (58 until 62).map { bmp[it].toInt() and 0xFF })
    }
}
