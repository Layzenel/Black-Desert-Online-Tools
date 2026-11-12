package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.runtime.Composable

/**
 * Зажат ли Alt — клавиша полного тултипа навыка. На Desktop это знает окно Compose, а в браузере Compose его не
 * обновляет: там клавишу слушает сама страница.
 */
@Composable
internal expect fun rememberAltPressed(): Boolean
