package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import org.jetbrains.compose.resources.stringResource

/** Название умения на языке интерфейса или его номер, если умения нет в справочнике. */
@Composable
internal fun skillName(skills: Map<Int, SkillItem>, skillNo: Int?): String =
    skillNo?.let { skills[it]?.skill?.name(stringResource(Res.string.combo_names_language)) ?: "#$it" }.orEmpty()
