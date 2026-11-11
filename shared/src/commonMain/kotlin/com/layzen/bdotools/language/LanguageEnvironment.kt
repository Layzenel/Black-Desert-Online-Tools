package com.layzen.bdotools.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.layzen.bdotools.core.language.AppLanguage

/**
 * Показывает [content] на языке [language]: строки из `strings.xml` берутся из папки `values-<tag>` этого языка.
 *
 * Ресурсы Compose читают язык платформы и не следят за его сменой, поэтому при смене языка [content]
 * собирается заново и его `remember`-состояние сбрасывается. Состояние, которое должно пережить смену языка
 * (стек навигации), держи снаружи; ViewModel она не затрагивает.
 */
@Composable
internal fun LanguageEnvironment(language: AppLanguage, content: @Composable () -> Unit) {
    remember(language) { applyPlatformLocale(language.tag) }
    key(language) { content() }
}

/** Делает язык с кодом [tag] языком платформы, по которому Compose выбирает папку ресурсов. */
internal expect fun applyPlatformLocale(tag: String)
