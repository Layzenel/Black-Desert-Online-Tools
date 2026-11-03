package com.layzen.bdotools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.terrakok.navigation3.browser.ChronologicalBrowserNavigation
import com.layzen.bdotools.feature.portraits.di.portraitsModule
import com.layzen.bdotools.feature.portraits.presentation.PortraitsRoute
import com.layzen.bdotools.navigation.AppTab
import com.layzen.bdotools.navigation.Destination
import com.layzen.bdotools.navigation.PlaceholderScreen
import com.layzen.bdotools.navigation.destinationFromFragment
import com.layzen.bdotools.navigation.toFragment
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.app_title
import com.layzen.bdotools.uikit.components.TopNavigation
import com.layzen.bdotools.uikit.showcase.ComponentShowcase
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

/** Koin-модули всех фич. Новая фича добавляет сюда свой модуль. */
private val featureModules = listOf(
    portraitsModule,
)

@Composable
@Preview
fun App() {
    KoinApplication(configuration = koinConfiguration { modules(featureModules) }) {
        AppTheme {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    val backStack = remember { mutableStateListOf<Destination>(Destination.Tab(AppTab.Portraits)) }
    ChronologicalBrowserNavigation(
        backStack = backStack,
        saveKey = { it.toFragment() },
        restoreKey = ::destinationFromFragment,
    )
    val current = backStack.last()

    Column(Modifier.fillMaxSize().background(AppTheme.colors.backgroundDeep)) {
        TopNavigation(
            tabs = AppTab.entries,
            selected = (current as? Destination.Tab)?.tab,
            onSelect = { tab ->
                if (current != Destination.Tab(tab)) backStack.add(Destination.Tab(tab))
            },
            label = { stringResource(it.title) },
            title = stringResource(Res.string.app_title),
        )
        when (current) {
            Destination.Showcase -> ComponentShowcase()
            is Destination.Tab -> Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppTheme.dimens.spaceXl),
                contentAlignment = Alignment.TopCenter,
            ) {
                TabContent(current.tab)
            }
        }
    }
}

@Composable
private fun TabContent(tab: AppTab) {
    when (tab) {
        AppTab.Portraits -> PortraitsRoute()
        AppTab.Tab2, AppTab.Tab3, AppTab.Tab4, AppTab.Tab5 -> PlaceholderScreen(stringResource(tab.title))
    }
}
