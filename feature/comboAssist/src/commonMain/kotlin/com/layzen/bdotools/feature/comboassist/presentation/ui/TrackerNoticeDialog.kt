package com.layzen.bdotools.feature.comboassist.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_intro
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_limit_admin
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_limit_cheap
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_limit_hidden
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_limit_history_gaps
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_limit_history_off
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_limit_keys
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_limit_rage
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_limits
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_ok
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_source_history
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_source_keys
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_source_resource
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_sources
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_tip_history
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_tip_resync
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_tips
import com.layzen.bdotools.feature.comboassist.resources.combo_notice_title
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.BulletList
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Разделы окна: заголовок и пункты. */
private val SECTIONS: List<Pair<StringResource, List<StringResource>>> = listOf(
    Res.string.combo_notice_sources to listOf(
        Res.string.combo_notice_source_history, Res.string.combo_notice_source_keys, Res.string.combo_notice_source_resource,
    ),
    Res.string.combo_notice_limits to listOf(
        Res.string.combo_notice_limit_admin, Res.string.combo_notice_limit_history_off, Res.string.combo_notice_limit_history_gaps,
        Res.string.combo_notice_limit_cheap, Res.string.combo_notice_limit_keys, Res.string.combo_notice_limit_hidden,
        Res.string.combo_notice_limit_rage,
    ),
    Res.string.combo_notice_tips to listOf(Res.string.combo_notice_tip_history, Res.string.combo_notice_tip_resync),
)

/**
 * Окно «Как работает трекер»: откуда он знает о применённых умениях, его ограничения и советы. Само открывается
 * при первом открытии помощника комбо в настольной версии; закрыть — [onClose] (кнопка, крестик, Esc).
 */
@Composable
internal fun TrackerNoticeDialog(onClose: () -> Unit) {
    ModalWindow(
        title = stringResource(Res.string.combo_notice_title),
        onDismissRequest = onClose,
        modifier = Modifier.width(ComboDimens.noticeWidth),
        footer = { ActionButton(stringResource(Res.string.combo_notice_ok), onClick = onClose) },
    ) {
        val scroll = rememberScrollState()
        Column(
            Modifier.heightIn(max = ComboDimens.noticeMaxHeight).verticalScrollbar(scroll).verticalScroll(scroll).padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            Text(stringResource(Res.string.combo_notice_intro), style = AppTheme.typography.body, color = AppTheme.colors.textPrimary)
            SECTIONS.forEach { (title, points) ->
                SectionHeader(stringResource(title))
                BulletList(points.map { stringResource(it) })
            }
        }
    }
}
