package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Иконка навыка в рамке слота (с отступом, как в игре) и поверх неё номер слота [number] на затенённой подложке, чтобы номер
 * читался на любой иконке. Так слот выглядит и на холсте редактора, и в превью импорта и экспорта.
 */
@Composable
internal fun BoxScope.SkillSlotContent(icon: ImageBitmap, number: String?, style: TextStyle) {
    // Как в игре: иконка меньше рамки слота и стоит по центру с ровным отступом
    Image(
        icon,
        contentDescription = null,
        modifier = Modifier.align(Alignment.Center).fillMaxSize(SlotsDimens.SKILL_ICON_SHARE),
        contentScale = ContentScale.Crop,
    )
    if (number == null) return
    Box(
        Modifier
            .align(Alignment.Center)
            .background(AppTheme.colors.backgroundDeep.copy(alpha = SlotsDimens.SKILL_NUMBER_BACKDROP_ALPHA))
            .padding(horizontal = SlotsDimens.skillNumberPadding),
    ) {
        Text(number, style = style, color = AppTheme.colors.textPrimary, maxLines = 1)
    }
}
