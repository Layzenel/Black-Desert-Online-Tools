package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.slotTitle
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Рамка элемента на холсте в настоящем размере, как в режиме правки интерфейса игры: глазик сверху, номер в центре.
 * Клик выбирает элемент, Ctrl+клик добавляет к выбору, перетаскивание двигает все выбранные.
 * Скрытый в игре элемент нарисован приглушённым. [anchor] — опора выравнивания, пока открыто меню: золотая рамка.
 */
@Composable
internal fun SlotFrame(
    id: SlotId,
    position: SlotPosition,
    screen: ScreenSpace,
    scale: Float,
    selected: Boolean,
    anchor: Boolean,
    visible: Boolean,
    onEvent: (SlotsEvent) -> Unit,
    onPress: () -> Unit,
) {
    val border = when {
        anchor -> AppTheme.colors.accentGold
        selected -> AppTheme.colors.textPrimary
        else -> id.group.frameColor()
    }
    // За краем экрана — красная заливка; рамка остаётся цветом группы, чтобы было видно, что это за элемент
    val fill = if (screen.fits(id, position)) AppTheme.colors.backgroundDeep else AppTheme.colors.danger
    val density = LocalDensity.current
    val iconWidth = with(density) { (id.width * scale).toDp() }
    // У больших окон глазик в игре такой же, как у слота, а не во всю ширину
    val eyeSize = with(density) { (minOf(id.width, id.height, SlotGroup.Cooldown.width) * scale).toDp() } * SlotsDimens.EYE_SHARE
    Box(
        Modifier
            .offset {
                IntOffset(
                    (screen.iconLeft(id, position) * scale).roundToInt(),
                    (screen.iconTop(id, position) * scale).roundToInt(),
                )
            }
            .size(iconWidth, with(density) { (id.height * scale).toDp() }),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .alpha(if (visible) 1f else SlotsDimens.HIDDEN_ALPHA)
                .background(fill.copy(alpha = SlotsDimens.SLOT_FILL_ALPHA))
                .border(if (selected) SlotsDimens.selectedSlotBorder else SlotsDimens.slotBorder, border)
                .pointerHoverIcon(PointerIcon.Hand)
                .elementGestures(
                    key = id,
                    scale = scale,
                    onEvent = onEvent,
                    onPress = onPress,
                    select = { SlotsEvent.Select(id, it) },
                    dragStart = { SlotsEvent.DragStart(id, it) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            val label = when (id.group) {
                SlotGroup.ClassResource -> null
                SlotGroup.Interface -> slotTitle(id)
                else -> id.number.toString()
            }
            if (label != null && iconWidth >= SlotsDimens.labelMinIconWidth) {
                Text(
                    label,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                )
            }
        }
        // Глаз внутри рамки сверху по центру, как в режиме правки интерфейса игры; видимость выносливости меняет сама игра
        if (id.hasVisibilityToggle) {
            EyeToggle(
                visible = visible,
                eyeSize = eyeSize,
                onToggle = { onEvent(SlotsEvent.ToggleVisibility(id)) },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = eyeSize * SlotsDimens.EYE_TOP_SHARE),
            )
        }
    }
}
