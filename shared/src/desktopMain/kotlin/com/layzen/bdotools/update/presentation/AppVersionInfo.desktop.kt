package com.layzen.bdotools.update.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.menu_reload
import com.layzen.bdotools.resources.update_check
import com.layzen.bdotools.resources.update_check_failed
import com.layzen.bdotools.resources.update_checking
import com.layzen.bdotools.resources.update_install_version
import com.layzen.bdotools.resources.update_up_to_date
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.Toast
import com.layzen.bdotools.uikit.components.ToolbarButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Версия программы и кнопка обновления: «Обновить до …», если вышла новая версия (открывает «Что нового»
 * с кнопкой «Обновить»), иначе иконка «Проверить обновления» (круговая стрелка, подпись — в подсказке). Итог ручной проверки показывает всплывающее сообщение.
 * У программы, запущенной не из пакета, обновлять нечего: кнопки нет.
 */
@Composable
internal actual fun AppVersionInfo() {
    val viewModel = koinViewModel<UpdateViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val checkStatus by viewModel.checkStatus.collectAsStateWithLifecycle()
    val current = state
    VersionPanel {
        VersionText(viewModel.version?.toString())
        if (viewModel.version == null) return@VersionPanel
        if (current is UpdateState.Available) {
            ActionButton(
                stringResource(Res.string.update_install_version, current.info.version.toString()),
                onClick = { viewModel.showNotes(true) },
            )
        } else {
            val checking = checkStatus == UpdateCheckStatus.Checking
            ToolbarButton(
                painterResource(Res.drawable.menu_reload),
                stringResource(if (checking) Res.string.update_checking else Res.string.update_check),
                onClick = viewModel::checkNow,
                enabled = !checking && current == UpdateState.Hidden,
            )
        }
    }
    if (current is UpdateState.Available && current.notesOpen) {
        UpdateNotesDialog(current.info, onClose = { viewModel.showNotes(false) }, onInstall = viewModel::install)
    }
    CheckResultToast(checkStatus, onDismiss = viewModel::clearCheckStatus)
}

@Composable
private fun CheckResultToast(status: UpdateCheckStatus, onDismiss: () -> Unit) {
    val text = when (status) {
        UpdateCheckStatus.UpToDate -> stringResource(Res.string.update_up_to_date)
        UpdateCheckStatus.Failed -> stringResource(Res.string.update_check_failed)
        UpdateCheckStatus.Idle, UpdateCheckStatus.Checking -> null
    }
    Toast(text, onDismiss, isError = status == UpdateCheckStatus.Failed)
}
