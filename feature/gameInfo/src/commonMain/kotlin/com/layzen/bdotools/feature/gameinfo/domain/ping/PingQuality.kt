package com.layzen.bdotools.feature.gameinfo.domain.ping

/** Насколько хорош пинг: по нему выбирается цвет. */
enum class PingQuality {
    /** До 60 мс — нормально. */
    Good,

    /** 60–119 мс — задержка заметнее. */
    Fair,

    /** 120–199 мс — тяжело играть. */
    Poor,

    /** От 200 мс. */
    Bad,
}

/** Качество пинга [ms]; `null` — пинг не узнать. */
fun pingQuality(ms: Int?): PingQuality? = when {
    ms == null -> null
    ms < GOOD_MS -> PingQuality.Good
    ms < FAIR_MS -> PingQuality.Fair
    ms < POOR_MS -> PingQuality.Poor
    else -> PingQuality.Bad
}

private const val GOOD_MS = 60
private const val FAIR_MS = 120
private const val POOR_MS = 200
