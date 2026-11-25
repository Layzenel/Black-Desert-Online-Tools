package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.composable.skill.SkillMarkupText
import com.layzen.bdotools.composable.skill.rememberKeyLabel
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.keys.keyVariantLine
import com.layzen.bdotools.feature.comboassist.domain.keys.keyVariants
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_any
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_hint
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_record
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_record_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_remove
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_rerecord
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_title
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.menu_keyboard
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.CloseButton
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.OptionButton
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Какой командой игрок применяет умение [skillNo] в выбранной ячейке: любой вариант из описания, один из них или
 * свои клавиши, записанные нажатием ([KeyRecorder]); свои клавиши убирает крестик в конце их кнопки, запись поверх —
 * «Перезаписать». Клавиши — подписью или картинкой, как выбрано в настройках. [chosen] — команда ячейки или `null`
 * (любая); выбор — [ComboEvent.SetKeyVariant].
 */
@Composable
internal fun ActivationPicker(skillNo: Int, chosen: Set<String>?, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val language = stringResource(Res.string.combo_names_language)
    val details by produceState<SkillDetails?>(null, skillNo) { value = loadDetails(skillNo) }
    val variants = remember(details, language) { details?.text(language)?.command?.let(::keyVariants).orEmpty() }
    val label = rememberKeyLabel(KeyBindings.Default)
    var recording by remember(skillNo) { mutableStateOf(false) }
    HoverTooltip(stringResource(Res.string.combo_activation_hint)) { SectionHeader(stringResource(Res.string.combo_activation_title)) }
    Column(Modifier.width(ComboDimens.activationWidth), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        OptionButton(stringResource(Res.string.combo_activation_any), chosen == null, { onEvent(ComboEvent.SetKeyVariant(null)) }, Modifier.fillMaxWidth())
        variants.forEach { variant -> KeysOption(variant, chosen == variant, label) { onEvent(ComboEvent.SetKeyVariant(variant)) } }
        // свои клавиши, которых нет среди вариантов описания: с крестиком в конце, а запись — «Перезаписать»
        val custom = chosen?.takeIf { it !in variants }
        if (custom != null) {
            Box {
                KeysOption(custom, selected = true, label) {}
                HoverTooltip(stringResource(Res.string.combo_activation_remove), Modifier.align(Alignment.CenterEnd).padding(end = AppTheme.dimens.spaceS)) {
                    CloseButton({ onEvent(ComboEvent.SetKeyVariant(null)) })
                }
            }
        }
        ActionButton(
            ActionButtonState.Neutral(
                stringResource(if (custom != null) Res.string.combo_activation_rerecord else Res.string.combo_activation_record),
                icon = painterResource(Res.drawable.menu_keyboard),
                onClick = { recording = true },
                tooltip = stringResource(Res.string.combo_activation_record_tooltip),
            ),
            Modifier.fillMaxWidth(),
        )
    }
    if (recording) {
        KeyRecorder { actions ->
            recording = false
            if (actions.isNotEmpty()) onEvent(ComboEvent.SetKeyVariant(actions))
        }
    }
}

/** Вариант команды [keys]: клавиши подписью или картинкой; [label] — подпись для экранного диктора. */
@Composable
private fun KeysOption(keys: Set<String>, selected: Boolean, label: (String) -> String, onClick: () -> Unit) {
    OptionButton(keys.joinToString(" + ") { label(it) }, selected, onClick, Modifier.fillMaxWidth()) { color ->
        SkillMarkupText(keyVariantLine(keys), KeyBindings.Default, AppTheme.typography.bodyBold, color, maxLines = 1)
    }
}
