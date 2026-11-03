package com.layzen.bdotools.feature.portraits.presentation.ui.decoration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_clear
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_counter
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_description
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_field
import com.layzen.bdotools.feature.portraits.resources.portraits_caption_placeholder
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import org.jetbrains.compose.resources.stringResource

/** Поле надписи со счётчиком символов и кнопкой «Убрать надпись». */
@Composable
internal fun CaptionControls(draft: PortraitDecoration, onEvent: (PortraitsEvent) -> Unit) {
    SectionHeader(
        stringResource(Res.string.portraits_caption_field),
        description = stringResource(Res.string.portraits_caption_description),
    )
    InputField(
        value = draft.caption,
        onValueChange = { onEvent(PortraitsEvent.CaptionChanged(it)) },
        placeholder = stringResource(Res.string.portraits_caption_placeholder),
        modifier = Modifier.fillMaxWidth(),
    )
    DescriptionText(stringResource(Res.string.portraits_caption_counter, draft.caption.length, PortraitDecoration.CAPTION_MAX_LENGTH))
    SectionDivider()
    ActionButton(
        stringResource(Res.string.portraits_caption_clear),
        onClick = { onEvent(PortraitsEvent.CaptionChanged("")) },
        enabled = draft.caption.isNotEmpty(),
    )
}
