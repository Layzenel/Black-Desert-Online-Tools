package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import com.layzen.bdotools.composable.overlay.GameOverlayWindow
import com.layzen.bdotools.composable.overlay.OverlayAnchor
import com.layzen.bdotools.composable.overlay.OverlayContribution
import com.layzen.bdotools.feature.screenshoteditor.presentation.ScreenshotEditorViewModel
import com.layzen.bdotools.feature.screenshoteditor.presentation.alert.ScreenshotAlertViewModel
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.resources.Res
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_alert_count
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_alert_edit
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_alert_edit_tooltip
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_alert_hint
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_alert_setting
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_alert_setting_description
import com.layzen.bdotools.feature.screenshoteditor.resources.screenshots_alert_title
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ToggleSwitch
import com.layzen.bdotools.uikit.components.TooltipPanel
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Маршрут вкладки редактора: «Редактировать» разворачивает программу на ней. */
private const val SCREENSHOTS_ID = "screenshots"

/** Окно уведомления запоминает место под этим именем. */
private const val ALERT_WINDOW = "screenshot-alert"

/** Что редактор добавляет поверх игры: уведомление о новом скриншоте и его включатель в «Настройках» меню оверлея. */
internal fun screenshotOverlayContribution(): OverlayContribution = OverlayContribution(
    id = SCREENSHOTS_ID,
    windows = { openApp -> ScreenshotAlertWindow(koinViewModel(), koinViewModel(), openApp) },
    settings = { AlertSetting(koinViewModel()) },
)

/**
 * «Новый скриншот — Редактировать» справа над игрой. Кликнуть можно, только пока курсор показан: в бою клики
 * уходят сквозь окно в игру, поэтому подсказка про курсор — прямо в уведомлении.
 */
@Composable
private fun ScreenshotAlertWindow(alert: ScreenshotAlertViewModel, editor: ScreenshotEditorViewModel, openApp: () -> Unit) {
    val state by alert.state.collectAsState()
    val latest = state.latest ?: return
    val width = ScreenshotEditorDimens.alertWidth
    GameOverlayWindow(ALERT_WINDOW, OverlayAnchor.BottomRight, DpSize(width, width)) {
        TooltipPanel {
            Column(Modifier.width(width), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                val title = if (state.count > 1) {
                    stringResource(Res.string.screenshots_alert_count, state.count)
                } else {
                    stringResource(Res.string.screenshots_alert_title)
                }
                Text(title, style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textBeige)
                DescriptionText(latest.name)
                ActionButton(
                    ActionButtonState.Positive(
                        stringResource(Res.string.screenshots_alert_edit),
                        onClick = {
                            editor.onEvent(ScreenshotEditorEvent.OpenEntry(latest))
                            alert.dismiss()
                            openApp()
                        },
                        tooltip = stringResource(Res.string.screenshots_alert_edit_tooltip),
                    ),
                    Modifier.fillMaxWidth(),
                )
                DescriptionText(stringResource(Res.string.screenshots_alert_hint))
            }
        }
    }
}

/** Включатель предложения в общей вкладке «Настройки» меню оверлея. */
@Composable
private fun AlertSetting(alert: ScreenshotAlertViewModel) {
    if (!alert.supported) return
    val state by alert.state.collectAsState()
    ToggleSwitch(
        state.enabled,
        alert::setEnabled,
        label = stringResource(Res.string.screenshots_alert_setting),
        description = stringResource(Res.string.screenshots_alert_setting_description),
    )
}
