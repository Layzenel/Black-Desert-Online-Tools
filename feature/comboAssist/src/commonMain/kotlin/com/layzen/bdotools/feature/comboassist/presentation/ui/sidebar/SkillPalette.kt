package com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.layzen.bdotools.uikit.components.verticalScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillHover
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.CooldownBadge
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_catalog_failed
import com.layzen.bdotools.feature.comboassist.resources.combo_loading
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.combo_search
import com.layzen.bdotools.feature.comboassist.resources.combo_section_skills
import com.layzen.bdotools.feature.comboassist.resources.combo_suggest_chain
import com.layzen.bdotools.feature.comboassist.resources.combo_suggest_title
import com.layzen.bdotools.uikit.components.CloseButton
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.LoadingRow
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.WarningText
import org.jetbrains.compose.resources.stringResource

/** Умения класса: сверху — что можно поставить после выбранной ячейки (связки из данных), ниже — поиск и весь список. */
@Composable
internal fun SkillPalette(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val language = stringResource(Res.string.combo_names_language)
    if (state.suggestions.isNotEmpty()) {
        SectionHeader(stringResource(Res.string.combo_suggest_title))
        val chain = stringResource(Res.string.combo_suggest_chain)
        state.suggestions.mapNotNull { state.skills[it] }.forEach { item ->
            SkillRow(item, state, onEvent, loadDetails, hint = if (item.skill.onlyAfter) chain else null)
        }
    }
    SectionHeader(stringResource(Res.string.combo_section_skills))
    when {
        state.loading && state.skills.isEmpty() -> LoadingRow(stringResource(Res.string.combo_loading))
        state.catalogFailed -> WarningText(stringResource(Res.string.combo_catalog_failed))
    }
    InputField(
        state.query, { onEvent(ComboEvent.Query(it)) }, Modifier.fillMaxWidth(), placeholder = stringResource(Res.string.combo_search),
        trailing = { if (state.query.isNotEmpty()) CloseButton({ onEvent(ComboEvent.Query("")) }) },
    )
    // список своей высоты с прокруткой, чтобы под ним были видны филлеры
    val search = rememberSkillSearch(state.query)
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxWidth().heightIn(max = ComboDimens.paletteHeight).verticalScrollbar(scroll).verticalScroll(scroll)) {
        state.skills.values
            // без перезарядки комбо не ждёт — им место в связках, а не в списке
            .filter { it.skill.cooldownMs > 0 || it.skill.onlyAfter }
            .filter(search)
            .sortedBy { it.skill.name(language).lowercase() }
            .forEach { item -> SkillRow(item, state, onEvent, loadDetails, hint = null, cooldownMs = item.skill.cooldownMs) }
    }
}

@Composable
private fun SkillRow(
    item: SkillItem,
    state: ComboState,
    onEvent: (ComboEvent) -> Unit,
    loadDetails: suspend (Int) -> SkillDetails?,
    hint: String?,
    cooldownMs: Long = 0,
) {
    val language = stringResource(Res.string.combo_names_language)
    SkillHover(item, loadDetails, beside = true) {
        MenuItem(
            item.skill.name(language), { onEvent(ComboEvent.Fill(item.skillNo)) }, Modifier.fillMaxWidth(),
            icon = { SkillIconView(item.icon, ComboDimens.paletteIcon) },
            hotkey = hint,
            badge = if (cooldownMs > 0) ({ CooldownBadge(cooldownMs) }) else null,
        )
    }
}
