package com.layzen.bdotools.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.placeholder_description
import com.layzen.bdotools.resources.placeholder_title
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Заглушка для вкладки, у которой ещё нет модуля фичи. */
@Composable
fun PlaceholderScreen(title: String) {
    GameWindow(title = title, modifier = Modifier.widthIn(max = AppTheme.dimens.contentMaxWidth)) {
        SectionHeader(
            stringResource(Res.string.placeholder_title),
            modifier = Modifier.padding(AppTheme.dimens.spaceL),
            description = stringResource(Res.string.placeholder_description),
        )
    }
}
