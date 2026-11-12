package com.layzen.bdotools.feature.uieditor.domain.file

import com.layzen.bdotools.feature.uieditor.domain.game.SlotsRepository
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout

/** Сценарии файла раскладки `*.json`: выбор файла для импорта и экспорт раскладки в файл. */
class LayoutFilesInteractor(
    private val repository: SlotsRepository,
    private val device: DeviceFiles,
) {

    /** См. [DeviceFiles.pickLayoutFile]. */
    suspend fun pickLayoutFile(): String? = device.pickLayoutFile()

    /** Скачивает [layout] файлом раскладки; имя файла — название раскладки (см. [SlotsRepository.fileName]). */
    suspend fun export(layout: NamedLayout) = device.download(repository.fileName(layout.name), LayoutCodec.encode(layout))

    /** Раскладка [layout] короткой строкой для чата (Discord): её вставляют в «Импорт» как текст файла. */
    fun shareText(layout: NamedLayout): String = LayoutCodec.encode(layout, compact = true)
}
