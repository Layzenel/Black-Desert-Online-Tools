package com.layzen.bdotools.feature.portraits.presentation.ui

import org.jetbrains.compose.resources.StringResource

// Тексты, которые различаются на сайте и в настольной версии: в браузере файлы читает сайт, на Desktop — программа

/** Пояснение к выбору папки игры. */
internal expect val folderDescriptionText: StringResource

/** Сообщение, когда нет доступа к файлам в папке игры. */
internal expect val permissionErrorText: StringResource

/** Подсказка для конкретного браузера на экране «браузер не поддерживается» (например, флаг в Brave) или `null`. */
internal expect fun unsupportedBrowserHint(): StringResource?
