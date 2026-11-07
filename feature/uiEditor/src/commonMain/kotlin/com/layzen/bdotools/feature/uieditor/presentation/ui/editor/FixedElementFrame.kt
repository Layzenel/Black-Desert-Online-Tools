package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.uieditor.domain.FixedElement
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_fixed_menu_bar
import com.layzen.bdotools.feature.uieditor.resources.slots_fixed_tips
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** Неподвижный элемент игры на холсте: серая рамка с прозрачно-красной заливкой, как в режиме правки игры, и названием. Нажатия не перехватывает. */
@Composable
internal fun FixedElementFrame(element: FixedElement, screen: ScreenSpace, scale: Float) {
    val density = LocalDensity.current
    val area = element.area(screen)
    val title = when (element) {
        FixedElement.Tips -> stringResource(Res.string.slots_fixed_tips)
        FixedElement.MenuBar -> stringResource(Res.string.slots_fixed_menu_bar)
    }
    Box(
        Modifier
            .offset { IntOffset((area.left * scale).roundToInt(), (area.top * scale).roundToInt()) }
            .size(
                with(density) { ((area.right - area.left).toFloat() * scale).toDp() },
                with(density) { ((area.bottom - area.top).toFloat() * scale).toDp() },
            )
            .background(fixedElementFill())
            .border(SlotsDimens.slotBorder, fixedElementColor()),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, style = AppTheme.typography.caption, color = AppTheme.colors.textDisabled, maxLines = 1, overflow = TextOverflow.Clip)
    }
}

/** Заливка неподвижных элементов — прозрачно-красная, как в режиме правки игры; на холсте и в легенде одинаковая. */
@Composable
internal fun fixedElementFill() = AppTheme.colors.danger.copy(alpha = SlotsDimens.FIXED_FILL_ALPHA)

/** Цвет рамки неподвижных элементов: на холсте и в легенде одинаковый. */
@Composable
internal fun fixedElementColor() = AppTheme.colors.textDisabled
