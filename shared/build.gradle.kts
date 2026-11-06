import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")

    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":platform:composable"))
            implementation(project(":feature:portraitConstructor"))
            implementation(project(":feature:uiEditor"))

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    packageOfResClass = "com.layzen.bdotools.resources"
}

/**
 * Версия сайта: CI передаёт `-PwebVersion=1.0.<номер сборки>` и кладёт тот же номер в `version.txt` рядом
 * с `index.html`. Открытая страница сравнивает с ним свою версию (`shared/src/webMain/.../update`).
 * Без свойства — сборка разработчика, версии нет.
 */
val webVersion: String? = providers.gradleProperty("webVersion").orNull
val generateWebVersion by tasks.registering {
    val output = layout.buildDirectory.dir("generated/webVersion/kotlin")
    val value = webVersion?.let { "\"$it\"" } ?: "null"
    inputs.property("webVersion", value)
    outputs.dir(output)
    doLast {
        val file = output.get().file("com/layzen/bdotools/update/WebVersion.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package com.layzen.bdotools.update
            |
            |/** Версия сайта из сборки (`-PwebVersion`); `null` — сборка разработчика. Файл создаёт `shared/build.gradle.kts`. */
            |internal val WEB_VERSION: String? = $value
            |""".trimMargin(),
        )
    }
}
// webMain появляется из иерархии по умолчанию позже, поэтому не named()
kotlin.sourceSets.matching { it.name == "webMain" }.configureEach { kotlin.srcDir(generateWebVersion) }
