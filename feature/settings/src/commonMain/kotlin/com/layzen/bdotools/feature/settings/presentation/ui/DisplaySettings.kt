package com.layzen.bdotools.feature.settings.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.composable.skill.keyIconOf
import com.layzen.bdotools.core.keyhint.KEY_CODE_LMB
import com.layzen.bdotools.core.keyhint.KEY_CODE_RMB
import com.layzen.bdotools.core.keyhint.KEY_CODE_SHIFT
import com.layzen.bdotools.core.keyhint.KeyHintStyle
import com.layzen.bdotools.core.keyhint.KeyHints
import com.layzen.bdotools.feature.settings.resources.Res
import com.layzen.bdotools.feature.settings.resources.settings_display_description
import com.layzen.bdotools.feature.settings.resources.settings_display_lmb
import com.layzen.bdotools.feature.settings.resources.settings_display_rmb
import com.layzen.bdotools.feature.settings.resources.settings_display_shift
import com.layzen.bdotools.feature.settings.resources.settings_display_title
import com.layzen.bdotools.feature.settings.resources.settings_key_style_icon
import com.layzen.bdotools.feature.settings.resources.settings_key_text_lmb
import com.layzen.bdotools.feature.settings.resources.settings_key_text_lmb_short
import com.layzen.bdotools.feature.settings.resources.settings_key_text_rmb_short
import com.layzen.bdotools.feature.settings.resources.settings_key_text_rmb
import com.layzen.bdotools.feature.settings.resources.settings_key_text_shift
import com.layzen.bdotools.feature.settings.resources.settings_key_text_shift_short
import com.layzen.bdotools.uikit.components.OptionGroup
import com.layzen.bdotools.uikit.components.SectionHeader
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Раздел «Отображение»: как показывать Shift, ЛКМ и ПКМ в подсказках умений (трекер, схема, редактор комбо). */
@Composable
internal fun DisplaySettings(hints: KeyHints, onSelect: (KeyHints) -> Unit) {
    SectionHeader(stringResource(Res.string.settings_display_title), description = stringResource(Res.string.settings_display_description))
    KeyHintChoice(
        title = stringResource(Res.string.settings_display_shift),
        keyCode = KEY_CODE_SHIFT,
        options = listOf(KeyHintStyle.Text, KeyHintStyle.Short, KeyHintStyle.Icon),
        selected = hints.shift,
        textLabel = stringResource(Res.string.settings_key_text_shift),
        shortLabel = stringResource(Res.string.settings_key_text_shift_short),
        onSelect = { onSelect(hints.copy(shift = it)) },
    )
    KeyHintChoice(
        title = stringResource(Res.string.settings_display_lmb),
        keyCode = KEY_CODE_LMB,
        options = listOf(KeyHintStyle.Text, KeyHintStyle.Short, KeyHintStyle.Icon),
        selected = hints.lmb,
        textLabel = stringResource(Res.string.settings_key_text_lmb),
        shortLabel = stringResource(Res.string.settings_key_text_lmb_short),
        onSelect = { onSelect(hints.copy(lmb = it)) },
    )
    KeyHintChoice(
        title = stringResource(Res.string.settings_display_rmb),
        keyCode = KEY_CODE_RMB,
        options = listOf(KeyHintStyle.Text, KeyHintStyle.Short, KeyHintStyle.Icon),
        selected = hints.rmb,
        textLabel = stringResource(Res.string.settings_key_text_rmb),
        shortLabel = stringResource(Res.string.settings_key_text_rmb_short),
        onSelect = { onSelect(hints.copy(rmb = it)) },
    )
}

/** Заголовок клавиши и ряд вариантов: слово, сокращение (если есть) и сама картинка клавиши — без подписи. */
@Composable
private fun KeyHintChoice(
    title: String,
    keyCode: Int,
    options: List<KeyHintStyle>,
    selected: KeyHintStyle,
    textLabel: String,
    shortLabel: String,
    onSelect: (KeyHintStyle) -> Unit,
) {
    SectionHeader(title)
    val picture = keyIconOf(keyCode)?.let { painterResource(it) }
    OptionGroup(
        options = options,
        selected = selected,
        onSelect = onSelect,
        label = {
            when (it) {
                KeyHintStyle.Text -> textLabel
                KeyHintStyle.Short -> shortLabel
                KeyHintStyle.Icon -> stringResource(Res.string.settings_key_style_icon)
            }
        },
        icon = { if (it == KeyHintStyle.Icon) picture else null },
    )
}
