package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Схема комбо целиком на подложке тултипа игры — отдельное окно поверх игры рядом с трекером, слева направо или
 * сверху вниз ([Combo.vertical]). [onPress] — клик по иконке (предпросмотр). [inGame] — окно поверх игры: во всю длину
 * без прокрутки (окно растёт под схему) и без тултипов умений. [keys] — под умениями их клавиши.
 */
@Composable
internal fun SchemaOverlay(
    combo: Combo,
    overlay: OverlayState,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
    onPress: ((Int) -> Unit)? = null,
    inGame: Boolean = false,
    rage: Boolean = false,
    keys: Boolean = false,
) {
    // вертикальная схема прокручивается в своей высоте: на странице программы над ней уже есть прокрутка
    val scroll = when {
        inGame -> Modifier
        combo.vertical -> Modifier.heightIn(max = ComboDimens.overlaySchemaMaxHeight).verticalScroll(rememberScrollState())
        else -> Modifier.horizontalScroll(rememberScrollState())
    }
    OverlayPanel(rage) {
        Row(scroll.padding(AppTheme.dimens.spaceXs)) {
            ComboSchema(combo, overlay, skills, loadDetails, onPress, tooltips = !inGame, keys = keys)
        }
    }
}
