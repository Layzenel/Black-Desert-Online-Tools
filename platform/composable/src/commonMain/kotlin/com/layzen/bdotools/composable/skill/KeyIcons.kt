package com.layzen.bdotools.composable.skill

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import com.layzen.bdotools.composable.resources.Res
import com.layzen.bdotools.composable.resources.key_down
import com.layzen.bdotools.composable.resources.key_left
import com.layzen.bdotools.composable.resources.key_lmb
import com.layzen.bdotools.composable.resources.key_move
import com.layzen.bdotools.composable.resources.key_right
import com.layzen.bdotools.composable.resources.key_rmb
import com.layzen.bdotools.composable.resources.key_shift
import com.layzen.bdotools.composable.resources.key_up
import com.layzen.bdotools.core.keyhint.AppKeyHints
import com.layzen.bdotools.core.keyhint.KEY_CODE_A
import com.layzen.bdotools.core.keyhint.KEY_CODE_D
import com.layzen.bdotools.core.keyhint.KEY_CODE_S
import com.layzen.bdotools.core.keyhint.KEY_CODE_W
import com.layzen.bdotools.core.keyhint.KEY_CODE_LMB
import com.layzen.bdotools.core.keyhint.KEY_CODE_RMB
import com.layzen.bdotools.core.keyhint.KEY_CODE_SHIFT
import com.layzen.bdotools.core.keyhint.KeyHintStyle
import com.layzen.bdotools.core.keyhint.KeyHints
import com.layzen.bdotools.core.skill.domain.KeyBindings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject


/** Выбранный в настройках вид подсказок клавиш; меняется на лету. */
@Composable
internal fun rememberKeyHints(): KeyHints {
    val hints by koinInject<AppKeyHints>().selected.collectAsState()
    return hints
}

/** Картинка клавиши с кодом [keyCode] (Shift, ЛКМ, ПКМ, W/A/S/D — стрелками) или `null`, если у неё картинки нет. */
fun keyIconOf(keyCode: Int): DrawableResource? = when (keyCode) {
    KEY_CODE_SHIFT -> Res.drawable.key_shift
    KEY_CODE_LMB -> Res.drawable.key_lmb
    KEY_CODE_RMB -> Res.drawable.key_rmb
    KEY_CODE_W -> Res.drawable.key_up
    KEY_CODE_A -> Res.drawable.key_left
    KEY_CODE_S -> Res.drawable.key_down
    KEY_CODE_D -> Res.drawable.key_right
    else -> null
}

/** Все четыре стрелки в ряд — картинка варианта «стрелками» для клавиш движения в настройках. */
val keyMoveIcon: DrawableResource get() = Res.drawable.key_move

/**
 * Картинка клавиши действия по имени из `{KeyBind:…}` — только если в настройках для неё выбрана [KeyHintStyle.Icon];
 * иначе `null`, и клавишу показывают текстом ([rememberKeyLabel]).
 */
@Composable
fun rememberKeyIcon(keys: KeyBindings): (action: String) -> DrawableResource? {
    val hints = rememberKeyHints()
    return { action -> keys[action]?.takeIf { hints.styleOf(it) == KeyHintStyle.Icon }?.let(::keyIconOf) }
}

/**
 * Картинка клавиши [icon] высотой во всю строку текста [style] (`lineHeight`), а не в размер шрифта: так клавиша
 * с картинкой такого же размера, как с буквой. Цвет [color]: белый контур красится через `ColorFilter.tint`.
 */
@Composable
fun KeyIcon(icon: DrawableResource, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    val size = with(LocalDensity.current) { (if (style.lineHeight.isSp) style.lineHeight else style.fontSize).toDp() }
    Image(painterResource(icon), null, modifier.height(size), colorFilter = ColorFilter.tint(color))
}
