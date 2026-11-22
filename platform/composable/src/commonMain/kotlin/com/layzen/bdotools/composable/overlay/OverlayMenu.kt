package com.layzen.bdotools.composable.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.composable.resources.Res
import com.layzen.bdotools.composable.resources.overlay_app_icon
import com.layzen.bdotools.composable.resources.overlay_app_icon_admin
import com.layzen.bdotools.composable.resources.overlay_game_admin
import com.layzen.bdotools.uikit.components.onSecondaryClick
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Прозрачность значка, пока оверлей выключен ([OverlayActive]). */
private const val INACTIVE_ALPHA = 0.4f

/**
 * Кнопка меню оверлея — значок программы: клик открывает и закрывает меню ([OverlayMenuPanel]), [dragModifier] —
 * перетаскивание окна кнопки зажатой ЛКМ. ПКМ включает и выключает оверлей ([OverlayActive]): выключенный — бледный
 * значок. [gameElevated] — игра запущена от имени администратора: в значке щиток вместо ромба.
 */
@Composable
internal fun OverlayMenuButton(open: Boolean, onOpenChange: (Boolean) -> Unit, dragModifier: Modifier, gameElevated: Boolean) {
    Image(
        painterResource(if (gameElevated) Res.drawable.overlay_app_icon_admin else Res.drawable.overlay_app_icon),
        if (gameElevated) stringResource(Res.string.overlay_game_admin) else null,
        Modifier.size(OverlayDimens.menuButton).pointerHoverIcon(PointerIcon.Hand)
            .alpha(if (OverlayActive.value) 1f else INACTIVE_ALPHA)
            // ПКМ включает и выключает оверлей; раньше перетаскивания и клика, чтобы они его не получили
            .onSecondaryClick { OverlayActive.value = !OverlayActive.value }
            // перетаскивание раньше клика: сдвиг с зажатой ЛКМ двигает окно, а клик без сдвига открывает меню
            .then(dragModifier)
            // без индикации: иначе после нажатия на квадрате значка остаётся серая подложка фокуса
            .clickable(remember { MutableInteractionSource() }, indication = null) { onOpenChange(!open) },
    )
}
