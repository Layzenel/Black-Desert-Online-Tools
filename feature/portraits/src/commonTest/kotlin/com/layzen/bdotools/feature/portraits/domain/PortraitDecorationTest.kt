package com.layzen.bdotools.feature.portraits.domain

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.game.LifeSkill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortraitDecorationTest {

    private val barter = SlotIcon.Skill(LifeSkill.Barter)
    private val darkKnight = SlotIcon.Class(CharacterClass.DarkKnight)

    @Test
    fun captionIsSingleLineAndLimited() {
        assertEquals("Рыбак и коневод", PortraitDecoration.normalizeCaption("  Рыбак\n  и \t коневод"))
        assertEquals(24, PortraitDecoration.normalizeCaption("а".repeat(40)).length)
    }

    @Test
    fun emptyWhenNoCaptionAndNoBadge() {
        assertTrue(PortraitDecoration().isEmpty)
        assertTrue(PortraitDecoration().withCaption("   ").isEmpty)
        assertFalse(PortraitDecoration().withBadge(barter).isEmpty)
        assertTrue(PortraitDecoration().withBadge(barter).withBadge(null).isEmpty)
    }

    @Test
    fun fileRoundTrip() {
        val decoration = PortraitDecoration().withCaption("Твинк артефакта").withBadge(barter)

        val text = encodeDecoration(decoration)

        assertEquals("caption=Твинк артефакта\nbadge=barter\n", text)
        assertEquals(decoration, decodeDecoration(text))
    }

    @Test
    fun classBadgeRoundTripsWithPrefixedKey() {
        val decoration = PortraitDecoration().withBadge(darkKnight)
        val text = encodeDecoration(decoration)

        assertEquals("badge=class-dark-knight\n", text)
        assertEquals(decoration, decodeDecoration(text))
    }

    @Test
    fun decodingSkipsUnknownKeysAndIcons() {
        val decoration = decodeDecoration("caption=Сбор=лут\r\nbadge=dragon\nfoo=fisher\n")

        assertEquals("Сбор=лут", decoration.caption)
        assertNull(decoration.badge)
    }

    @Test
    fun emptyDecorationEncodesToEmptyText() {
        assertEquals("", encodeDecoration(PortraitDecoration()))
        assertTrue(decodeDecoration("").isEmpty)
    }

    @Test
    fun legacyFilesKeepClassFirst() {
        // версии с несколькими местами: класс важнее, где бы он ни стоял
        assertEquals(darkKnight, decodeDecoration("left=sailor\ntop=class-dark-knight\nright=barter\n").badge)
    }

    @Test
    fun legacyFilesWithoutClassKeepFirstBadge() {
        assertEquals(SlotIcon.Skill(LifeSkill.Sailing), decodeDecoration("left-top=sailor\nleft-bottom=fisher\nright=barter\n").badge)
    }

    @Test
    fun legacyBadgeKeysMapToLifeSkills() {
        assertEquals(SlotIcon.Skill(LifeSkill.Training), decodeDecoration("top=horse-breeder\n").badge)
        assertEquals(SlotIcon.Skill(LifeSkill.Fishing), decodeDecoration("left=fisher\n").badge)
        assertEquals(SlotIcon.Skill(LifeSkill.Gathering), decodeDecoration("right=gatherer\n").badge)
    }

    @Test
    fun badgeIsCenteredBetweenGameStripAndCaption() {
        val half = PortraitOverlayLayout.BADGE_SIZE / 2
        assertEquals(PortraitTexture.WIDTH / 2, PortraitOverlayLayout.BADGE_CENTER_X)
        assertTrue(PortraitOverlayLayout.BADGE_CENTER_X - half >= 0 && PortraitOverlayLayout.BADGE_CENTER_X + half <= PortraitTexture.WIDTH)
        assertTrue(PortraitOverlayLayout.BADGE_CENTER_Y - half >= PortraitOverlayLayout.GAME_TOP_STRIP_BOTTOM, "значок задевает верхнюю полосу игры")
    }
}
