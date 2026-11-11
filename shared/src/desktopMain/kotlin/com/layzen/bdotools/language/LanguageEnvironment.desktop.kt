package com.layzen.bdotools.language

import java.util.Locale

internal actual fun applyPlatformLocale(tag: String) = Locale.setDefault(Locale.forLanguageTag(tag))
