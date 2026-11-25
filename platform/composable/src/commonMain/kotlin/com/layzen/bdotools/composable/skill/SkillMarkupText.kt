package com.layzen.bdotools.composable.skill

import com.layzen.bdotools.core.skill.domain.keyName
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
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
 * Текст тултипа навыка [markup] с цветами сайта данных; `{KeyBind:…}` — клавиша из [keys], как выбрано в настройках
 * (Отображение): подписью или картинкой ([KeyIcon], цвета [color]). Действие без клавиши остаётся своим именем.
 */
@Composable
fun SkillMarkupText(
    markup: String,
    keys: KeyBindings,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    val label = rememberPartLabel(keys)
    val icon = rememberKeyIcon(keys)
    val icons = mutableMapOf<String, DrawableResource>()
    val text = buildAnnotatedString {
        parseSkillMarkup(markup).forEach { part ->
            val keyIcon = (part as? SkillTextPart.Key)?.let { icon(it.action) }
            val partColor = part.color
            when {
                part is SkillTextPart.Key && keyIcon != null -> {
                    val id = KEY_ICON_ID + part.action
                    icons[id] = keyIcon
                    appendInlineContent(id, label(part))
                }
                partColor == null -> append(label(part))
                else -> withStyle(SpanStyle(color = Color(0xFF000000 or partColor.toLong()))) { append(label(part)) }
            }
        }
    }
    // картинка клавиши высотой в строку текста, как в KeyIcon, ширина — по её пропорциям (Shift — широкая клавиша)
    val size = if (style.lineHeight.isSp) style.lineHeight else style.fontSize
    val inline = icons.mapValues { (_, keyIcon) ->
        val intrinsic = painterResource(keyIcon).intrinsicSize
        val aspect = if (intrinsic.height > 0f) intrinsic.width / intrinsic.height else 1f
        InlineTextContent(Placeholder(size * aspect, size, PlaceholderVerticalAlign.TextCenter)) { KeyIcon(keyIcon, style, color) }
    }
    Text(text, modifier, style = style, color = color, maxLines = maxLines, overflow = TextOverflow.Ellipsis, inlineContent = inline)
}

/** Начало id картинки клавиши во встроенном содержимом текста. */
private const val KEY_ICON_ID = "key:"

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
