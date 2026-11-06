package com.layzen.bdotools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.layzen.bdotools.account.AccountPicker
import com.layzen.bdotools.account.AccountSwitcher
import com.layzen.bdotools.core.account.GameAccounts
import com.layzen.bdotools.core.account.accountsModule
import com.layzen.bdotools.feature.portraits.di.portraitsModule
import com.layzen.bdotools.feature.portraits.presentation.ui.PortraitsRoute
import com.layzen.bdotools.feature.uieditor.di.slotsModule
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsRoute
import com.layzen.bdotools.navigation.AppTab
import com.layzen.bdotools.navigation.Destination
import com.layzen.bdotools.navigation.PlaceholderScreen
import com.layzen.bdotools.navigation.PlatformNavigation
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.app_title
import com.layzen.bdotools.uikit.components.TopNavigation
import com.layzen.bdotools.uikit.showcase.ComponentShowcase
import com.layzen.bdotools.uikit.theme.AppTheme
import com.layzen.bdotools.update.AppUpdateBanner
import com.layzen.bdotools.update.AppVersionInfo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

/** Koin-модули: общий список аккаунтов и все фичи. Новая фича добавляет сюда свой модуль. */
private val featureModules = listOf(
    accountsModule,
    portraitsModule,
    slotsModule,
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
    PlatformNavigation(backStack)
    val current = backStack.last()
    val accounts = koinInject<GameAccounts>()
    val accountsState by accounts.state.collectAsState()
    val selectedAccount = accountsState.selected

    Column(Modifier.fillMaxSize().background(AppTheme.colors.backgroundDeep)) {
        TopNavigation(
            tabs = AppTab.entries,
            selected = (current as? Destination.Tab)?.tab,
            onSelect = { tab ->
                if (current != Destination.Tab(tab)) backStack.add(Destination.Tab(tab))
            },
            label = { stringResource(it.title) },
            icon = { painterResource(it.icon) },
            title = stringResource(Res.string.app_title),
            trailing = {
                AppVersionInfo()
                if (accountsState.accounts.size > 1 && selectedAccount != null) {
                    AccountSwitcher(selectedAccount, accountsState.accounts, accounts::select)
                }
            },
        )
        if (accountsState.needsChoice) {
            AccountPicker(accountsState.accounts, accounts::select, accounts::readPortrait)
        }
        AppUpdateBanner()
        Box(Modifier.weight(1f)) {
            when (current) {
                Destination.Showcase -> ComponentShowcase()
                is Destination.Tab -> Box(
                    Modifier
                        .fillMaxSize()
                        .then(
                            if (current.tab.fillsScreen) Modifier
                            else Modifier.verticalScroll(rememberScrollState()).padding(AppTheme.dimens.spaceXl),
                        ),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    TabContent(current.tab)
                }
            }
            DisclaimerFooter(Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun TabContent(tab: AppTab) {
    when (tab) {
        AppTab.Portraits -> PortraitsRoute()
        AppTab.Slots -> SlotsRoute()
    }
}
