package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Stable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.delay

/**
 * Контекстное меню по правому клику: тёмная панель с пунктами [ActionMenuItem] и [ActionMenuDivider].
 * Появляется от левого верхнего угла родителя — поставь его в пустой `Box` в точке клика, см. [onSecondaryClick].
 * Не влезает в окно справа или снизу — раскрывается влево или вверх от точки клика. После появления меню стоит
 * на месте, что бы ни раскрывалось рядом.
 *
 * Вложенные панели: у каждой свой ключ [K]. Поставь `Modifier`, который вернёт `submenuTrigger(ключ)`, на пункт,
 * при наведении на который должна раскрыться панель; её содержимое [submenu] получает этот ключ. Панель открывается
 * справа от меню (у правого края окна — слева), пока курсор над пунктом или над ней, и прячется, когда курсор ушёл
 * с обоих. Одновременно открыта одна. Верх панели — на уровне её пункта; у нижнего края окна панель сдвигается
 * вверх, чтобы влезть, а само меню не двигается. Нажатие мимо меню закрывает его.
 */
@Composable
fun <K : Any> ContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    submenu: @Composable ColumnScope.(K) -> Unit,
    content: @Composable ColumnScope.(submenuTrigger: @Composable (K) -> Modifier) -> Unit,
) {
    if (!expanded) return
    var hoveredKey by remember { mutableStateOf<K?>(null) }
    var openKey by remember { mutableStateOf<K?>(null) }
    // верх каждого пункта-триггера в колонке меню: панель встаёт напротив своего пункта
    val anchorTops = remember { mutableStateMapOf<K, Int>() }
    val submenuHover = remember { MutableInteractionSource() }
    val submenuHovered by submenuHover.collectIsHoveredAsState()
    // пауза перед закрытием — чтобы панель не мигала, пока курсор переходит с пункта на панель
    LaunchedEffect(hoveredKey, submenuHovered) {
        val key = hoveredKey
        if (key != null) {
            openKey = key
        } else if (!submenuHovered) {
            delay(SUBMENU_CLOSE_DELAY_MS)
            openKey = null
        }
    }
    val trigger: @Composable (K) -> Modifier = { key ->
        val source = remember(key) { MutableInteractionSource() }
        val hovered by source.collectIsHoveredAsState()
        LaunchedEffect(hovered) {
            if (hovered) hoveredKey = key else if (hoveredKey == key) hoveredKey = null
        }
        Modifier.hoverable(source).onGloballyPositioned { anchorTops[key] = it.positionInParent().y.toInt() }
    }
    // Где стоит окно меню: точку клика и размер окна приложения сообщает позиционер, углы меню и панели считает разметка
    val placement = remember { MenuPlacement() }
    val positionProvider = remember(placement.origin) { MenuPositionProvider(placement) }
    // Окно по размеру меню: нажатие мимо него доходит до страницы (правый клик в другом месте откроет там новое меню),
    // а само меню закрывается. Не фокусируемое: фокусируемое окно в веб-сборке после закрытия съедает следующий клик
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = false, dismissOnClickOutside = true),
    ) {
        val point = placement.anchor ?: return@Popup
        val window = placement.window ?: return@Popup
        MenuLayout(
            point = point,
            window = window,
            itemTop = anchorTops[openKey] ?: 0,
            onOrigin = { placement.origin = it },
            menu = { Column(modifier.width(AppTheme.dimens.contextMenuWidth).menuPanel()) { content(trigger) } },
            panel = openKey?.let { key ->
                { Column(Modifier.width(AppTheme.dimens.contextMenuWidth).menuPanel().hoverable(submenuHover)) { submenu(key) } }
            },
        )
    }
}

/**
 * Меню у точки [point] и вложенная панель напротив пункта [itemTop], обе внутри окна [window] (координаты окна).
 * Размер разметки — рамка вокруг меню и панели; её левый верхний угол в окне отдаётся в [onOrigin].
 */
@Composable
private fun MenuLayout(
    point: IntOffset,
    window: IntSize,
    itemTop: Int,
    onOrigin: (IntOffset) -> Unit,
    menu: @Composable () -> Unit,
    panel: (@Composable () -> Unit)?,
) {
    Layout(
        content = {
            menu()
            panel?.invoke()
        },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val menuPlaceable = measurables[0].measure(loose)
        val panelPlaceable = measurables.getOrNull(1)?.measure(loose)
        val menuAt = menuPosition(point, IntSize(menuPlaceable.width, menuPlaceable.height), window)
        val panelAt = panelPlaceable?.let { panelPosition(menuAt, menuPlaceable.width, IntSize(it.width, it.height), itemTop, window) }
        val origin = IntOffset(minOf(menuAt.x, panelAt?.x ?: menuAt.x), minOf(menuAt.y, panelAt?.y ?: menuAt.y))
        val right = maxOf(menuAt.x + menuPlaceable.width, panelAt?.let { it.x + panelPlaceable.width } ?: 0)
        val bottom = maxOf(menuAt.y + menuPlaceable.height, panelAt?.let { it.y + panelPlaceable.height } ?: 0)
        onOrigin(origin)
        layout(right - origin.x, bottom - origin.y) {
            menuPlaceable.place(menuAt - origin)
            if (panelAt != null) panelPlaceable.place(panelAt - origin)
        }
    }
}

/** Левый верхний угол меню: от точки клика вправо и вниз, а где не влезает — влево или вверх от неё. */
private fun menuPosition(point: IntOffset, menu: IntSize, window: IntSize) = IntOffset(
    x = (if (point.x + menu.width > window.width) point.x - menu.width else point.x).coerceAtLeast(0),
    y = (if (point.y + menu.height > window.height) point.y - menu.height else point.y).coerceAtLeast(0),
)

/** Левый верхний угол вложенной панели: справа от меню напротив пункта [itemTop], прижатая внутрь окна. */
private fun panelPosition(menuAt: IntOffset, menuWidth: Int, panel: IntSize, itemTop: Int, window: IntSize): IntOffset {
    val right = menuAt.x + menuWidth
    val x = if (right + panel.width > window.width) menuAt.x - panel.width else right
    val y = (menuAt.y + itemTop).coerceAtMost(window.height - panel.height).coerceAtLeast(0)
    return IntOffset(x.coerceAtLeast(0), y)
}

/** Где окно меню: точка клика и размер окна приложения (их знает позиционер) и угол окна меню (его считает разметка). */
@Stable
private class MenuPlacement {
    var anchor by mutableStateOf<IntOffset?>(null)
    var window by mutableStateOf<IntSize?>(null)
    var origin by mutableStateOf(IntOffset.Zero)
}

/** Ставит окно меню в [MenuPlacement.origin] и сообщает точку клика и размер окна приложения. */
private class MenuPositionProvider(private val placement: MenuPlacement) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        if (placement.anchor != anchorBounds.topLeft) placement.anchor = anchorBounds.topLeft
        if (placement.window != windowSize) placement.window = windowSize
        return placement.origin
    }
}
