package com.layzen.bdotools.feature.comboassist.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_cancel_selection
import com.layzen.bdotools.feature.comboassist.resources.combo_name_cancel_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_name_placeholder
import com.layzen.bdotools.feature.comboassist.resources.combo_name_save_tooltip
import com.layzen.bdotools.feature.comboassist.resources.combo_name_title
import com.layzen.bdotools.feature.comboassist.resources.combo_new
import com.layzen.bdotools.feature.comboassist.resources.combo_save
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Название комбо перед сохранением: без названия и с названием «Новое комбо» (так зовётся несохранённое комбо в
 * списке) сохранить нельзя. «Сохранить» — [onSave] с названием, «Отмена», крестик и Esc — [onCancel].
 */
@Composable
internal fun ComboNameDialog(initial: String, onCancel: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    val reserved = stringResource(Res.string.combo_new)
    val valid = name.isNotBlank() && !name.trim().equals(reserved, ignoreCase = true)
    ModalWindow(
        title = stringResource(Res.string.combo_name_title),
        onDismissRequest = onCancel,
        modifier = Modifier.width(ComboDimens.confirmWidth),
        footer = {
            ActionButton(ActionButtonState.Neutral(stringResource(Res.string.combo_cancel_selection), onClick = onCancel, tooltip = stringResource(Res.string.combo_name_cancel_tooltip)))
            ActionButton(ActionButtonState.Positive(stringResource(Res.string.combo_save), enabled = valid, onClick = { onSave(name) }, tooltip = stringResource(Res.string.combo_name_save_tooltip)))
        },
    ) {
        InputField(name, { name = it }, Modifier.fillMaxWidth().padding(AppTheme.dimens.spaceL), placeholder = stringResource(Res.string.combo_name_placeholder))
    }
}

/** Комбо с названием [name] сохранять нельзя — нужно окно названия. */
@Composable
internal fun needsName(name: String): Boolean =
    name.isBlank() || name.trim().equals(stringResource(Res.string.combo_new), ignoreCase = true)
