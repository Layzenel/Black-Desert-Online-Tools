package com.layzen.bdotools.update

import androidx.compose.runtime.Composable

/**
 * Плашка «доступна новая версия» под верхней навигацией. Есть только в настольной версии: сайт и так
 * всегда свежий, поэтому в браузере функция ничего не показывает.
 */
@Composable
internal expect fun AppUpdateBanner()
