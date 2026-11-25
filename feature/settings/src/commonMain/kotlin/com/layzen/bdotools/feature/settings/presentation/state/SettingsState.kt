package com.layzen.bdotools.feature.settings.presentation.state

import com.layzen.bdotools.core.keyhint.KeyHints
import com.layzen.bdotools.core.theme.WindowTheme
import com.layzen.bdotools.feature.settings.domain.startup.StartupOptions

/**
 * Состояние экрана настроек.
 *
 * @property section открытый раздел
 * @property startup автозапуск и права администратора, прочитанные из системы
 * @property startupBusy настройки запуска записываются (может висеть окно UAC): включатели недоступны
 * @property theme выбранная тема окон
 * @property keyHints выбранный вид подсказок клавиш умений
 */
data class SettingsState(
    val section: SettingsSection = SettingsSection.General,
    val startup: StartupOptions = StartupOptions(),
    val startupBusy: Boolean = false,
    val theme: WindowTheme = WindowTheme.Default,
    val keyHints: KeyHints = KeyHints.Default,
)
