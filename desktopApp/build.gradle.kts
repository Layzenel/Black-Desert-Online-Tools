import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)
        }
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.layzen.bdotools.desktop.resources"
    generateResClass = always
}

/** Версия установщика: CI передаёт `-PdesktopVersion=1.0.<номер сборки>`, чтобы MSI обновлял старую установку. */
val desktopVersion = providers.gradleProperty("desktopVersion").getOrElse("1.0.0")

compose.desktop {
    application {
        mainClass = "com.layzen.bdotools.desktop.MainKt"
        // по версии программа понимает, вышло ли обновление (проверка в shared/.../update)
        jvmArgs("-Dbdotools.version=$desktopVersion")

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "BDO Tools"
            packageVersion = desktopVersion
            // Только латиница: WiX собирает MSI в кодовой странице 1252 и падает на кириллице (ошибка LGHT0311)
            description = "Black Desert Online Tools"
            vendor = "Layzen"
            // Список от задачи suggestRuntimeModules: без них урезанная JRE не запустит программу
            modules("java.instrument", "java.net.http", "java.prefs", "jdk.security.auth", "jdk.unsupported")

            windows {
                menuGroup = "BDO Tools"
                shortcut = true
                menu = true
                dirChooser = true
                perUserInstall = true
                // Иконка exe, ярлыков и установщика; рисунок тот же, что app_icon.svg
                iconFile.set(project.file("icon.ico"))
                // Постоянный код продукта: новая версия MSI заменяет старую, а не ставится рядом
                upgradeUuid = "6f1d1c52-7d0e-4b8a-9a52-3f0b6f6e2c41"
            }
        }
    }
}
