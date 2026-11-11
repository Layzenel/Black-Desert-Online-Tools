package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_empty
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_link
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_memo
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_names_language
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_number
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_unknown
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.LinkText
import com.layzen.bdotools.uikit.components.ValueText
import org.jetbrains.compose.resources.stringResource

/**
 * Навык в слоте перезарядки [id] раскладки [layout]: название на языке интерфейса, `SkillNo`, надпись и ссылка
 * на навык на bdolytics. Навыка нет в справочнике — только номер.
 */
@Composable
internal fun SkillDetails(id: SlotId, layout: SlotLayout, skills: SkillsState) {
    val skillNo = layout.skillAt(id)
    if (skillNo == null) {
        DescriptionText(stringResource(Res.string.slots_skill_empty))
        return
    }
    val language = stringResource(Res.string.slots_skill_names_language)
    ValueText(skills.skill(skillNo)?.name(language) ?: stringResource(Res.string.slots_skill_unknown))
    DescriptionText(stringResource(Res.string.slots_skill_number, skillNo))
    layout.memoAt(id)?.let { DescriptionText(stringResource(Res.string.slots_skill_memo, it)) }
    LinkText(stringResource(Res.string.slots_skill_link), "https://bdolytics.com/$language/db/skill/$skillNo")
}
