package com.layzen.bdotools.feature.uieditor.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.intro_ui_editor
import com.layzen.bdotools.feature.uieditor.resources.slots_intro_image
import com.layzen.bdotools.feature.uieditor.resources.slots_intro_layout
import com.layzen.bdotools.feature.uieditor.resources.slots_intro_safety
import com.layzen.bdotools.feature.uieditor.resources.slots_intro_share
import com.layzen.bdotools.feature.uieditor.resources.slots_intro_skills
import com.layzen.bdotools.uikit.components.ToolIntro
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Что умеет редактор — под просьбой выбрать папку, чтобы было понятно, зачем давать доступ к файлам. */
@Composable
internal fun SlotsIntro() {
    ToolIntro(
        points = listOf(
            stringResource(Res.string.slots_intro_layout),
            stringResource(Res.string.slots_intro_skills),
            stringResource(Res.string.slots_intro_share),
            stringResource(Res.string.slots_intro_safety),
        ),
        image = painterResource(Res.drawable.intro_ui_editor),
        imageDescription = stringResource(Res.string.slots_intro_image),
    )
}
