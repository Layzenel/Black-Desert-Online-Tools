package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/** Переключатель-пилюля: включён — золотой с кремовым кружком справа. */
@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(remember { MutableInteractionSource() }, null, role = Role.Switch) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
    ) {
        Box(
            Modifier
                .size(width = dimens.toggleWidth, height = dimens.toggleHeight)
                .background(if (checked) colors.accentGold else colors.backgroundItem, AppShapes.Pill)
                .border(dimens.borderWidth, if (checked) colors.accentGoldPale else colors.buttonBorder, AppShapes.Pill)
                .padding(dimens.spaceXxs),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(Modifier.size(dimens.toggleThumbSize).background(if (checked) colors.textPrimary else colors.textDisabled, AppShapes.Pill))
        }
        if (label != null || description != null) {
            Column {
                if (label != null) Text(label, style = AppTheme.typography.sectionTitle, color = colors.textBeige)
                if (description != null) DescriptionText(description)
            }
        }
    }
}

/**
 * Вариант из группы (как «Полноэкран. режим / Большое окно / Окно»):
 * квадратик-чекбокс и подпись. Выбранный — коричневая подложка и светлый текст.
 */
@Composable
fun OptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Row(
        modifier
            .heightIn(min = dimens.buttonHeight)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(interaction, null, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .background(if (selected) colors.backgroundSelected else colors.backgroundSurface)
            .border(
                dimens.borderWidth,
                when {
                    selected -> colors.borderSelected
                    hovered && enabled -> colors.buttonBorder
                    else -> colors.border
                },
            )
            .padding(horizontal = dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
    ) {
        Box(
            Modifier.size(dimens.checkboxSize).border(dimens.borderWidth, if (selected) colors.textBeige else colors.textDisabled),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(dimens.checkboxMarkSize).background(colors.danger))
        }
        Text(
            text,
            style = AppTheme.typography.bodyBold,
            color = if (selected) colors.textBeige else colors.textDisabled,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
    }
}

/** Ряд взаимоисключающих вариантов одинаковой ширины. */
@Composable
fun <T> OptionGroup(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        options.forEach { option ->
            OptionButton(
                text = label(option),
                selected = option == selected,
                onClick = { onSelect(option) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Слайдер как «Масштаб интерфейса»: подписи min/max по краям, текущее значение
 * золотом по центру, тонкая тёмная дорожка и белый ромб-ползунок.
 */
@Composable
fun ValueSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val currentOnChange by rememberUpdatedState(onValueChange)
    Column(modifier) {
        if (showLabels) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(range.first.toString(), style = AppTheme.typography.bodyBold, color = colors.textPrimary)
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ValueText(value.toString(), color = colors.accentGold)
                }
                Text(range.last.toString(), style = AppTheme.typography.bodyBold, color = colors.textPrimary)
            }
        }
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(dimens.sliderTouchHeight)
                .pointerHoverIcon(PointerIcon.Hand),
            contentAlignment = Alignment.CenterStart,
        ) {
            val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            fun valueAt(x: Float): Int {
                val fraction = (x / widthPx).coerceIn(0f, 1f)
                return range.first + (fraction * (range.last - range.first)).roundToInt()
            }
            val fraction = (value - range.first).toFloat() / (range.last - range.first).coerceAtLeast(1)
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(range, widthPx) { detectTapGestures { currentOnChange(valueAt(it.x)) } }
                    .pointerInput(range, widthPx) {
                        detectHorizontalDragGestures { change, _ -> currentOnChange(valueAt(change.position.x)) }
                    },
            )
            Box(Modifier.fillMaxWidth().height(dimens.sliderTrackHeight).background(colors.track))
            Box(Modifier.width(maxWidth * fraction).height(dimens.sliderTrackHeight).background(colors.textMuted))
            Box(
                Modifier
                    .offset(x = maxWidth * fraction - dimens.sliderThumbSize / 2)
                    .size(dimens.sliderThumbSize)
                    .rotate(45f)
                    .background(colors.textPrimary),
            )
        }
    }
}

/** Выпадающий список: тёмное поле со значением по центру и треугольником справа. */
@Composable
fun <T> DropdownSelect(
    selected: T,
    options: List<T>,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(Dp.Unspecified) }
    val density = LocalDensity.current
    Box(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidth = with(density) { it.width.toDp() } }
                .heightIn(min = dimens.inputHeight)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(remember { MutableInteractionSource() }, null, role = Role.DropdownList) { expanded = true }
                .background(colors.backgroundItem)
                .border(dimens.borderWidth, colors.border)
                .padding(horizontal = dimens.spaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label(selected),
                style = AppTheme.typography.bodyBold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            // Стандартный шрифт web-сборки не содержит символов вроде ▼, поэтому стрелка рисуется
            Canvas(Modifier.size(width = dimens.dropdownArrowWidth, height = dimens.dropdownArrowHeight)) {
                drawPath(
                    Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2, size.height)
                        close()
                    },
                    colors.textBeige,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = if (anchorWidth != Dp.Unspecified) Modifier.width(anchorWidth) else Modifier,
            containerColor = colors.backgroundRaised,
            shape = AppShapes.Sharp,
            border = BorderStroke(dimens.borderWidth, colors.border),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            label(option),
                            style = AppTheme.typography.bodyBold,
                            color = if (option == selected) colors.accentGold else colors.textPrimary,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

/** Поле ввода (поиск, числа в калькуляторах). Тёмная подложка, бежевая подсказка. */
@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leading: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.Start,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = AppTheme.typography.bodyBold.copy(color = colors.textPrimary, textAlign = textAlign),
        cursorBrush = SolidColor(colors.accentGold),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        decorationBox = { inner ->
            Row(
                Modifier
                    .heightIn(min = dimens.inputHeight)
                    .background(colors.backgroundInput)
                    .border(dimens.borderWidth, colors.border)
                    .padding(horizontal = dimens.spaceS),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
            ) {
                if (leading != null) leading()
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(placeholder, style = AppTheme.typography.bodyBold, color = colors.textMuted)
                    }
                    inner()
                }
            }
        },
    )
}

/** Числовое поле: принимает только цифры, значение за пределами [range] прижимается к краю. */
@Composable
fun NumberField(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..Int.MAX_VALUE,
) {
    InputField(
        value = value.toString(),
        onValueChange = { text ->
            val digits = text.filter(Char::isDigit).take(10)
            onValueChange((digits.toLongOrNull() ?: 0L).coerceIn(range.first.toLong(), range.last.toLong()).toInt())
        },
        modifier = modifier,
        keyboardType = KeyboardType.Number,
        textAlign = TextAlign.End,
    )
}

/** Квадратный чекбокс: тёмная подложка, при отметке — золотая галочка и светлая рамка. */
@Composable
fun CheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box(
        modifier
            .size(dimens.checkBoxTouchSize)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Checkbox) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(dimens.checkBoxSize)
                .background(colors.backgroundInput)
                .border(
                    dimens.borderWidth,
                    when {
                        checked -> colors.accentGoldPale
                        hovered && enabled -> colors.textMuted
                        else -> colors.buttonBorder
                    },
                ),
        ) {
            if (checked) {
                Canvas(Modifier.matchParentSize()) {
                    val stroke = dimens.accentLineWidth.toPx()
                    drawPath(
                        Path().apply {
                            moveTo(size.width * 0.22f, size.height * 0.52f)
                            lineTo(size.width * 0.42f, size.height * 0.72f)
                            lineTo(size.width * 0.78f, size.height * 0.30f)
                        },
                        colors.accentGold,
                        style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
        }
    }
}
