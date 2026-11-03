package com.layzen.bdotools.feature.portraits.presentation.ui.decoration

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.domain.BadgeSlot
import com.layzen.bdotools.feature.portraits.domain.PortraitOverlayLayout
import com.layzen.bdotools.feature.portraits.domain.PortraitTexture
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent

/** Места значков поверх предпросмотра — там же и того же размера, что значки на портрете. */
@Composable
internal fun BoxScope.BadgeSlotsOverlay(badges: Map<BadgeSlot, BadgeIcon>, onEvent: (PortraitsEvent) -> Unit) {
    BoxWithConstraints(Modifier.matchParentSize()) {
        val texturePixel = maxWidth / PortraitTexture.WIDTH
        val diameter = texturePixel * PortraitOverlayLayout.BADGE_DIAMETER
        BadgeSlot.entries.forEach { slot ->
            BadgeSlotButton(
                icon = badges[slot],
                onSelect = { onEvent(PortraitsEvent.BadgeChanged(slot, it)) },
                modifier = Modifier
                    .offset(
                        x = texturePixel * PortraitOverlayLayout.badgeCenterX(slot) - diameter / 2,
                        y = texturePixel * PortraitOverlayLayout.badgeCenterY(slot) - diameter / 2,
                    )
                    .size(diameter),
            )
        }
    }
}
