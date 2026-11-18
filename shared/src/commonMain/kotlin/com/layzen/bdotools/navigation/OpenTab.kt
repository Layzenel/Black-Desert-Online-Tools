package com.layzen.bdotools.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList

/** Открывает вкладку [tab], если она ещё не открыта: стек растёт, «назад» вернёт на прошлую. */
fun SnapshotStateList<Destination>.openTab(tab: AppTab) {
    if (lastOrNull() != Destination.Tab(tab)) add(Destination.Tab(tab))
}
