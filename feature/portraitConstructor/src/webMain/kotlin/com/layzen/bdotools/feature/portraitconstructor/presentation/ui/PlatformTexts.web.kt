@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.feature.portraitconstructor.presentation.ui

import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_error_permission
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_folder_description
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_unsupported_brave
import org.jetbrains.compose.resources.StringResource

// Brave прячет File System Access API за флагом и отличается от Chrome только объектом navigator.brave
private fun isBraveBrowser(): Boolean =
    js("typeof navigator !== 'undefined' && typeof navigator.brave !== 'undefined'")

internal actual val folderDescriptionText: StringResource = Res.string.portraits_folder_description

internal actual val permissionErrorText: StringResource = Res.string.portraits_error_permission

internal actual fun unsupportedBrowserHint(): StringResource? =
    if (isBraveBrowser()) Res.string.portraits_unsupported_brave else null
