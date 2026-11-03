package com.layzen.bdotools.feature.portraits.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortraitDecorationTest {

    @Test
    fun captionIsSingleLineAndLimited() {
        assertEquals("Рыбак и коневод", PortraitDecoration.normalizeCaption("  Рыбак\n  и \t коневод"))
        assertEquals(24, PortraitDecoration.normalizeCaption("а".repeat(40)).length)
    }

    @Test
    fun emptyWhenNoCaptionAndNoBadges() {
        assertTrue(PortraitDecoration().isEmpty)
        assertTrue(PortraitDecoration().withCaption("   ").isEmpty)
        assertFalse(PortraitDecoration().withBadge(BadgeSlot.Top, BadgeIcon.Fisher).isEmpty)
    }

    @Test
    fun removingBadgeClearsSlot() {
        val decoration = PortraitDecoration()
            .withBadge(BadgeSlot.Left, BadgeIcon.Gatherer)
            .withBadge(BadgeSlot.Left, null)

        assertTrue(decoration.badges.isEmpty())
    }

    @Test
    fun fileRoundTrip() {
        val decoration = PortraitDecoration()
            .withCaption("Твинк артефакта")
            .withBadge(BadgeSlot.Top, BadgeIcon.Fisher)
            .withBadge(BadgeSlot.Right, BadgeIcon.HorseBreeder)

        val text = encodeDecoration(decoration)

        assertEquals("caption=Твинк артефакта\ntop=fisher\nright=horse-breeder\n", text)
        assertEquals(decoration, decodeDecoration(text))
    }

    @Test
    fun decodingSkipsUnknownKeysAndIcons() {
        val decoration = decodeDecoration("caption=Сбор=лут\r\nleft=dragon\nfoo=bar\ntop=gatherer\n")

        assertEquals("Сбор=лут", decoration.caption)
        assertEquals(mapOf(BadgeSlot.Top to BadgeIcon.Gatherer), decoration.badges)
    }

    @Test
    fun emptyDecorationEncodesToEmptyText() {
        assertEquals("", encodeDecoration(PortraitDecoration()))
        assertTrue(decodeDecoration("").isEmpty)
    }

    @Test
    fun legacyPairedSideKeysKeepUpperBadge() {
        // версия с двумя значками сбоку: верхний остаётся, нижний используется, только если верхнего нет
        val decoration = decodeDecoration("left-top=sailor\nleft-bottom=fisher\nright-bottom=barter\n")

        assertEquals(
            mapOf(
                BadgeSlot.Left to BadgeIcon.Sailor,
                BadgeSlot.Right to BadgeIcon.Barter,
            ),
            decoration.badges,
        )
        assertEquals("left=sailor\nright=barter\n", encodeDecoration(decoration))
    }

    @Test
    fun badgesStayInsideFreeZonesAndAboveCaption() {
        val r = PortraitOverlayLayout.BADGE_DIAMETER / 2
        BadgeSlot.entries.forEach { slot ->
            val x = PortraitOverlayLayout.badgeCenterX(slot)
            val y = PortraitOverlayLayout.badgeCenterY(slot)
            assertTrue(x - r >= 0 && x + r <= PortraitTexture.WIDTH, "$slot выходит за край по ширине")
            assertTrue(y - r >= 0 && y + r <= PortraitOverlayLayout.CAPTION_MIN_TOP, "$slot заходит на надпись")
        }
    }

    @Test
    fun badgesDoNotOverlap() {
        val r = PortraitOverlayLayout.BADGE_DIAMETER / 2
        val slots = BadgeSlot.entries
        slots.forEachIndexed { i, a ->
            slots.drop(i + 1).forEach { b ->
                val dx = PortraitOverlayLayout.badgeCenterX(a) - PortraitOverlayLayout.badgeCenterX(b)
                val dy = PortraitOverlayLayout.badgeCenterY(a) - PortraitOverlayLayout.badgeCenterY(b)
                assertTrue(dx * dx + dy * dy >= 4 * r * r, "$a и $b перекрываются")
            }
        }
    }
}
