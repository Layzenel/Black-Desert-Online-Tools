package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLabel
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Место, которое займёт надпись слота в игре: полоса шириной [chars] символов под рамкой, по центру слота.
 * [text] — сама надпись навыка из игры: тогда полоса по ширине текста и с текстом внутри. Клики проходят сквозь неё
 * к холсту. У скрытого в игре слота приглушена, как и сам слот.
 */
@Composable
internal fun SlotLabelStub(
    id: SlotId,
    position: SlotPosition,
    screen: ScreenSpace,
    scale: Float,
    chars: Int,
    visible: Boolean,
    text: String? = null,
) {
    val centerX = screen.iconLeft(id, position) + id.width / 2f
    val top = screen.iconTop(id, position) + id.height + SlotLabel.GAP
    val density = LocalDensity.current
    Box(
        Modifier
            // По центру слота при любой ширине: у надписи она по тексту, а не по числу символов
            .layout { measurable, _ ->
                val placeable = measurable.measure(Constraints())
                layout(placeable.width, placeable.height) {
                    placeable.place((centerX * scale - placeable.width / 2f).roundToInt(), (top * scale).roundToInt())
                }
            }
            .then(
                if (text == null) Modifier.width(with(density) { (chars * SlotLabel.CHAR_WIDTH * scale).toDp() })
                else Modifier.padding(horizontal = with(density) { (SlotLabel.TEXT_PADDING * scale).toDp() }),
            )
            .height(with(density) { (SlotLabel.HEIGHT * scale).toDp() })
            .alpha(if (visible) 1f else SlotsDimens.HIDDEN_ALPHA)
            .background(AppTheme.colors.textPrimary.copy(alpha = SlotsDimens.LABEL_FILL_ALPHA))
            .border(SlotsDimens.slotBorder, AppTheme.colors.textPrimary),
        contentAlignment = Alignment.Center,
    ) {
        if (text != null) {
            Text(
                text,
                style = AppTheme.typography.caption.copy(
                    fontSize = with(density) { (SlotLabel.HEIGHT * SlotsDimens.MEMO_TEXT_SHARE * scale).toSp() },
                    lineHeight = with(density) { (SlotLabel.HEIGHT * scale).toSp() },
                ),
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
