package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.ImageSource

/** Файлы игры и источник картинок в одном объекте: на каждой платформе они работают через одни и те же средства. */
internal interface GameFiles : PortraitsStorage, ImageSource

/** Реализация для браузера лежит в `webMain` (мост к JS), для настольной версии — в `desktopMain` (`java.nio.file`). */
internal expect fun createGameFiles(): GameFiles
