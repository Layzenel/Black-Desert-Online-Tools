package com.layzen.bdotools.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Связывает [backStack] с платформой. В браузере — с адресом страницы и кнопками «Назад» и «Вперёд»,
 * в настольном приложении связывать не с чем.
 */
@Composable
internal expect fun PlatformNavigation(backStack: SnapshotStateList<Destination>)
