package com.layzen.bdotools.feature.portraits.presentation.ui

import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_error_permission_desktop
import com.layzen.bdotools.feature.portraits.resources.portraits_folder_description_desktop
import org.jetbrains.compose.resources.StringResource

internal actual val folderDescriptionText: StringResource = Res.string.portraits_folder_description_desktop

internal actual val permissionErrorText: StringResource = Res.string.portraits_error_permission_desktop

/** В настольной версии экрана «браузер не поддерживается» не бывает. */
internal actual fun unsupportedBrowserHint(): StringResource? = null
