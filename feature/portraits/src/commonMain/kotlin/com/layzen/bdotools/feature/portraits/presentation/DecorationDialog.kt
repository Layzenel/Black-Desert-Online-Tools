package com.layzen.bdotools.feature.portraits.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.domain.BadgeSlot
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.domain.PortraitOverlayLayout
import com.layzen.bdotools.feature.portraits.domain.PortraitTexture
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_none
import com.layzen.bdotools.feature.portraits.resources.portraits_badges_clear
import com.layzen.bdotools.feature.portraits.resources.portraits_badges_description
import com.layzen.bdotools.feature.portraits.resources.portraits_badges_title
import com.layzen.bdotools.feature.portraits.resources.portraits_cancel
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_clear
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_counter
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_description
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_field
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_placeholder
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_title
import com.layzen.bdotools.feature.portraits.resources.portraits_decoration_loading
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_apply
import com.layzen.bdotools.feature.portraits.resources.portraits_preview_hint
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

private const val GAME_ZONE_ALPHA = 0.55f
private const val GAME_TOP_STRIP_RATIO = 0.15f

/** Надпись или значки для одного портрета: слева живой предпросмотр, справа настройки. */
@Composable
internal fun DecorationDialog(
    state: DecorationDialogState,
    decorator: PortraitDecorator,
    onEvent: (PortraitsEvent) -> Unit,
) {
    val title = when (state.mode) {
        DecorationMode.Caption -> Res.string.portraits_caption_title
        DecorationMode.Badges -> Res.string.portraits_badges_title
    }
    val ready = state.base != null && decorator.isReady
    ModalWindow(
        title = stringResource(title),
        onDismissRequest = { onEvent(PortraitsEvent.CloseDecoration) },
        modifier = Modifier.width(PortraitsDimens.decorationDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = { onEvent(PortraitsEvent.CloseDecoration) })
            ActionButton(
                stringResource(Res.string.portraits_editor_apply),
                onClick = { onEvent(PortraitsEvent.ApplyDecoration) },
                enabled = ready,
            )
        },
    ) {
        Row(
            Modifier.padding(AppTheme.dimens.spaceL),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL),
        ) {
            Column(Modifier.width(PortraitsDimens.decorationPreviewWidth), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
                DecorationPreview(state, decorator)
                DescriptionText(stringResource(Res.string.portraits_preview_hint))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
                when (state.mode) {
                    DecorationMode.Caption -> CaptionControls(state.draft, onEvent)
                    DecorationMode.Badges -> BadgeControls(state.draft, onEvent)
                }
            }
        }
    }
}

/** Портрет с украшениями и затемнёнными полосами, где игра рисует свои значки и уровень. */
@Composable
private fun DecorationPreview(state: DecorationDialogState, decorator: PortraitDecorator) {
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
        val preview = remember(base, state.draft, decorator) { decorator.decorate(base, state.draft) }
        Image(
            bitmap = preview,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
            modifier = Modifier.matchParentSize(),
        )
        Canvas(Modifier.matchParentSize()) {
            val bottomTop = size.height * PortraitOverlayLayout.GAME_BOTTOM_STRIP_TOP / PortraitTexture.HEIGHT
            drawRect(zone, Offset.Zero, Size(size.width, size.height * GAME_TOP_STRIP_RATIO))
            drawRect(zone, Offset(0f, bottomTop), Size(size.width, size.height - bottomTop))
        }
    }
}

@Composable
private fun CaptionControls(draft: PortraitDecoration, onEvent: (PortraitsEvent) -> Unit) {
    SectionHeader(
        stringResource(Res.string.portraits_caption_field),
        description = stringResource(Res.string.portraits_caption_description),
    )
    InputField(
        value = draft.caption,
        onValueChange = { onEvent(PortraitsEvent.CaptionChanged(it)) },
        placeholder = stringResource(Res.string.portraits_caption_placeholder),
        modifier = Modifier.fillMaxWidth(),
    )
    DescriptionText(stringResource(Res.string.portraits_caption_counter, draft.caption.length, PortraitDecoration.CAPTION_MAX_LENGTH))
    SectionDivider()
    ActionButton(
        stringResource(Res.string.portraits_caption_clear),
        onClick = { onEvent(PortraitsEvent.CaptionChanged("")) },
        enabled = draft.caption.isNotEmpty(),
    )
}

@Composable
private fun BadgeControls(draft: PortraitDecoration, onEvent: (PortraitsEvent) -> Unit) {
    DescriptionText(stringResource(Res.string.portraits_badges_description))
    val options: List<BadgeIcon?> = listOf(null) + BadgeIcon.entries
    val none = stringResource(Res.string.portraits_badge_none)
    BadgeSlot.entries.forEach { slot ->
        SectionHeader(stringResource(slot.label))
        DropdownSelect(
            selected = draft.badges[slot],
            options = options,
            onSelect = { onEvent(PortraitsEvent.BadgeChanged(slot, it)) },
            label = { icon -> icon?.let { stringResource(it.label) } ?: none },
            modifier = Modifier.fillMaxWidth(),
        )
    }
    SectionDivider()
    ActionButton(
        stringResource(Res.string.portraits_badges_clear),
        onClick = { BadgeSlot.entries.forEach { onEvent(PortraitsEvent.BadgeChanged(it, null)) } },
        enabled = draft.badges.isNotEmpty(),
    )
}
