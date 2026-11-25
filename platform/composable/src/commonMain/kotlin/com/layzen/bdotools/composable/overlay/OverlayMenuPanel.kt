package com.layzen.bdotools.composable.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.composable.resources.Res
import com.layzen.bdotools.composable.resources.overlay_panel_title
import com.layzen.bdotools.uikit.components.WindowCloseButton
import com.layzen.bdotools.uikit.components.windowDecorationHeight
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.TabBar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Меню оверлея: окно «BDO Tools оверлей» с вкладками всех фич ([tabs]: маршрут фичи и вкладка).
 * [dragModifier] — перетаскивание окна меню за шапку. То, что вкладка показывает рядом с меню
 * ([OverlayScope.aside]), стоит со стороны центра экрана: при [alignEnd] — слева.
 */
@Composable
internal fun OverlayMenuPanel(
    tabs: List<Pair<String, OverlayTab>>,
    onOpenApp: (id: String) -> Unit,
    onClose: () -> Unit,
    dragModifier: Modifier,
    alignEnd: Boolean,
) {
    var selected by remember { mutableIntStateOf(0) }
    var aside by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    val index = selected.coerceIn(tabs.indices)
    val (id, tab) = tabs[index]
    val scope = object : OverlayScope {
        override fun openApp() {
            onOpenApp(id)
            onClose()
        }
        override fun aside(content: (@Composable () -> Unit)?) {
            aside = content
        }
    }
    // место над окном под украшение темы: окно меню — отдельное окно ОС, за его краем ничего не рисуется
    Row(Modifier.padding(top = windowDecorationHeight()), horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        if (alignEnd) aside?.invoke()
        GameWindow(
            stringResource(Res.string.overlay_panel_title),
            Modifier.width(OverlayDimens.panelWidth),
            titleBarModifier = dragModifier,
            actions = { WindowCloseButton(onClose) },
        ) {
            Column(Modifier.padding(AppTheme.dimens.spaceM), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
                TabBar(tabs.indices.toList(), index, { selected = it }, { stringResource(tabs[it].second.title) })
                tab.content(scope)
            }
        }
        if (!alignEnd) aside?.invoke()
    }
}
