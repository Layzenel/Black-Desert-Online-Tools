package com.layzen.bdotools.composable.skill

import com.layzen.bdotools.core.skill.domain.keyName
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.layzen.bdotools.composable.resources.skill_key_shift_short
import com.layzen.bdotools.composable.resources.skill_key_lmb_short
import com.layzen.bdotools.composable.resources.skill_key_rmb_short
import com.layzen.bdotools.core.keyhint.KEY_CODE_LMB
import com.layzen.bdotools.core.keyhint.KEY_CODE_RMB
import com.layzen.bdotools.core.keyhint.KEY_CODE_SHIFT
import com.layzen.bdotools.core.keyhint.KeyHintStyle
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.core.skill.domain.SkillTextPart
import com.layzen.bdotools.core.skill.domain.parseSkillMarkup
import com.layzen.bdotools.composable.resources.Res
import com.layzen.bdotools.composable.resources.skill_key_lmb
import com.layzen.bdotools.composable.resources.skill_key_mmb
import com.layzen.bdotools.composable.resources.skill_key_rmb
import com.layzen.bdotools.composable.resources.skill_key_space
import org.jetbrains.compose.resources.stringResource

/**
 * Текст тултипа навыка [markup] с цветами сайта данных; `{KeyBind:…}` заменяется подписью клавиши из [keys].
 * Действие без клавиши остаётся своим именем.
 */
@Composable
fun skillMarkupText(markup: String, keys: KeyBindings): AnnotatedString {
    val label = rememberPartLabel(keys)
    return buildAnnotatedString {
        parseSkillMarkup(markup).forEach { part ->
            val text = label(part)
            val color = part.color
            if (color == null) append(text) else withStyle(SpanStyle(color = Color(0xFF000000 or color.toLong()))) { append(text) }
        }
    }
}

/** Куски тултипа [parts] одной строкой без цветов: `{KeyBind:…}` — подписью клавиши из [keys], как в тултипе. */
@Composable
fun skillPartsText(parts: List<SkillTextPart>, keys: KeyBindings): String {
    val label = rememberPartLabel(keys)
    return parts.joinToString("") { label(it) }
}

/**
 * Подпись клавиши действия: по имени действия из `{KeyBind:…}` берёт клавишу из [keys]; кнопки мыши и пробел —
 * на языке интерфейса, остальные — [keyName]. Shift при выборе «Сокращение» в настройках — «Sh». Картинку клавиши,
 * если она выбрана, рисует [rememberKeyIcon], а здесь остаётся текст. Действие без клавиши остаётся своим именем.
 */
@Composable
fun rememberKeyLabel(keys: KeyBindings): (action: String) -> String {
    val translated = mapOf(
        1 to stringResource(Res.string.skill_key_lmb),
        2 to stringResource(Res.string.skill_key_rmb),
        4 to stringResource(Res.string.skill_key_mmb),
        32 to stringResource(Res.string.skill_key_space),
    )
    // сокращения клавиш, у которых их можно выбрать в настройках (Отображение)
    val short = mapOf(
        KEY_CODE_SHIFT to stringResource(Res.string.skill_key_shift_short),
        KEY_CODE_LMB to stringResource(Res.string.skill_key_lmb_short),
        KEY_CODE_RMB to stringResource(Res.string.skill_key_rmb_short),
    )
    val hints = rememberKeyHints()
    return { action ->
        keys[action]?.let { code ->
            short[code]?.takeIf { hints.styleOf(code) == KeyHintStyle.Short } ?: translated[code] ?: keyName(code)
        } ?: action
    }
}

/** Текст куска: обычный — как есть, клавиша — её подпись. */
@Composable
private fun rememberPartLabel(keys: KeyBindings): (SkillTextPart) -> String {
    val keyLabel = rememberKeyLabel(keys)
    return { part ->
        when (part) {
            is SkillTextPart.Plain -> part.text
            is SkillTextPart.Key -> keyLabel(part.action)
        }
    }
}
