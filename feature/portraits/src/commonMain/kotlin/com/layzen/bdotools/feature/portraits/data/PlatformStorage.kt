package com.layzen.bdotools.feature.portraits.data

/** Файлы игры и источник картинок в одном объекте: оба работают через один мост к браузеру. */
internal interface GameFiles : PortraitsStorage, ImageSource

/** Реализация для браузера лежит в `webMain`: там доступен JS. */
internal expect fun createGameFiles(): GameFiles
