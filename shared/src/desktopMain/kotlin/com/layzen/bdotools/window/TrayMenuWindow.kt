package com.layzen.bdotools.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.layzen.bdotools.navigation.AppTab
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.window_close_exit
import com.layzen.bdotools.uikit.components.ActionMenuDivider
import com.layzen.bdotools.uikit.components.ActionMenuItem
import com.layzen.bdotools.uikit.components.TooltipPanel
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle
import com.sun.jna.platform.win32.User32
import kotlinx.coroutines.delay
import java.awt.MouseInfo
import kotlin.math.ceil

/**
 * Меню значка в трее в стиле программы: вкладки по порядку ([onOpenTab]) и «Закрыть» ([onExit]) на подложке
 * подсказки. Встаёт над курсором [at] (у края экрана — внутрь) и закрывается ([onDismiss]) по нажатию мыши мимо него.
 * Окно не берёт фокус, как системные меню: иначе Windows закрывает панель скрытых значков трея. Диалог без
 * владельца, а не `Window`: у него нет кнопки в панели задач. Показывается, только когда содержимое
 * замерено и окно встало на место: иначе мелькнуло бы в углу экрана.
 */
@Composable
internal fun TrayMenuWindow(at: Point, onOpenTab: (AppTab) -> Unit, onExit: () -> Unit, onDismiss: () -> Unit) {
    val start = AppTheme.dimens.contextMenuWidth
    val state = rememberDialogState(size = DpSize(start, start))
    var placed by remember { mutableStateOf(false) }
    val dismiss by rememberUpdatedState(onDismiss)
    DialogWindow(
        onCloseRequest = onDismiss,
        state = state,
        visible = placed,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        LaunchedEffect(placed) {
            if (!placed) return@LaunchedEffect
            window.toFront()
            while (!clickedOutside(window.bounds)) delay(CLICK_POLL_MS)
            dismiss()
        }
        val density = LocalDensity.current
        Box(
            Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).onSizeChanged {
                // размер и место — прямо окну: через состояние диалога после замера они не применялись
                val size = with(density) { DpSize(it.width.toDp(), it.height.toDp()) }
                window.bounds = aboveCursor(at, size)
                placed = true
            },
        ) {
            TrayMenu(onOpenTab, onExit)
        }
    }
}

/** Как часто проверять, не нажата ли мышь мимо меню. */
private const val CLICK_POLL_MS = 30L

/** Кнопки мыши (`VK_LBUTTON`, `VK_RBUTTON`, `VK_MBUTTON`) для `GetAsyncKeyState`. */
private val MOUSE_BUTTONS = intArrayOf(0x01, 0x02, 0x04)

/**
 * Нажата ли сейчас кнопка мыши вне [bounds]. Окно без фокуса не узнаёт о кликах в других программах, поэтому
 * состояние кнопок спрашивается у Windows; это не хук, ввод не перехватывается.
 */
private fun clickedOutside(bounds: Rectangle): Boolean =
    MOUSE_BUTTONS.any { User32.INSTANCE.GetAsyncKeyState(it).toInt() and 0x8000 != 0 } &&
        !bounds.contains(MouseInfo.getPointerInfo().location)

/** Пункты меню трея: вкладки программы, разделитель и «Закрыть». */
@Composable
private fun TrayMenu(onOpenTab: (AppTab) -> Unit, onExit: () -> Unit) {
    TooltipPanel {
        // ширина — по самой длинной подписи, отступы пунктов остаются свои
        Column(Modifier.width(IntrinsicSize.Max)) {
            AppTab.entries.forEach { tab ->
                ActionMenuItem(stringResource(tab.title).replace('\n', ' '), onClick = { onOpenTab(tab) })
            }
            ActionMenuDivider()
            ActionMenuItem(stringResource(Res.string.window_close_exit), onClick = onExit)
        }
    }
}

/**
 * Место окна меню размера [size] (dp — это и логические пиксели AWT): левый край у курсора [at], низ над ним,
 * внутри рабочей области экрана (без панели задач).
 */
private fun aboveCursor(at: Point, size: DpSize): Rectangle {
    val screen = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
    val width = ceil(size.width.value).toInt()
    val height = ceil(size.height.value).toInt()
    val x = at.x.coerceAtMost(screen.x + screen.width - width).coerceAtLeast(screen.x)
    val y = (at.y - height).coerceAtLeast(screen.y)
    return Rectangle(x, y, width, height)
}
