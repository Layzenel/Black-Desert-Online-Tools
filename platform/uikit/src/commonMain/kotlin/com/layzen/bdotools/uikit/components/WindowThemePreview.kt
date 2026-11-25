package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.theme.WindowTheme
import com.layzen.bdotools.uikit.theme.AppTheme
import com.layzen.bdotools.uikit.theme.LocalWindowTheme

/**
 * Превью темы окон, как в игре при наведении на тему: маленькое окно [title] в теме [theme] — шапка, украшение над ней
 * и крестик — с пустым телом. Место над окном под украшение оставлено внутри превью.
 */
@Composable
fun WindowThemePreview(theme: WindowTheme, title: String, modifier: Modifier = Modifier) {
    CompositionLocalProvider(LocalWindowTheme provides theme) {
        val dimens = AppTheme.dimens
        Box(modifier.padding(top = windowDecorationHeight())) {
            GameWindow(title, Modifier.width(dimens.themePreviewWidth), actions = { WindowCloseButton(onClick = {}) }) {
                Column(Modifier.padding(dimens.spaceM), verticalArrangement = Arrangement.spacedBy(dimens.spaceS)) {
                    Skeleton(Modifier.fillMaxWidth().height(dimens.themePreviewLine))
                    Skeleton(Modifier.fillMaxWidth(PREVIEW_SHORT_LINE).height(dimens.themePreviewLine))
                }
            }
        }
    }
}

/** Длина второй строки-заглушки в превью — доля ширины. */
private const val PREVIEW_SHORT_LINE = 0.6f
