package com.layzen.bdotools.feature.uieditor.presentation.ui.preview

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_memos
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_memos_description
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_skills
import com.layzen.bdotools.feature.uieditor.resources.slots_preview_skills_description
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.ToggleSwitch
import org.jetbrains.compose.resources.stringResource

/**
 * «Умения в слотах» и, пока оно включено, «Заметки под слотами» в превью импорта и экспорта: переносить ли
 * навыки слотов перезарядки (`skill` в файле) и их надписи (`memo`). Видны, только если в раскладке есть навыки.
 */
@Composable
internal fun PreviewSkillSettings(dialog: SlotsDialog.Preview, onEvent: (SlotsEvent) -> Unit) {
    if (dialog.layout.layout.skills == null) return
    HoverTooltip(stringResource(Res.string.slots_preview_skills_description)) {
        ToggleSwitch(
            checked = dialog.includeSkills,
            onCheckedChange = { onEvent(SlotsEvent.PreviewSkills(it)) },
            label = stringResource(Res.string.slots_preview_skills),
        )
    }
    if (dialog.includeSkills && dialog.layout.layout.memos != null) {
        HoverTooltip(stringResource(Res.string.slots_preview_memos_description)) {
            ToggleSwitch(
                checked = dialog.includeMemos,
                onCheckedChange = { onEvent(SlotsEvent.PreviewMemos(it)) },
                label = stringResource(Res.string.slots_preview_memos),
            )
        }
    }
}
