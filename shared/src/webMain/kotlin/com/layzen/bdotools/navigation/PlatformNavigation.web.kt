package com.layzen.bdotools.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.github.terrakok.navigation3.browser.ChronologicalBrowserNavigation

@Composable
internal actual fun PlatformNavigation(backStack: SnapshotStateList<Destination>) {
    ChronologicalBrowserNavigation(
        backStack = backStack,
        saveKey = { it.toFragment() },
        restoreKey = ::destinationFromFragment,
    )
}
