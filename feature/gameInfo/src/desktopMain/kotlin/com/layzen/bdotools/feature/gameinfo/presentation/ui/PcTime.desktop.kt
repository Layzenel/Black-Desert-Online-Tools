package com.layzen.bdotools.feature.gameinfo.presentation.ui

import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val FORMAT = DateTimeFormatter.ofPattern("HH:mm")

internal actual fun pcTime(): String = LocalTime.now().format(FORMAT)
