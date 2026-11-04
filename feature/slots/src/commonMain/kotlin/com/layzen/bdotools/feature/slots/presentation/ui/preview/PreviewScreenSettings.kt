package com.layzen.bdotools.feature.slots.presentation.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_preview_reset_screen
import com.layzen.bdotools.feature.slots.resources.slots_preview_resolution
import com.layzen.bdotools.feature.slots.resources.slots_preview_resolution_value
import com.layzen.bdotools.feature.slots.resources.slots_preview_scale
import com.layzen.bdotools.feature.slots.resources.slots_preview_scale_value
import com.layzen.bdotools.feature.slots.resources.slots_preview_screen
import com.layzen.bdotools.feature.slots.resources.slots_preview_screen_description
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** Распространённые мониторы для просмотра. */
private val RESOLUTIONS = listOf(
    1280 to 720, 1366 to 768, 1600 to 900, 1920 to 1080, 1920 to 1200, 2560 to 1080,
    2560 to 1440, 3440 to 1440, 3840 to 1600, 3840 to 2160,
)

/** Масштаб интерфейса в процентах, как у ползунка в игре. */
private val SCALES = (50..200 step 5).toList()

/**
 * Монитор, на котором показывается превью: разрешение и масштаб интерфейса. Только для просмотра;
 * «Мой экран» возвращает экран из файла игры [gameScreen].
 */
@Composable
internal fun PreviewScreenSettings(screen: ScreenSpace, gameScreen: ScreenSpace, onEvent: (SlotsEvent) -> Unit) {
    val resolution = screen.resolutionWidth to screen.resolutionHeight
    val resolutions = (RESOLUTIONS + (gameScreen.resolutionWidth to gameScreen.resolutionHeight)).distinct().sortedBy { it.first * it.second }
    val scale = (screen.uiScale * 100).roundToInt()
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SectionHeader(stringResource(Res.string.slots_preview_screen), description = stringResource(Res.string.slots_preview_screen_description))
        DescriptionText(stringResource(Res.string.slots_preview_resolution))
        DropdownSelect(
            selected = resolution,
            options = resolutions,
            onSelect = { (width, height) -> onEvent(SlotsEvent.PreviewScreen(screen.copy(resolutionWidth = width, resolutionHeight = height))) },
            label = { stringResource(Res.string.slots_preview_resolution_value, it.first, it.second) },
        )
        DescriptionText(stringResource(Res.string.slots_preview_scale))
        DropdownSelect(
            selected = scale,
            options = (SCALES + scale).distinct().sorted(),
            onSelect = { onEvent(SlotsEvent.PreviewScreen(screen.copy(uiScale = it / 100.0))) },
            label = { stringResource(Res.string.slots_preview_scale_value, it) },
        )
        ActionButton(
            stringResource(Res.string.slots_preview_reset_screen),
            onClick = { onEvent(SlotsEvent.PreviewScreen(gameScreen)) },
            enabled = screen != gameScreen,
        )
    }
}
