package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * «Цвет заметок и значков» всех портретов сразу: заметки и значки остаются как были, меняется только цвет.
 * У одного портрета цвет выбирают в его окне [DecorationDialog].
 */
@Composable
internal fun DecorationColorsDialog(state: DecorationColorsState, decorator: PortraitDecorator, onEvent: (PortraitsEvent) -> Unit) {
    val close = { onEvent(PortraitsEvent.CloseDecorationColors) }
    ModalWindow(
        title = stringResource(Res.string.portraits_colors_title),
        onDismissRequest = close,
        modifier = Modifier.width(PortraitsDimens.decorationDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = close)
            ActionButton(stringResource(Res.string.portraits_editor_apply), onClick = { onEvent(PortraitsEvent.ApplyDecorationColors) })
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
            DescriptionText(stringResource(Res.string.portraits_colors_description))
            DecorationColorsPicker(
                captionColor = state.captionColor,
                badgeColor = state.badgeColor,
                decorator = decorator,
                onCaptionColor = { onEvent(PortraitsEvent.AllCaptionColorChanged(it)) },
                onBadgeColor = { onEvent(PortraitsEvent.AllBadgeColorChanged(it)) },
            )
        }
    }
}
