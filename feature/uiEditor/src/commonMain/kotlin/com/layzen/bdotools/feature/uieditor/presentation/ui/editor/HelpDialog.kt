package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_help
import com.layzen.bdotools.feature.uieditor.resources.slots_help_controls
import com.layzen.bdotools.feature.uieditor.resources.slots_help_item
import com.layzen.bdotools.feature.uieditor.resources.slots_help_items
import com.layzen.bdotools.feature.uieditor.resources.slots_help_legend
import com.layzen.bdotools.feature.uieditor.resources.slots_screen
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.verticalScrollbar
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/**
 * Окно «Справка» по кнопке «?» на панели инструментов: управление по пунктам, цвета рамок ([EditorLegend])
 * и экран игры [screen]. Ширина — по содержимому, но не шире [SlotsDimens.helpDialogMaxWidth]: длинные пункты переносятся.
 */
@Composable
internal fun HelpDialog(screen: ScreenSpace?, onDismiss: () -> Unit) {
    ModalWindow(
        title = stringResource(Res.string.slots_help),
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = SlotsDimens.helpDialogMaxWidth),
    ) {
        val scroll = rememberScrollState()
        Column(
            Modifier
                .heightIn(max = SlotsDimens.helpMaxHeight)
                .verticalScrollbar(scroll)
                .verticalScroll(scroll)
                .padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            HelpHeading(stringResource(Res.string.slots_help_controls))
            stringArrayResource(Res.array.slots_help_items).forEachIndexed { index, item ->
                DescriptionText(stringResource(Res.string.slots_help_item, index + 1, item))
            }
            HelpHeading(stringResource(Res.string.slots_help_legend))
            EditorLegend()
            screen?.let {
                DescriptionText(stringResource(Res.string.slots_screen, it.resolutionWidth, it.resolutionHeight, it.uiScale.toString()))
            }
        }
    }
}

@Composable
private fun HelpHeading(text: String) {
    Text(text, style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textBeige)
}
