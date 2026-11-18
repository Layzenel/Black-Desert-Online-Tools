package com.layzen.bdotools.core.skill

import com.layzen.bdotools.core.skill.data.SkillCatalogRepositoryImpl
import com.layzen.bdotools.core.skill.data.createSkillDownloads
import com.layzen.bdotools.core.skill.domain.SkillCatalogRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Koin-модуль общего справочника навыков: загрузки своей платформы и один [SkillCatalogRepository] на приложение. */
val skillCatalogModule = module {
    single { createSkillDownloads() }
    singleOf(::SkillCatalogRepositoryImpl) bind SkillCatalogRepository::class
}
