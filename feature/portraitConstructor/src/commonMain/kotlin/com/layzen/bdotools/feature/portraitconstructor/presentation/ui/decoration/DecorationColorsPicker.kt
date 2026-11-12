package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.decoration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.DecorationPalette
import com.layzen.bdotools.feature.portraitconstructor.presentation.render.PortraitDecorator
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_badge_color
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_caption_color
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_color_default
import com.layzen.bdotools.uikit.components.ColorPalette
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Цвет иконки (значка) и цвет заметки из [DecorationPalette]; первый квадрат — цвет по умолчанию (`null`).
 * Нужен и окну одного портрета, и окну цветов всех портретов.
 */
@Composable
internal fun DecorationColorsPicker(
    captionColor: Int?,
    badgeColor: Int?,
    decorator: PortraitDecorator,
    onCaptionColor: (Int?) -> Unit,
    onBadgeColor: (Int?) -> Unit,
) {
    ColorRow(stringResource(Res.string.portraits_badge_color), badgeColor, decorator, onBadgeColor)
    ColorRow(stringResource(Res.string.portraits_caption_color), captionColor, decorator, onCaptionColor)
}

@Composable
private fun ColorRow(title: String, selected: Int?, decorator: PortraitDecorator, onSelect: (Int?) -> Unit) {
    val palette = DecorationPalette.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        Text(title, style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        ColorPalette(
            colors = palette.map { decorator.decorationColor(it) },
            selected = selected?.let { palette.indexOf(it).takeIf { index -> index >= 0 } },
            onSelect = { index -> onSelect(index?.let(palette::get)) },
            defaultColor = decorator.decorationColor(null),
            defaultLabel = stringResource(Res.string.portraits_color_default),
        )
    }
}

