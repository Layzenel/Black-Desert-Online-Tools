package com.layzen.bdotools.feature.portraits.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_desktop_description
import com.layzen.bdotools.feature.portraits.resources.portraits_desktop_download
import com.layzen.bdotools.feature.portraits.resources.portraits_desktop_title
import com.layzen.bdotools.feature.portraits.resources.portraits_unsupported_description
import com.layzen.bdotools.feature.portraits.resources.portraits_unsupported_title
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.LinkText
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Постоянная ссылка на установщик: CI выкладывает его в релиз `desktop-latest` под этим именем (`desktop.yml`). */
private const val DESKTOP_INSTALLER_URL =
    "https://github.com/Layzenel/Black-Desert-Online-Tools/releases/download/desktop-latest/BDO-Tools.msi"

/** Браузер не умеет писать в папки: подсказка для него, если есть, и ссылка на настольную версию. */
@Composable
internal fun UnsupportedNotice() {
    val browserHint = remember { unsupportedBrowserHint() }
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        SectionHeader(
            stringResource(Res.string.portraits_unsupported_title),
            warning = stringResource(Res.string.portraits_unsupported_description),
        )
        browserHint?.let { DescriptionText(stringResource(it)) }
        SectionDivider()
        SectionHeader(
            stringResource(Res.string.portraits_desktop_title),
            description = stringResource(Res.string.portraits_desktop_description),
        )
        LinkText(stringResource(Res.string.portraits_desktop_download), DESKTOP_INSTALLER_URL)
    }
}
