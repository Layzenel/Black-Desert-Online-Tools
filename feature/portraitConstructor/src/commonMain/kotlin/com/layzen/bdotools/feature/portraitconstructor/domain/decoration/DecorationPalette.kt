package com.layzen.bdotools.feature.portraitconstructor.domain.decoration

/**
 * Цвета на выбор для надписи и значка портрета, `0xRRGGBB`. Это данные портрета, а не тема сайта: цвет
 * записывается в файл украшения. Цвет по умолчанию — `null`, его в списке нет.
 */
object DecorationPalette {
    val colors: List<Int> = listOf(
        0xF8C038, // золотой
        0xE0783C, // оранжевый, как заметка персонажа в игре
        0xE04848, // красный
        0xE070B0, // розовый
        0xA070E0, // фиолетовый
        0x5090E8, // синий
        0x50C8D8, // бирюзовый
        0x70C850, // зелёный
        0xB0B0B0, // серый
        0x000000, // чёрный
    )
}
