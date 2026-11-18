import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
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
            implementation(libs.kotlinx.serialization.json)
        }
        // Окно оверлея поверх игры пропускает клики: стиль окна Windows через JNA
        val desktopMain by getting {
            dependencies {
                implementation(libs.jna.platform)
                // окна «Открыть» и «Сохранить как» для экспорта и импорта комбо
                implementation(libs.file.picker.dialogs)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    packageOfResClass = "com.layzen.bdotools.feature.comboassist.resources"
    generateResClass = always
}
