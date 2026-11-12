package com.layzen.bdotools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.layzen.bdotools.account.AccountPicker
import com.layzen.bdotools.account.AccountSwitcher
import com.layzen.bdotools.core.account.GameAccounts
import com.layzen.bdotools.core.account.accountsModule
import com.layzen.bdotools.core.language.AppLanguages
import com.layzen.bdotools.core.language.languageModule
import com.layzen.bdotools.feature.portraitconstructor.di.portraitsModule
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsRoute
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.preloadPortraitsStrings
import com.layzen.bdotools.feature.uieditor.di.slotsModule
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsRoute
import com.layzen.bdotools.feature.uieditor.presentation.ui.preloadSlotsStrings
import com.layzen.bdotools.language.LanguageEnvironment
import com.layzen.bdotools.language.LanguageSwitcher
import com.layzen.bdotools.navigation.AppTab
import com.layzen.bdotools.navigation.Destination
import com.layzen.bdotools.navigation.PageTitle
import com.layzen.bdotools.navigation.PlatformNavigation
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.app_title
import com.layzen.bdotools.resources.page_title
import com.layzen.bdotools.uikit.components.TopNavigation
import com.layzen.bdotools.uikit.showcase.ComponentShowcase
import com.layzen.bdotools.uikit.theme.AppTheme
import com.layzen.bdotools.update.di.updateModule
import com.layzen.bdotools.update.presentation.AppUpdateBanner
import com.layzen.bdotools.update.presentation.AppVersionInfo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

/** Koin-модули: общий список аккаунтов, язык интерфейса, обновление и все фичи. Новая фича добавляет сюда свой модуль. */
private val featureModules = listOf(
    accountsModule,
    languageModule,
    updateModule,
    portraitsModule,
    slotsModule,
)

/** Корень приложения, общий для сайта и настольной версии: Koin, тема, язык и экран с верхней навигацией. */
@Composable
@Preview
fun App() {
    KoinApplication(configuration = koinConfiguration { modules(featureModules) }) {
        AppTheme {
            val languages = koinInject<AppLanguages>()
            val language by languages.selected.collectAsState()
            // Стек навигации и его связь с адресом страницы живут снаружи LanguageEnvironment: смена языка
            // не возвращает на первую вкладку и не сбивает адрес
            val backStack = remember { mutableStateListOf<Destination>(Destination.Tab(AppTab.Portraits)) }
            PlatformNavigation(backStack)
            LanguageEnvironment(language) {
                AppContent(backStack, languages)
            }
        }
    }
}

@Composable
private fun AppContent(backStack: SnapshotStateList<Destination>, languages: AppLanguages) {
    val language by languages.selected.collectAsState()
    val current = backStack.last()
    val accounts = koinInject<GameAccounts>()
    val accountsState by accounts.state.collectAsState()
    val selectedAccount = accountsState.selected
    val appTitle = stringResource(Res.string.app_title)
    val tabTitle = (current as? Destination.Tab)?.let { stringResource(it.tab.title).replace('\n', ' ') }
    PageTitle(tabTitle?.let { stringResource(Res.string.page_title, it, appTitle) } ?: appTitle)
    // Строки фич грузятся при первом показе; заранее — чтобы вкладка не мелькала пустыми кнопками
    LaunchedEffect(language) {
        preloadPortraitsStrings()
        preloadSlotsStrings()
    }

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
                if (accountsState.accounts.size > 1 && selectedAccount != null) {
                    AccountSwitcher(selectedAccount, accountsState.accounts, accounts::select, accounts::readPortrait)
                }
                LanguageSwitcher(language, languages::select)
                AppVersionInfo()
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
