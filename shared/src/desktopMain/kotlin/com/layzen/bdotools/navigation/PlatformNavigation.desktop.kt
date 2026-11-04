package com.layzen.bdotools.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList

@Composable
internal actual fun PlatformNavigation(backStack: SnapshotStateList<Destination>) = Unit
