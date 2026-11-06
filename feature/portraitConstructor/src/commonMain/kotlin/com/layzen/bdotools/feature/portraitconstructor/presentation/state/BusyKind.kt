package com.layzen.bdotools.feature.portraitconstructor.presentation.state

/** Какая долгая операция идёт: определяет текст окна ожидания. */
enum class BusyKind { Rendering, Saving, Restoring, Deleting, SavingFavorites, ReadingFavorites }
