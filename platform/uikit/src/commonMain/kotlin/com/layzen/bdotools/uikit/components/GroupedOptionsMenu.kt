package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.delay

/** Сколько вложенная панель ждёт после ухода курсора, прежде чем спрятаться: за это время курсор успевает дойти до неё. */
internal const val SUBMENU_CLOSE_DELAY_MS = 150L

/**
 * Двухуровневый список: слева группы, при наведении на группу (или клике) справа раскрываются её варианты.
 * Когда курсор уходит и с групп, и с вариантов, варианты прячутся.
 * Обе колонки — в одном всплывающем окне, поэтому клик по варианту не закрывает список раньше времени.
 * Колонка вариантов не выше `optionsMenuMaxHeight` и прокручивается с полосой прокрутки.
 */
@Composable
fun <G, T> GroupedOptionsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    groups: List<G>,
    groupLabel: @Composable (G) -> String,
    options: (G) -> List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    groupIcon: (@Composable (G) -> Unit)? = null,
    icon: (@Composable (T) -> Unit)? = null,
) {
    var open by remember(expanded) { mutableStateOf<G?>(null) }
    var hoveredGroup by remember(expanded) { mutableStateOf<G?>(null) }
    val optionsHover = remember { MutableInteractionSource() }
    val optionsHovered by optionsHover.collectIsHoveredAsState()
    // варианты видны, пока курсор над своей группой или над ними; пауза — чтобы список не мигал,
    // пока курсор переходит с группы на варианты
    LaunchedEffect(hoveredGroup, optionsHovered) {
        val group = hoveredGroup
        if (group != null) {
            open = group
        } else if (!optionsHovered) {
            delay(SUBMENU_CLOSE_DELAY_MS)
            open = null
        }
    }
    // у каждой колонки своя панель: колонка групп не растягивается до высоты длинного списка вариантов
    MenuPopup(expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier, framed = false) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.width(IntrinsicSize.Max).menuPanel()) {
                groups.forEach { group ->
                    GroupItem(
                        text = groupLabel(group),
                        open = group == open,
                        icon = groupIcon?.let { { it(group) } },
                        onOpen = { open = group },
                        onHoverChange = { hovered ->
                            if (hovered) hoveredGroup = group else if (hoveredGroup == group) hoveredGroup = null
                        },
                    )
                }
            }
            open?.let { group ->
                OptionsColumn(options(group), selected, label, icon, Modifier.hoverable(optionsHover)) { option ->
                    onDismissRequest()
                    onSelect(option)
                }
            }
        }
    }
}

/** Колонка вариантов группы: своя панель, не выше `optionsMenuMaxHeight`, с полосой прокрутки. */
@Composable
private fun <T> OptionsColumn(
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    icon: (@Composable (T) -> Unit)?,
    modifier: Modifier,
    onClick: (T) -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier
            .width(AppTheme.dimens.groupedMenuOptionsWidth)
            .menuPanel()
            .heightIn(max = AppTheme.dimens.optionsMenuMaxHeight)
            .verticalScrollbar(scroll)
            .verticalScroll(scroll),
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { MenuOptionText(label(option), highlighted = option == selected) },
                leadingIcon = icon?.let { { it(option) } },
                onClick = { onClick(option) },
            )
        }
    }
}

/** Пункт группы: стрелка вправо; клик раскрывает варианты, о наведении сообщает [onHoverChange]. */
@Composable
private fun GroupItem(
    text: String,
    open: Boolean,
    icon: (@Composable () -> Unit)?,
    onOpen: () -> Unit,
    onHoverChange: (Boolean) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    LaunchedEffect(hovered) { onHoverChange(hovered) }
    DropdownMenuItem(
        text = { MenuOptionText(text, highlighted = open) },
        leadingIcon = icon,
        trailingIcon = { SubmenuArrow() },
        onClick = onOpen,
        interactionSource = interaction,
    )
}

/** Треугольник вправо — признак вложенного списка. Нарисован: в шрифте web-сборки нет символа ›. */
@Composable
private fun SubmenuArrow() {
    val color = AppTheme.colors.textMuted
    val dimens = AppTheme.dimens
    // отступ слева отодвигает стрелку от названия группы к правому краю колонки
    Canvas(Modifier.padding(start = dimens.spaceXl).size(width = dimens.dropdownArrowHeight, height = dimens.dropdownArrowWidth)) {
        drawPath(
            Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2)
                lineTo(0f, size.height)
                close()
            },
            color,
        )
    }
}
