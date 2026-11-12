package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.platform.LocalWindowInfo

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun rememberAltPressed(): Boolean = LocalWindowInfo.current.keyboardModifiers.isAltPressed
