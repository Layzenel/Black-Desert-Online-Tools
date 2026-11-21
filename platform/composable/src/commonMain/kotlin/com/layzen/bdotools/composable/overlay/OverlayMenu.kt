package com.layzen.bdotools.composable.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.composable.resources.Res
import com.layzen.bdotools.composable.resources.overlay_app_icon
import com.layzen.bdotools.composable.resources.overlay_panel_title
import com.layzen.bdotools.uikit.components.CloseButton
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Кнопка меню оверлея — значок программы — и меню под ней: окно «BDO Tools оверлей» с вкладками всех фич
 * ([tabs]: маршрут фичи и вкладка). [open] — меню раскрыто, [onOpenChange] — открыть или закрыть.
 * [dragModifier] — перетаскивание окна кнопки зажатой ЛКМ; [alignEnd] — меню под кнопкой по её правому краю.
 */
@Composable
internal fun OverlayMenu(
    tabs: List<Pair<String, OverlayTab>>,
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onOpenApp: (id: String) -> Unit,
    dragModifier: Modifier,
    alignEnd: Boolean = false,
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        Image(
            painterResource(Res.drawable.overlay_app_icon), null,
            Modifier.size(OverlayDimens.menuButton).pointerHoverIcon(PointerIcon.Hand)
                // перетаскивание раньше клика: сдвиг с зажатой ЛКМ двигает окно, а клик без сдвига открывает меню
                .then(dragModifier)
                // без индикации: иначе после нажатия на квадрате значка остаётся серая подложка фокуса
                .clickable(remember { MutableInteractionSource() }, indication = null) { onOpenChange(!open) },
        )
        if (open) {
            // то, что вкладка показывает рядом с меню, — со стороны центра экрана: у правого края — слева
            var aside by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                if (alignEnd) aside?.invoke()
                OverlayPanel(tabs, onOpenApp = { id -> onOpenApp(id); onOpenChange(false) }, onClose = { onOpenChange(false) }, onAside = { aside = it })
                if (!alignEnd) aside?.invoke()
            }
        }
    }
}

/** Окно меню с вкладками; открытая вкладка запоминается, пока меню закрыто. [onAside] — что показать рядом с меню. */
@Composable
private fun OverlayPanel(tabs: List<Pair<String, OverlayTab>>, onOpenApp: (String) -> Unit, onClose: () -> Unit, onAside: ((@Composable () -> Unit)?) -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    val index = selected.coerceIn(tabs.indices)
    val (id, tab) = tabs[index]
    val scope = object : OverlayScope {
        override fun openApp() = onOpenApp(id)
        override fun aside(content: (@Composable () -> Unit)?) = onAside(content)
    }
    GameWindow(stringResource(Res.string.overlay_panel_title), Modifier.width(OverlayDimens.panelWidth), actions = { CloseButton(onClose) }) {
        Column(Modifier.padding(AppTheme.dimens.spaceM), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
            OptionGroup(tabs.indices.toList(), index, { selected = it }, { stringResource(tabs[it].second.title) })
            tab.content(scope)
        }
    }
}
