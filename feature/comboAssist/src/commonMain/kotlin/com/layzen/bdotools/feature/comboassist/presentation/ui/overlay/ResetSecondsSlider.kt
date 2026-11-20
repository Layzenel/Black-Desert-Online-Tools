package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_reset_description
import com.layzen.bdotools.feature.comboassist.resources.combo_reset_title
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ValueSlider
import org.jetbrains.compose.resources.stringResource

/**
 * «Сброс комбо»: через сколько секунд без умений комбо трекер начинает его сначала ([seconds]). Пока ползунок тянут,
 * значение только показывается; сохраняется и перезапускает трекер, когда его отпустили. Стоит в меню оверлея,
 * во вкладке «Комбо».
 */
@Composable
internal fun ResetSecondsSlider(seconds: Int, onEvent: (ComboEvent) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    SectionHeader(stringResource(Res.string.combo_reset_title, seconds), description = stringResource(Res.string.combo_reset_description))
    ValueSlider(
        seconds, { onEvent(ComboEvent.SetResetSeconds(it, save = false)) }, RESET_SECONDS, modifier,
        onValueChangeFinished = { onEvent(ComboEvent.SetResetSeconds(seconds, save = true)) },
    )
}

/** Сколько секунд можно задать для сброса комбо. */
private val RESET_SECONDS = 1..60
