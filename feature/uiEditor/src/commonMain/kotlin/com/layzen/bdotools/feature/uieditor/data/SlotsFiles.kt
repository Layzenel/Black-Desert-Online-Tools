package com.layzen.bdotools.feature.uieditor.data

/** Файлы игры и файлы устройства в одном объекте: оба работают через один мост к браузеру. */
internal interface SlotsFiles : SlotsStorage, DeviceFiles

/** Реализация для браузера лежит в `webMain`: там доступен JS. */
internal expect fun createSlotsFiles(): SlotsFiles
