package com.layzen.bdotools

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.admin_badge_hint
import com.layzen.bdotools.resources.admin_shield
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Золотой щит перед названием программы: она запущена от имени администратора; подробнее — подсказкой. */
@Composable
internal fun AdminBadge() {
    val hint = stringResource(Res.string.admin_badge_hint)
    HoverTooltip(hint) {
        Image(painterResource(Res.drawable.admin_shield), hint, Modifier.size(AppTheme.dimens.iconSize), colorFilter = ColorFilter.tint(AppTheme.colors.accentGold))
    }
}
