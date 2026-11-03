package com.layzen.bdotools.feature.portraits.presentation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.scale
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.EditorState
import com.layzen.bdotools.feature.portraits.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraits.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_cancel
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_apply
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_hint
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_reset
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_rotation
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_title_all
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_title_one
import com.layzen.bdotools.feature.portraits.resources.portraits_editor_zoom
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

private val ZOOM_PERCENT_RANGE = 10..400
private val ROTATION_RANGE = -180..180

/**
 * Подгонка изображения под маску. Маска повторяет сетку окна выбора персонажа: ячейки прозрачные,
 * отступы и поля вокруг затемнены. Изображение двигается мышью, масштаб — колесом и слайдером.
 */
@Composable
internal fun MaskEditor(state: EditorState, onEvent: (PortraitsEvent) -> Unit) {
    val title = when (state.target) {
        ReplaceTarget.All -> Res.string.portraits_editor_title_all
        is ReplaceTarget.One -> Res.string.portraits_editor_title_one
    }
    ModalWindow(
        title = stringResource(title),
        onDismissRequest = { onEvent(PortraitsEvent.CloseEditor) },
        modifier = Modifier.widthIn(max = PortraitsDimens.editorMaxWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_editor_reset), onClick = { onEvent(PortraitsEvent.ResetPlacement) })
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = { onEvent(PortraitsEvent.CloseEditor) })
            ActionButton(stringResource(Res.string.portraits_editor_apply), onClick = { onEvent(PortraitsEvent.Apply) })
        },
    ) {
        Column(
            Modifier.padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            DescriptionText(stringResource(Res.string.portraits_editor_hint))
            MaskViewport(state, onEvent)
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXl)) {
                val zoomPercent = (state.placement.scale / state.initialPlacement.scale * 100).roundToInt()
                Column(Modifier.weight(1f)) {
                    SectionHeader(stringResource(Res.string.portraits_editor_zoom))
                    ValueSlider(
                        value = zoomPercent.coerceIn(ZOOM_PERCENT_RANGE),
                        onValueChange = { onEvent(PortraitsEvent.ZoomImage(state.initialPlacement.scale * it / 100f)) },
                        range = ZOOM_PERCENT_RANGE,
                    )
                }
                Column(Modifier.weight(1f)) {
                    SectionHeader(stringResource(Res.string.portraits_editor_rotation))
                    ValueSlider(
                        value = state.placement.rotationDegrees.roundToInt(),
                        onValueChange = { onEvent(PortraitsEvent.RotateImage(it.toFloat())) },
                        range = ROTATION_RANGE,
                    )
                }
            }
        }
    }
}
