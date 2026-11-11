package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.layzen.bdotools.feature.uieditor.domain.skill.KeyBindings
import com.layzen.bdotools.feature.uieditor.domain.skill.SkillTextPart
import com.layzen.bdotools.feature.uieditor.domain.skill.parseSkillMarkup
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_key_lmb
import com.layzen.bdotools.feature.uieditor.resources.slots_key_mmb
import com.layzen.bdotools.feature.uieditor.resources.slots_key_rmb
import com.layzen.bdotools.feature.uieditor.resources.slots_key_space
import org.jetbrains.compose.resources.stringResource

/**
 * Текст тултипа навыка [markup] с цветами сайта данных; `{KeyBind:…}` заменяется подписью клавиши из [keys].
 * Действие без клавиши остаётся своим именем.
 */
@Composable
internal fun skillMarkupText(markup: String, keys: KeyBindings): AnnotatedString {
    val translated = mapOf(
        1 to stringResource(Res.string.slots_key_lmb),
        2 to stringResource(Res.string.slots_key_rmb),
        4 to stringResource(Res.string.slots_key_mmb),
        32 to stringResource(Res.string.slots_key_space),
    )
    return buildAnnotatedString {
        parseSkillMarkup(markup).forEach { part ->
            val text = when (part) {
                is SkillTextPart.Plain -> part.text
                is SkillTextPart.Key -> keys[part.action]?.let { translated[it] ?: keyName(it) } ?: part.action
            }
            val color = part.color
            if (color == null) append(text) else withStyle(SpanStyle(color = Color(0xFF000000 or color.toLong()))) { append(text) }
        }
    }
}

/** Подпись клавиши по виртуальному коду Windows; такие клавиши одинаковы на всех языках. */
private fun keyName(keyCode: Int): String = when (keyCode) {
    in 'A'.code..'Z'.code, in '0'.code..'9'.code -> keyCode.toChar().toString()
    in F1..F1 + 11 -> "F${keyCode - F1 + 1}"
    in NUMPAD_0..NUMPAD_0 + 9 -> "Num ${keyCode - NUMPAD_0}"
    else -> NAMED_KEYS[keyCode] ?: "#$keyCode"
}

private const val F1 = 112
private const val NUMPAD_0 = 96
private val NAMED_KEYS = mapOf(
    8 to "Backspace", 9 to "Tab", 13 to "Enter", 16 to "Shift", 17 to "Ctrl", 18 to "Alt", 20 to "Caps Lock",
    27 to "Esc", 37 to "Left", 38 to "Up", 39 to "Right", 40 to "Down", 192 to "~",
)
