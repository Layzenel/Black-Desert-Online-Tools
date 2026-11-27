package com.layzen.bdotools.feature.screenshoteditor.domain.file

import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat

private const val EDITED_SUFFIX = "_edited"

/** Имя копии скриншота [source] в формате [format]: `2026-11-27_230089871.PNG` → `2026-11-27_230089871_edited.jpg`. */
fun editedName(source: String, format: ExportFormat): String =
    source.substringBeforeLast('.').ifEmpty { source } + EDITED_SUFFIX + "." + format.extension

/** [name], а если оно занято ([taken]) — с номером перед расширением: `a.png` → `a_2.png`, `a_3.png`… */
fun freeName(name: String, taken: (String) -> Boolean): String {
    if (!taken(name)) return name
    val base = name.substringBeforeLast('.')
    val extension = name.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".$it" }
    return generateSequence(2) { it + 1 }.map { "${base}_$it$extension" }.first { !taken(it) }
}
