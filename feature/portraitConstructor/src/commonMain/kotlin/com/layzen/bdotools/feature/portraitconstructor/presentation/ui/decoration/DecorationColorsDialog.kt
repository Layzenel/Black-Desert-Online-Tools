package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitTexture
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitDecorator
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.DecorationColorsState
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_cancel
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_colors_description
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_colors_title
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_editor_apply
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.Skeleton
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * «Цвет заметок, значков и затенения» всех портретов сразу: заметки и значки остаются как были, меняются только
 * цвета и затенение. Слева — превью первого украшенного портрета в выбранном стиле, оно меняется сразу.
 * У одного портрета стиль выбирают в его окне [DecorationDialog].
 */
@Composable
internal fun DecorationColorsDialog(state: DecorationColorsState, decorator: PortraitDecorator, onEvent: (PortraitsEvent) -> Unit) {
    val close = { onEvent(PortraitsEvent.CloseDecorationColors) }
    ModalWindow(
        title = stringResource(Res.string.portraits_colors_title),
        onDismissRequest = close,
        modifier = Modifier.width(PortraitsDimens.colorsDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = close)
            ActionButton(stringResource(Res.string.portraits_editor_apply), onClick = { onEvent(PortraitsEvent.ApplyDecorationColors) })
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
            DescriptionText(stringResource(Res.string.portraits_colors_description))
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL)) {
                StylePreview(state, decorator)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
                    DecorationColorsPicker(state.style, decorator) { onEvent(PortraitsEvent.AllStyleChanged(it)) }
                }
            }
        }
    }
}

/** Превью стиля: первый портрет с заметкой или значком, перерисованный в выбранных цветах и затенении. */
@Composable
private fun StylePreview(state: DecorationColorsState, decorator: PortraitDecorator) {
    val decoration = state.previewDecoration ?: return
    Box(
        Modifier
            .width(PortraitsDimens.colorsPreviewWidth)
            .aspectRatio(PortraitTexture.WIDTH.toFloat() / PortraitTexture.HEIGHT)
            .background(AppTheme.colors.backgroundSurface),
    ) {
        val base = state.previewBase
        if (base == null || !decorator.isReady) {
            Skeleton(Modifier.matchParentSize())
            return@Box
        }
        val preview = remember(base, decoration, state.style, decorator) { decorator.decorate(base, decoration.copy(style = state.style)) }
        Image(preview, contentDescription = null, contentScale = ContentScale.Fit, filterQuality = FilterQuality.High, modifier = Modifier.matchParentSize())
    }
}
