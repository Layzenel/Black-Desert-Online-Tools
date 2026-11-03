package com.layzen.bdotools.feature.portraits.data

/** Состояние доступа к папке `Документы\Black Desert`. */
sealed interface FolderAccess {
    /** Браузер не умеет записывать в папки на диске (не Chromium). */
    data object Unsupported : FolderAccess

    /** Папка ещё ни разу не выбиралась. */
    data object NotSelected : FolderAccess

    /** Папка выбрана раньше, но после перезагрузки страницы браузер снова спрашивает разрешение. */
    data class NeedsPermission(val folderName: String) : FolderAccess

    data class Granted(val folderName: String) : FolderAccess
}
