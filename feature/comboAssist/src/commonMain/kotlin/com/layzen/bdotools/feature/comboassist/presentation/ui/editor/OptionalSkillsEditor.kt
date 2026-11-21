package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.cells
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.common.CrossIcon
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillHover
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_names_language
import com.layzen.bdotools.feature.comboassist.resources.combo_optional_add
import com.layzen.bdotools.feature.comboassist.resources.combo_optional_description
import com.layzen.bdotools.feature.comboassist.resources.combo_optional_search
import com.layzen.bdotools.feature.comboassist.resources.combo_optional_title
import com.layzen.bdotools.feature.comboassist.resources.combo_remove
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.OptionsMenu
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.onSecondaryClick
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Опциональные умения комбо ([com.layzen.bdotools.feature.comboassist.domain.combo.Combo.pool]) в редакторе, под
 * правкой ячейки: иконки в том порядке, в каком их покажет трекер. Порядок меняют перетаскиванием, удаляют крестиком
 * в углу или ПКМ. «+» в конце открывает умения класса с поиском: не из комбо и не филлеры, с перезарядкой, которые
 * можно начать сами.
 */
@Composable
internal fun OptionalSkillsEditor(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val combo = state.edited ?: return
    val language = stringResource(Res.string.combo_names_language)
    SectionHeader(stringResource(Res.string.combo_optional_title), description = stringResource(Res.string.combo_optional_description))
    // шаг между иконками в пикселях: на столько мест сдвигается умение, сколько шагов его протащили
    val step = with(LocalDensity.current) { (ComboDimens.editorIcon + AppTheme.dimens.spaceXs).toPx() }
    var dragged by remember { mutableStateOf<Int?>(null) }
    var offset by remember { mutableFloatStateOf(0f) }
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        combo.pool.forEach { skillNo ->
            val drag = PoolDrag(
                onStart = { dragged = skillNo; offset = 0f },
                onMove = { offset += it },
                onEnd = {
                    val by = (offset / step).roundToInt()
                    if (by != 0) onEvent(ComboEvent.MovePool(skillNo, by))
                    dragged = null
                },
                onCancel = { dragged = null },
            )
            OptionalSkill(skillNo, state.skills[skillNo], if (dragged == skillNo) offset else null, drag, onEvent, loadDetails)
        }
        var open by remember { mutableStateOf(false) }
        Box {
            AddCellSlot(stringResource(Res.string.combo_optional_add)) { open = true }
            val inCombo = combo.cells().mapNotNull { it.second.skillNo }.toSet()
            val candidates = state.skills.values
                .filter { it.skillNo !in inCombo && it.skillNo !in combo.fillers && it.skillNo !in combo.pool && !it.skill.onlyAfter && it.skill.cooldownMs > 0 }
                .sortedBy { it.skill.name(language).lowercase() }
            OptionsMenu(
                open, { open = false }, candidates, null, { onEvent(ComboEvent.AddPool(it.skillNo)) }, { it.skill.name(language) },
                icon = { SkillIconView(it.icon, ComboDimens.paletteIcon) },
                searchPlaceholder = stringResource(Res.string.combo_optional_search),
            )
        }
    }
}

/** Перетаскивание опционального умения: начало, сдвиг по горизонтали в пикселях, конец и отмена. */
private class PoolDrag(val onStart: () -> Unit, val onMove: (Float) -> Unit, val onEnd: () -> Unit, val onCancel: () -> Unit)

/** Одно опциональное умение: иконка с крестиком в правом углу; [offset] — сдвиг, пока его тянут (`null` — не тянут). */
@Composable
private fun OptionalSkill(
    skillNo: Int,
    item: SkillItem?,
    offset: Float?,
    drag: PoolDrag,
    onEvent: (ComboEvent) -> Unit,
    loadDetails: suspend (Int) -> SkillDetails?,
) {
    val current by rememberUpdatedState(drag)
    Box(Modifier.zIndex(if (offset != null) 1f else 0f).graphicsLayer { translationX = offset ?: 0f }) {
        SkillHover(item, loadDetails) {
            SkillIconView(
                item?.icon, ComboDimens.editorIcon,
                Modifier.pointerHoverIcon(PointerIcon.Hand)
                    .onSecondaryClick { onEvent(ComboEvent.RemovePool(skillNo)) }
                    .pointerInput(skillNo) {
                        detectDragGestures(
                            onDragStart = { current.onStart() },
                            onDrag = { change, amount -> change.consume(); current.onMove(amount.x) },
                            onDragEnd = { current.onEnd() },
                            onDragCancel = { current.onCancel() },
                        )
                    },
            )
        }
        // обёртка с выравниванием: HoverTooltip модификатор позиции в Box не передаёт
        Box(Modifier.align(Alignment.TopEnd)) {
            HoverTooltip(stringResource(Res.string.combo_remove)) {
                Box(
                    Modifier.padding(ComboDimens.nextFrame).size(ComboDimens.removeButton).background(AppTheme.colors.backgroundDeep)
                        .pointerHoverIcon(PointerIcon.Hand).clickable { onEvent(ComboEvent.RemovePool(skillNo)) },
                    contentAlignment = Alignment.Center,
                ) { CrossIcon() }
            }
        }
    }
}
