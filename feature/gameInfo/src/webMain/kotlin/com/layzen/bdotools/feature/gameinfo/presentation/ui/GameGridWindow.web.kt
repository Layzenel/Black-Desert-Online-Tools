package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.gameinfo.domain.grid.GridStep

/** В браузере окон поверх игры нет. */
@Composable
internal actual fun GameGridWindow(step: GridStep, uiHeight: Double) = Unit
