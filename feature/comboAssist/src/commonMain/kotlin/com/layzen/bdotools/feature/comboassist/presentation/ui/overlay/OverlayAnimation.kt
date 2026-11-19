package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith

/** Длительность смены шага в трекере, мс. */
internal const val SWAP_MS = 250

/** Новый шаг выезжает справа на эту долю своей ширины. */
private const val SLIDE_SHARE = 4

/** Смена шага «что жать сейчас»: новый выезжает справа и проявляется, старый гаснет. */
internal fun <S> AnimatedContentTransitionScope<S>.stepSwap(): ContentTransform =
    (slideInHorizontally(tween(SWAP_MS)) { it / SLIDE_SHARE } + fadeIn(tween(SWAP_MS))) togetherWith fadeOut(tween(SWAP_MS))

/** Смена следующего шага: старый гаснет, новый проявляется. */
internal fun <S> AnimatedContentTransitionScope<S>.stepFade(): ContentTransform =
    fadeIn(tween(SWAP_MS)) togetherWith fadeOut(tween(SWAP_MS))
