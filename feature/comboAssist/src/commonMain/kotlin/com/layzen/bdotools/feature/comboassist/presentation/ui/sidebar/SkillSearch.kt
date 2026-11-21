package com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.composable.skill.rememberKeyLabel
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_key_aliases
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import org.jetbrains.compose.resources.stringResource

/**
 * Поиск умения по строке [query]: по названию или по клавишам активации («shift лкм», «W+C»). Клавиши — те же
 * подписи, что в тултипе, и их другие написания языка интерфейса ([keyAliases]: «шифт», «lmb», «ку»); каждое слово
 * запроса должно быть началом одной из клавиш умения. Пустой запрос — все.
 */
@Composable
internal fun rememberSkillSearch(query: String): (SkillItem) -> Boolean {
    val language = stringResource(Res.string.combo_names_language)
    val keyLabel = rememberKeyLabel(KeyBindings.Default)
    val aliases = keyAliases(stringResource(Res.string.combo_key_aliases))
    val text = query.trim()
    val words = text.lowercase().split(' ', '+').filter { it.isNotBlank() }
    return { item ->
        val keys = item.traits?.keyActions.orEmpty().flatMap { action ->
            val label = keyLabel(action).lowercase()
            listOf(label) + aliases[label].orEmpty()
        }
        text.isEmpty() || item.skill.name(language).contains(text, ignoreCase = true) ||
            (keys.isNotEmpty() && words.all { word -> keys.any { it.startsWith(word) } })
    }
}

/** Другие написания подписей клавиш из строки вида «лкм=lmb;shift=шифт» (подпись в нижнем регистре → написания). */
internal fun keyAliases(spec: String): Map<String, List<String>> =
    spec.split(';').mapNotNull { entry ->
        val (label, names) = entry.split('=').takeIf { it.size == 2 } ?: return@mapNotNull null
        label.trim().lowercase() to names.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }
    }.toMap()
