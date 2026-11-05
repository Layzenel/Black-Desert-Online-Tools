package com.layzen.bdotools.feature.portraits.presentation.ui.notice

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraits.presentation.state.Notice
import com.layzen.bdotools.feature.portraits.presentation.state.NoticeKind
import com.layzen.bdotools.feature.portraits.presentation.ui.permissionErrorText
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_backups_deleted
import com.layzen.bdotools.feature.portraits.resources.portraits_decoration_saved
import com.layzen.bdotools.feature.portraits.resources.portraits_error_blocked
import com.layzen.bdotools.feature.portraits.resources.portraits_error_network
import com.layzen.bdotools.feature.portraits.resources.portraits_error_not_game_folder
import com.layzen.bdotools.feature.portraits.resources.portraits_error_not_image
import com.layzen.bdotools.feature.portraits.resources.portraits_error_unknown
import com.layzen.bdotools.feature.portraits.resources.portraits_favorite_added
import com.layzen.bdotools.feature.portraits.resources.portraits_favorite_applied
import com.layzen.bdotools.feature.portraits.resources.portraits_favorite_replaced
import com.layzen.bdotools.feature.portraits.resources.portraits_favorites_deleted
import com.layzen.bdotools.feature.portraits.resources.portraits_reorder_banner
import com.layzen.bdotools.feature.portraits.resources.portraits_restored
import com.layzen.bdotools.feature.portraits.resources.portraits_saved
import com.layzen.bdotools.feature.portraits.resources.portraits_swapped
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun noticeText(notice: Notice): String = when (notice.kind) {
    NoticeKind.Saved -> stringResource(Res.string.portraits_saved, notice.argument)
    NoticeKind.DecorationSaved -> stringResource(Res.string.portraits_decoration_saved, notice.argument)
    NoticeKind.Restored -> stringResource(Res.string.portraits_restored, notice.argument)
    NoticeKind.BackupsDeleted -> stringResource(Res.string.portraits_backups_deleted, notice.argument.toInt())
    NoticeKind.FavoriteAdded -> stringResource(Res.string.portraits_favorite_added, notice.argument)
    NoticeKind.FavoriteApplied -> stringResource(Res.string.portraits_favorite_applied, notice.argument)
    NoticeKind.Swapped -> stringResource(Res.string.portraits_swapped, notice.argument)
    NoticeKind.FavoritesDeleted -> stringResource(Res.string.portraits_favorites_deleted, notice.argument.toInt())
    NoticeKind.FavoriteReplaced -> stringResource(Res.string.portraits_favorite_replaced, notice.argument)
    NoticeKind.ReorderHint -> stringResource(Res.string.portraits_reorder_banner)
    NoticeKind.NotGameFolder -> stringResource(Res.string.portraits_error_not_game_folder)
    NoticeKind.Permission -> stringResource(permissionErrorText)
    NoticeKind.Blocked -> stringResource(Res.string.portraits_error_blocked)
    NoticeKind.Network -> stringResource(Res.string.portraits_error_network)
    NoticeKind.NotImage -> stringResource(Res.string.portraits_error_not_image)
    NoticeKind.Unknown -> stringResource(Res.string.portraits_error_unknown, notice.argument)
}
