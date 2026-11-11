package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitOverlayLayout as Layout
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.SlotIcon
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitTexture
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent

/** Значок поверх предпросмотра — там же и того же размера, что значок на портрете: по центру, 60 % ширины. */
@Composable
internal fun BoxScope.BadgeOverlay(badge: SlotIcon?, onEvent: (PortraitsEvent) -> Unit) {
    BoxWithConstraints(Modifier.matchParentSize()) {
        val texturePixel = maxWidth / PortraitTexture.WIDTH
        val size = texturePixel * Layout.BADGE_SIZE
        BadgeButton(
            icon = badge,
            onSelect = { onEvent(PortraitsEvent.BadgeChanged(it)) },
            modifier = Modifier
                .offset(
                    x = texturePixel * Layout.BADGE_CENTER_X - size / 2,
                    y = texturePixel * Layout.BADGE_CENTER_Y - size / 2,
                )
                .size(size),
        )
    }
}
