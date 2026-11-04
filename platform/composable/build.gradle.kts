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
            api(project(":platform:core"))
            api(project(":platform:uikit"))

            api(libs.androidx.lifecycle.viewmodelCompose)
            api(libs.androidx.lifecycle.runtimeCompose)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
        }
        // Навигация через историю браузера есть только в вебе
        webMain.dependencies {
            api(libs.navigation3.browser)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
