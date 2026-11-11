package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import com.layzen.bdotools.feature.uieditor.domain.arrangement.AlignEdge
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_align_bottom
import com.layzen.bdotools.feature.uieditor.resources.slots_align_center_x
import com.layzen.bdotools.feature.uieditor.resources.slots_align_center_y
import com.layzen.bdotools.feature.uieditor.resources.slots_align_left
import com.layzen.bdotools.feature.uieditor.resources.slots_align_right
import com.layzen.bdotools.feature.uieditor.resources.slots_align_top
import com.layzen.bdotools.feature.uieditor.resources.tool_align_bottom
import com.layzen.bdotools.feature.uieditor.resources.tool_align_center_x
import com.layzen.bdotools.feature.uieditor.resources.tool_align_center_y
import com.layzen.bdotools.feature.uieditor.resources.tool_align_left
import com.layzen.bdotools.feature.uieditor.resources.tool_align_right
import com.layzen.bdotools.feature.uieditor.resources.tool_align_top
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Иконка кнопки выравнивания по этому краю. */
internal fun AlignEdge.icon(): DrawableResource = when (this) {
    AlignEdge.Top -> Res.drawable.tool_align_top
    AlignEdge.Bottom -> Res.drawable.tool_align_bottom
    AlignEdge.Left -> Res.drawable.tool_align_left
    AlignEdge.Right -> Res.drawable.tool_align_right
    AlignEdge.CenterX -> Res.drawable.tool_align_center_x
    AlignEdge.CenterY -> Res.drawable.tool_align_center_y
}

/** Подпись кнопки выравнивания по этому краю. */
internal fun AlignEdge.title(): StringResource = when (this) {
    AlignEdge.Top -> Res.string.slots_align_top
    AlignEdge.Bottom -> Res.string.slots_align_bottom
    AlignEdge.Left -> Res.string.slots_align_left
    AlignEdge.Right -> Res.string.slots_align_right
    AlignEdge.CenterX -> Res.string.slots_align_center_x
    AlignEdge.CenterY -> Res.string.slots_align_center_y
}
