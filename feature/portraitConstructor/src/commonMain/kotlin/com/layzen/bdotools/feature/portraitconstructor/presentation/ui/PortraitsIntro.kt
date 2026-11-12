package com.layzen.bdotools.feature.portraitconstructor.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.intro_portraits
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_intro_image
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_intro_notes
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_intro_pictures
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_intro_safety
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_intro_sets
import com.layzen.bdotools.uikit.components.ToolIntro
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Что умеет конструктор — под просьбой выбрать папку, чтобы было понятно, зачем давать доступ к файлам. */
@Composable
internal fun PortraitsIntro() {
    ToolIntro(
        points = listOf(
            stringResource(Res.string.portraits_intro_pictures),
            stringResource(Res.string.portraits_intro_notes),
            stringResource(Res.string.portraits_intro_sets),
            stringResource(Res.string.portraits_intro_safety),
        ),
        image = painterResource(Res.drawable.intro_portraits),
        imageDescription = stringResource(Res.string.portraits_intro_image),
    )
}
