package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_style_focus
import com.layzen.bdotools.feature.comboassist.resources.combo_overlay_style_next
import org.jetbrains.compose.resources.StringResource

/** Название вида оверлея для переключателя. */
internal val OverlayStyle.title: StringResource
    get() = when (this) {
        OverlayStyle.Next -> Res.string.combo_overlay_style_next
        OverlayStyle.Focus -> Res.string.combo_overlay_style_focus
    }
