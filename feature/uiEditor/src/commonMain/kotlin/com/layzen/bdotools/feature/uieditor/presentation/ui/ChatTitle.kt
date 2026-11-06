package com.layzen.bdotools.feature.uieditor.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_chat
import org.jetbrains.compose.resources.stringResource

/** Название чата [window] — «Чат N», где N — порядковый номер: `WindowIndex` + 1. */
@Composable
internal fun chatTitle(window: Int): String = stringResource(Res.string.slots_chat, window + 1)
