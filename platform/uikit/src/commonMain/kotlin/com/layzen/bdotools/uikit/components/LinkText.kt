package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Ссылка на внешний адрес: подчёркнутый бежевый текст, при наведении светлеет.
 * Открывает [url] в браузере системы (в вебе — в новой вкладке) через [LocalUriHandler].
 */
@Composable
fun LinkText(text: String, url: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Text(
        text = text,
        style = AppTheme.typography.bodyBold,
        color = if (hovered) AppTheme.colors.textPrimary else AppTheme.colors.textBeige,
        textDecoration = TextDecoration.Underline,
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interaction, indication = null, role = Role.Button) { uriHandler.openUri(url) },
    )
}
