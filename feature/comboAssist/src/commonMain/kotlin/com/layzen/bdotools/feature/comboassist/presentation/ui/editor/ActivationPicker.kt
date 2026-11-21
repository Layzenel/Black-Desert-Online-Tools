package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.layzen.bdotools.composable.skill.rememberKeyLabel
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.keys.keyVariants
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_any
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_hint
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_record
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_remove
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_rerecord
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.CrossIcon
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_title
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.OptionButton
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Какой командой игрок применяет умение [skillNo] в выбранной ячейке: любой вариант из описания, один из них или
 * свои клавиши, записанные нажатием ([KeyRecorder]); свои клавиши убирает крестик, запись поверх — «Перезаписать».
 * [chosen] — команда ячейки или `null` (любая); выбор — [ComboEvent.SetKeyVariant].
 */
@Composable
internal fun ActivationPicker(skillNo: Int, chosen: Set<String>?, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val language = stringResource(Res.string.combo_names_language)
    val details by produceState<SkillDetails?>(null, skillNo) { value = loadDetails(skillNo) }
    val variants = remember(details, language) { details?.text(language)?.command?.let(::keyVariants).orEmpty() }
    val label = rememberKeyLabel(KeyBindings.Default)
    fun keys(actions: Set<String>) = actions.joinToString(" + ") { label(it) }
    var recording by remember(skillNo) { mutableStateOf(false) }
    HoverTooltip(stringResource(Res.string.combo_activation_hint)) { SectionHeader(stringResource(Res.string.combo_activation_title)) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        OptionButton(stringResource(Res.string.combo_activation_any), chosen == null, { onEvent(ComboEvent.SetKeyVariant(null)) })
        variants.forEach { variant -> OptionButton(keys(variant), chosen == variant, { onEvent(ComboEvent.SetKeyVariant(variant)) }) }
        // свои клавиши, которых нет среди вариантов описания: с крестиком, а запись — «Перезаписать»
        val custom = chosen?.takeIf { it !in variants }
        if (custom != null) {
            OptionButton(keys(custom), selected = true, onClick = {})
            SmallButton(stringResource(Res.string.combo_activation_remove), { onEvent(ComboEvent.SetKeyVariant(null)) }) { CrossIcon() }
        }
        if (recording) {
            KeyRecorder(label) { actions ->
                recording = false
                if (actions.isNotEmpty()) onEvent(ComboEvent.SetKeyVariant(actions))
            }
        } else {
            ActionButton(stringResource(if (custom != null) Res.string.combo_activation_rerecord else Res.string.combo_activation_record), { recording = true })
        }
    }
}
