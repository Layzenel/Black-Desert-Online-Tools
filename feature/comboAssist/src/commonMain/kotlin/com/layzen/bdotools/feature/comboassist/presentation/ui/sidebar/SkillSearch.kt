package com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.composable.skill.rememberKeyLabel
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import org.jetbrains.compose.resources.stringResource

/**
 * Поиск умения по строке [query]: по названию или по клавишам активации («shift лкм», «W+C»). Клавиши — те же
 * подписи, что в тултипе; каждое слово запроса должно быть началом одной из клавиш умения. Пустой запрос — все.
 */
@Composable
internal fun rememberSkillSearch(query: String): (SkillItem) -> Boolean {
    val language = stringResource(Res.string.combo_names_language)
    val keyLabel = rememberKeyLabel(KeyBindings.Default)
    val text = query.trim()
    val words = text.lowercase().split(' ', '+').filter { it.isNotBlank() }
    return { item ->
        val keys = item.traits?.keyActions.orEmpty().map { keyLabel(it).lowercase() }
        text.isEmpty() || item.skill.name(language).contains(text, ignoreCase = true) ||
            (keys.isNotEmpty() && words.all { word -> keys.any { it.startsWith(word) } })
    }
}
