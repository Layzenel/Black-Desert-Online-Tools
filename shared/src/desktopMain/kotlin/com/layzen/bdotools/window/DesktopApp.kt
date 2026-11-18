package com.layzen.bdotools.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.layzen.bdotools.AppRoot
import com.layzen.bdotools.AppScreen
import com.layzen.bdotools.composable.overlay.OverlayContribution
import com.layzen.bdotools.composable.overlay.OverlayHost
import org.koin.compose.getKoin
import com.layzen.bdotools.navigation.AppTab
import com.layzen.bdotools.navigation.Destination
import com.layzen.bdotools.navigation.openTab

/**
 * Настольная версия: окно программы, окна поверх игры, значок в трее и вопрос при закрытии — свернуть в трей или выйти.
 * Окна поверх игры стоят рядом с окном программы, а не внутри него: скрытое в трее окно не перерисовывается, и всё
 * внутри него замирает. ViewModel у всех общая ([ViewModelStoreOwner] приложения), поэтому оверлей и вкладка комбо —
 * одно и то же состояние.
 */
@Composable
fun ApplicationScope.DesktopApp(title: String, icon: Painter) {
    var visible by remember { mutableStateOf(true) }
    var askClose by remember { mutableStateOf(false) }
    var raise by remember { mutableIntStateOf(0) }
    val backStack = remember { mutableStateListOf<Destination>(Destination.Tab(AppTab.Portraits)) }
    val state = rememberWindowState(placement = WindowPlacement.Maximized)
    val owner = remember { object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() } }
    val show: () -> Unit = {
        visible = true
        state.isMinimized = false
        raise++
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        AppRoot(backStack) {
            Window(onCloseRequest = { askClose = true }, state = state, visible = visible, title = title, icon = icon) {
                LaunchedEffect(raise) { if (raise > 0) window.toFront() }
                // окно Compose подставляет своего владельца ViewModel — возвращаем общий
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                    AppScreen(backStack)
                    if (askClose) {
                        CloseChoiceDialog(
                            onTray = { askClose = false; visible = false },
                            onExit = ::exitApplication,
                            onDismiss = { askClose = false },
                        )
                    }
                }
            }
            // окна поверх игры от всех фич; «открыть программу» — на вкладке фичи, если она есть
            val koin = getKoin()
            val contributions = remember { koin.getAll<OverlayContribution>() }
            OverlayHost(contributions) { id ->
                AppTab.entries.firstOrNull { it.route == id }?.let(backStack::openTab)
                show()
            }
            AppTray(icon, title, onOpen = show, onOpenTab = { backStack.openTab(it); show() }, onExit = ::exitApplication)
        }
    }
}
