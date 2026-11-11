package com.layzen.bdotools.feature.uieditor.presentation.state

import com.layzen.bdotools.feature.uieditor.domain.preset.BuiltInLayout

/** Какой пресет сейчас загружен в редактор. После любой правки руками — никакой (`null` в состоянии). */
sealed interface PresetRef {
    /** Раскладка, которая сейчас сохранена в файле игры. */
    data object Game : PresetRef

    data class BuiltIn(val layout: BuiltInLayout) : PresetRef

    data class User(val name: String) : PresetRef
}
