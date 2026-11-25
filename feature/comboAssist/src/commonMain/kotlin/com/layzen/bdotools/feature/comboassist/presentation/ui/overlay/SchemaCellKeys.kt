package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.keys.keyVariantLine
import com.layzen.bdotools.feature.comboassist.domain.skill.commandLineFor
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import org.jetbrains.compose.resources.stringResource

/**
 * Клавиши применения умения [skillNo] в ячейке [at] схемы: выбранная в редакторе команда шага, иначе команда
 * из описания умения. Пока описание не загрузилось, ничего не рисует.
 */
@Composable
internal fun SchemaCellKeys(at: CellAt, skillNo: Int, overlay: OverlayState, loadDetails: suspend (Int) -> SkillDetails?, modifier: Modifier = Modifier) {
    val chosen = overlay.cellKeys[at]
    val language = stringResource(Res.string.combo_names_language)
    val details by produceState<SkillDetails?>(null, skillNo, chosen) { if (chosen == null) value = loadDetails(skillNo) }
    val line = chosen?.let { keyVariantLine(it) } ?: details?.text(language)?.command?.let { commandLineFor(it, null) }.orEmpty()
    if (line.isNotEmpty()) KeyCaps(line, modifier)
}
