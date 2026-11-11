package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitOverlayLayout
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitTexture
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitDecorator
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.DecorationDialogState
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_decoration_loading
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

private const val GAME_ZONE_ALPHA = 0.55f

/**
 * Портрет с украшениями и затемнёнными полосами, где игра рисует свои значки и уровень.
 * [overlay] рисуется поверх готового портрета, например места значков и поле надписи.
 * Текст надписи рисует поле ввода поверх, а здесь под [captionGlow] (надписью или подсказкой) — только затемнение.
 */
@Composable
internal fun DecorationPreview(
    state: DecorationDialogState,
    decorator: PortraitDecorator,
    captionGlow: String,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val zone = AppTheme.colors.backgroundDeep.copy(alpha = GAME_ZONE_ALPHA)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(PortraitTexture.WIDTH.toFloat() / PortraitTexture.HEIGHT)
            .background(AppTheme.colors.backgroundSurface),
        contentAlignment = Alignment.Center,
    ) {
        val base = state.base
        if (base == null || !decorator.isReady) {
            DescriptionText(stringResource(Res.string.portraits_decoration_loading))
            return@Box
        }
        val draft = state.draft.withCaption(captionGlow)
        val preview = remember(base, draft, decorator) { decorator.decorate(base, draft, drawCaptionText = false) }
        Image(
            bitmap = preview,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
            modifier = Modifier.matchParentSize(),
        )
        Canvas(Modifier.matchParentSize()) {
            val bottomTop = size.height * PortraitOverlayLayout.GAME_BOTTOM_STRIP_TOP / PortraitTexture.HEIGHT
            drawRect(zone, Offset.Zero, Size(size.width, size.height * PortraitOverlayLayout.GAME_TOP_STRIP_BOTTOM / PortraitTexture.HEIGHT))
            drawRect(zone, Offset(0f, bottomTop), Size(size.width, size.height - bottomTop))
        }
        overlay()
    }
}
