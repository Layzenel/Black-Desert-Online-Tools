package com.layzen.bdotools.feature.portraits.presentation.state

/** Какая долгая операция идёт: определяет текст окна ожидания. */
enum class BusyKind { Rendering, Saving, Restoring, Deleting, SavingFavorites, ReadingFavorites }
