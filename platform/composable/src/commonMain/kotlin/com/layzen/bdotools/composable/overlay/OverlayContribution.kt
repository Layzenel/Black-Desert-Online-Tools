package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable

/**
 * Что фича добавляет поверх игры (только настольная версия). Фича кладёт его в свой Koin-модуль, корень приложения
 * собирает все вклады и показывает их окна и вкладки общего меню оверлея ([OverlayHost]).
 *
 * @property id вкладка программы, которую открывает [OverlayScope.openApp] (маршрут вкладки, например `combo`)
 * @property windows окна фичи поверх игры ([GameOverlayWindow]); параметр — развернуть программу на вкладке фичи
 *   (как [OverlayScope.openApp]), например по клику на уведомление
 * @property tabs вкладки фичи в меню оверлея
 * @property settings пункты фичи в общей вкладке «Настройки» меню оверлея (её собирает [OverlayHost])
 */
class OverlayContribution(
    val id: String,
    val windows: @Composable (openApp: () -> Unit) -> Unit = {},
    val tabs: List<OverlayTab> = emptyList(),
    val settings: (@Composable () -> Unit)? = null,
)
