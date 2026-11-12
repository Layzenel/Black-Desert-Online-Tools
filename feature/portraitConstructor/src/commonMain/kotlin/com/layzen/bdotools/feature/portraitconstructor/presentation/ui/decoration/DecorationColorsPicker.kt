package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.DecorationPalette
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.DecorationStyle
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitDecorator
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_badge_color
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_caption_color
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_color_default
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_shade_color
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_shade_size
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_shade_strength
import com.layzen.bdotools.uikit.components.ColorPalette
import com.layzen.bdotools.uikit.components.ValueSlider
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Стиль украшения: цвет иконки (значка), цвет заметки и цвет затенения под ними из [DecorationPalette] — первый
 * квадрат каждой палитры цвет по умолчанию (`null`), — , сила и область затенения, %. Нужен и окну одного портрета,
 * и окну всех портретов; каждое изменение отдаёт новый стиль в [onChange].
 */
@Composable
internal fun DecorationColorsPicker(style: DecorationStyle, decorator: PortraitDecorator, onChange: (DecorationStyle) -> Unit) {
    val default = decorator.decorationColor(null)
    ColorRow(stringResource(Res.string.portraits_badge_color), style.badgeColor, default, decorator) { onChange(style.copy(badgeColor = it)) }
    ColorRow(stringResource(Res.string.portraits_caption_color), style.captionColor, default, decorator) { onChange(style.copy(captionColor = it)) }
    ColorRow(stringResource(Res.string.portraits_shade_color), style.shadeColor, decorator.shadeColor(DecorationStyle()), decorator) {
        onChange(style.copy(shadeColor = it))
    }
    // значение по умолчанию — `null`: в файл украшения попадает, только если его меняли
    PercentSlider(stringResource(Res.string.portraits_shade_strength), style.shadeStrength, DecorationStyle.DEFAULT_SHADE_STRENGTH, DecorationStyle.SHADE_RANGE) {
        onChange(style.copy(shadeStrength = it))
    }
    PercentSlider(stringResource(Res.string.portraits_shade_size), style.shadeSize, DecorationStyle.DEFAULT_SHADE_SIZE, DecorationStyle.SHADE_SIZE_RANGE) {
        onChange(style.copy(shadeSize = it))
    }
}

/** Слайдер процентов с заголовком; значение [default] отдаётся как `null` — «по умолчанию». */
@Composable
private fun PercentSlider(title: String, value: Int?, default: Int, range: IntRange, onChange: (Int?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        Text(title, style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        ValueSlider(value = value ?: default, onValueChange = { onChange(it.takeIf { percent -> percent != default }) }, range = range)
    }
}

@Composable
private fun ColorRow(title: String, selected: Int?, defaultColor: Color, decorator: PortraitDecorator, onSelect: (Int?) -> Unit) {
    val palette = DecorationPalette.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        Text(title, style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        ColorPalette(
            colors = palette.map { decorator.decorationColor(it) },
            selected = selected?.let { palette.indexOf(it).takeIf { index -> index >= 0 } },
            onSelect = { index -> onSelect(index?.let(palette::get)) },
            defaultColor = defaultColor,
            defaultLabel = stringResource(Res.string.portraits_color_default),
        )
    }
}
