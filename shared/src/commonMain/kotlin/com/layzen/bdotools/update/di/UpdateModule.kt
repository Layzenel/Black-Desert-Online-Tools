package com.layzen.bdotools.update.di

import org.koin.core.module.Module

/**
 * Koin-модуль обновления: репозиторий версии своей платформы, интерактор и ViewModel. На сайте он следит
 * за выложенной версией страницы, в настольной версии — за сборками программы.
 */
internal expect val updateModule: Module
