package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import com.layzen.bdotools.feature.comboassist.presentation.state.SkillItem
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_filler
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Оверлей комбо: карточка следующего умения и филлер сверху, схема комбо снизу. Один и тот же
 * в тренировке и в окне поверх игры. [onPress] — клик по иконке; `null` — оверлей только показывает.
 * [inGame] — оверлей в окне поверх игры: схема во всю длину без прокрутки (окно растёт под неё) и без тултипов.
 */
@Composable
fun ComboOverlay(
    combo: Combo,
    overlay: OverlayState,
    skills: Map<Int, SkillItem>,
    loadDetails: suspend (Int) -> SkillDetails?,
    modifier: Modifier = Modifier,
    onPress: ((Int) -> Unit)? = null,
    inGame: Boolean = false,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        // верх своей высоты: карточка появляется и меняется, а схема под ней не прыгает
        Row(
            Modifier.height(ComboDimens.overlayTopHeight),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                overlay.next?.skillNo,
                transitionSpec = { (slideInHorizontally(tween(SWAP_MS)) { it / SLIDE_SHARE } + fadeIn(tween(SWAP_MS))) togetherWith fadeOut(tween(SWAP_MS)) },
            ) { skillNo -> if (skillNo != null) NextSkillCard(overlay, skills, loadDetails) }
            AnimatedVisibility(overlay.filler != null, enter = fadeIn(tween(SWAP_MS)), exit = fadeOut(tween(SWAP_MS))) {
                overlay.filler?.let { FillerCard(it, skills) }
            }
        }
        // вертикальная схема прокручивается в своей высоте: в странице тренировки над ней уже есть прокрутка
        val scroll = when {
            inGame -> Modifier
            combo.vertical -> Modifier.heightIn(max = ComboDimens.overlaySchemaMaxHeight).verticalScroll(rememberScrollState())
            else -> Modifier.horizontalScroll(rememberScrollState())
        }
        Row(Modifier.background(AppTheme.colors.backgroundDeep).border(ComboDimens.frame, AppTheme.colors.border).then(scroll).padding(AppTheme.dimens.spaceM)) {
            ComboSchema(combo, overlay, skills, loadDetails, onPress, tooltips = !inGame)
        }
    }
}

/** Карточка филлера на время ожидания. */
@Composable
internal fun FillerCard(skillNo: Int, skills: Map<Int, SkillItem>) {
    Row(
        Modifier.border(ComboDimens.frame, AppTheme.colors.textBeige).padding(AppTheme.dimens.spaceXs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkillIconView(skills[skillNo]?.icon, ComboDimens.overlayIcon)
        Column {
            Text(stringResource(Res.string.combo_filler), style = AppTheme.typography.caption, color = AppTheme.colors.textMuted)
            Text(skillName(skills, skillNo), style = AppTheme.typography.bodyBold, color = AppTheme.colors.textPrimary)
        }
    }
}

private const val SWAP_MS = 250
private const val SLIDE_SHARE = 4
