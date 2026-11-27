package com.layzen.bdotools.feature.screenshoteditor.domain.adjust

/** Готовые фильтры — те же ползунки ретуши, выставленные заранее. [None] сбрасывает ретушь. */
enum class FilterPreset(val adjustments: Adjustments) {
    None(Adjustments()),
    Warm(Adjustments(temperature = 35, saturation = 10)),
    Cold(Adjustments(temperature = -35, contrast = 5)),
    Vivid(Adjustments(saturation = 35, contrast = 15)),
    Cinema(Adjustments(contrast = 20, saturation = -20, temperature = 10, vignette = 45)),
    Mono(Adjustments(saturation = -100, contrast = 15)),
}
