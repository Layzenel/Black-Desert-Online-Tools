package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlinx.coroutines.delay

/**
 * Контекстное меню по правому клику: тёмная панель с пунктами [ActionMenuItem] и [ActionMenuDivider].
 * Появляется в левом нижнем углу родителя — поставь его в пустой `Box` в точке клика, см. [onSecondaryClick].
 *
 * Вложенные панели: у каждой свой ключ [K]. Поставь `Modifier`, который вернёт `submenuTrigger(ключ)`, на пункт,
 * при наведении на который должна раскрыться панель; её содержимое [submenu] получает этот ключ. Панель открывается
 * справа от меню, пока курсор над пунктом или над ней, и прячется, когда курсор ушёл с обоих. Одновременно открыта одна.
 * Верх панели — на уровне её пункта.
 */
@Composable
fun <K : Any> ContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    submenu: @Composable ColumnScope.(K) -> Unit,
    content: @Composable ColumnScope.(submenuTrigger: @Composable (K) -> Modifier) -> Unit,
) {
    var hoveredKey by remember(expanded) { mutableStateOf<K?>(null) }
    var openKey by remember(expanded) { mutableStateOf<K?>(null) }
    // верх каждого пункта-триггера в колонке меню: панель встаёт напротив своего пункта
    val anchorTops = remember { mutableStateMapOf<K, Float>() }
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
        Modifier.hoverable(source).onGloballyPositioned { anchorTops[key] = it.positionInParent().y }
    }
    // у каждой панели своя подложка и рамка, поэтому окно прозрачное
    MenuPopup(expanded, onDismissRequest, modifier, framed = false) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.width(AppTheme.dimens.contextMenuWidth).menuPanel()) { content(trigger) }
            openKey?.let { key ->
                val top = with(LocalDensity.current) { (anchorTops[key] ?: 0f).toDp() }
                Column(Modifier.padding(top = top).width(AppTheme.dimens.contextMenuWidth).menuPanel().hoverable(submenuHover)) { submenu(key) }
            }
        }
    }
}
