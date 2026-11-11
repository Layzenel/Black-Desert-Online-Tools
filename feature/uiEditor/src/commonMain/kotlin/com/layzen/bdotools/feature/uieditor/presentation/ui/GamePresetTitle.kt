package com.layzen.bdotools.feature.uieditor.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.domain.game.GamePreset
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_game_preset_general
import com.layzen.bdotools.feature.uieditor.resources.slots_game_preset_number
import org.jetbrains.compose.resources.stringResource

/** Название набора позиций, как кнопки в окне «Редактирование интерфейса» игры. */
@Composable
internal fun gamePresetTitle(preset: GamePreset): String =
    if (preset == GamePreset.General) stringResource(Res.string.slots_game_preset_general)
    else stringResource(Res.string.slots_game_preset_number, preset.ordinal)
