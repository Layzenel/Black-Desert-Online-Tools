package com.layzen.bdotools.update.data

import com.layzen.bdotools.update.domain.SiteVersionRepository

/** Версия страницы из сборки ([WEB_VERSION]) и выложенная версия из файла `version.txt` рядом с `index.html`. */
internal class SiteVersionRepositoryImpl : SiteVersionRepository {

    override val ownVersion: String? get() = WEB_VERSION

    override suspend fun deployedVersion(): String? = fetchDeployedVersion()
}
