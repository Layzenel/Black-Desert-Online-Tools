---
name: platforms
description: Правило проекта BDO Tools — приложение работает на двух платформах, сайт (wasmJs и js в браузере) и настольная версия для Windows (JVM, таргет desktop), и любая фича должна собираться и работать на обеих. Использовать перед любой новой фичей, при работе с файлами, сетью, диалогами, буфером обмена, адресом страницы или другими платформенными API, при добавлении зависимости, при правке build.gradle.kts, и когда код не собирается под одну из платформ.
---

# Две платформы: сайт и настольная версия

Проект — Kotlin Multiplatform. Один и тот же код (`commonMain`) собирается в две программы:

| Платформа | Таргеты | Где запускается | Модуль запуска | Как попадает к пользователю |
|---|---|---|---|---|
| **Сайт** | `wasmJs` и `js` | браузер | `webApp` (`main()`, `index.html`) | GitHub Pages, `.github/workflows/deploy.yml` при пуше в `main` |
| **Настольная версия** | `jvm("desktop")` | Windows | `desktopApp` (`main()` с окном, `compose.desktop.application`) | MSI и портативный ZIP в релизе `desktop-latest`, `.github/workflows/desktop.yml` при пуше в `main` |

Настольная версия нужна там, где браузер не умеет того, что нужно тулзе. Например, портретам нужна запись
в папку игры: в браузере это File System Access API, а он есть только в Chromium. В Firefox и Safari его нет,
в Brave он выключен флагом. Для таких браузеров сайт показывает ссылку на установщик:
`https://github.com/Layzenel/Black-Desert-Online-Tools/releases/download/desktop-latest/BDO-Tools.msi`.

## Где лежит код

| Наборы исходников | Что там |
|---|---|
| `commonMain` | всё, что одинаково на обеих платформах: логика, состояние, экраны, ресурсы. Основная часть кода |
| `webMain` | общее для `js` и `wasmJs`: браузерные API, JS-мост, адрес страницы |
| `desktopMain` | только для JVM: `java.nio.file`, `java.net.http`, системные диалоги, `Preferences` |
| `commonTest` | тесты, которые запускаются на всех платформах |
| `desktopTest` | тесты JVM-реализаций, например хранилища на временной папке |

Каждый модуль, в котором есть код приложения (`platform/core`, `platform/uikit`, `platform/composable`,
`feature/<имя>`, `shared`), объявляет все три таргета: `js`, `wasmJs`, `jvm("desktop")`. Новый модуль фичи —
тоже (шаблон в скилле `feature-module`).

## Как разделять платформенный код

1. **Сначала общий код.** Если нужное есть в `commonMain` (Compose, корутины, Koin, ресурсы, `kotlinx.*`),
   пиши там. Платформенный набор — только когда без платформенного API не обойтись.
2. **Интерфейс в `commonMain`, реализации в `webMain` и `desktopMain`.** Так сделано хранилище портретов:
   `PortraitsStorage` и `ImageSource` — интерфейсы в `commonMain`, `GameFiles` объединяет их,
   `expect fun createGameFiles()` создаёт нужную реализацию (`BrowserPortraitsStorage` в вебе,
   `DesktopPortraitsStorage` на Desktop), Koin получает её из `PortraitsModule`. Логика (репозиторий,
   контроллеры, экраны) не знает, на какой она платформе.
3. **`expect`/`actual` — для маленьких точек различия**: фабрика реализации, платформенный текст, связь
   навигации с адресом страницы (`PlatformNavigation`: в браузере — `ChronologicalBrowserNavigation`,
   на Desktop пусто). Не тащи через `expect` целые экраны.
4. **Тексты, которые различаются.** В браузере файлы читает «сайт», на Desktop — «программа», а ошибки
   доступа звучат по-разному. Такие строки выбираются через `expect val` (пример — `PlatformTexts.kt` в
   `feature/portraits`), сами строки лежат в общем `strings.xml`.
5. **Одинаковое поведение.** Реализации одного интерфейса ведут себя одинаково: те же форматы файлов и
   имён папок, те же ошибки (`PortraitsError`), те же состояния (`FolderAccess`). Если поведение где-то
   отличается, опиши это в KDoc реализации. Тесты репозитория идут на `MemoryStorage` в `commonTest`
   и потому проверяют обе платформы.
6. **Состояния, которых нет на платформе.** `FolderAccess.Unsupported` и `NeedsPermission` бывают только
   в браузере; Desktop их не возвращает, но экран их всё равно умеет показывать.

## Зависимости

- Перед добавлением библиотеки проверь, что она есть для `js`, `wasmJs` и `jvm`. Если только для части
  платформ — подключай в `webMain.dependencies` или `desktopMain.dependencies`, а не в `commonMain`
  (пример — `navigation3-browser` в `platform/composable`: он есть только для браузера).
- JVM-библиотеки (`java.*`, `javax.*`, `java.awt`) — только в `desktopMain`. Браузерные (`kotlinx.browser`,
  `org.w3c.*`, `js(...)`) — только в `webMain`.
- Версии — в `gradle/libs.versions.toml`.

## Проверка — всегда обе платформы

Перед тем как закончить, собери и прогони тесты обеих платформ:

```bash
./gradlew :webApp:compileDevelopmentExecutableKotlinWasmJs :webApp:compileKotlinJs :desktopApp:compileKotlinDesktop
```

```bash
./gradlew :feature:portraits:wasmJsBrowserTest :feature:portraits:jsBrowserTest :feature:portraits:desktopTest
```

- Сайт: UI проверяй в браузере через dev-сервер `webApp-wasm` из `.claude/launch.json`.
  Пока он запущен, отдельный Gradle не запускай (см. `CLAUDE.md`).
- Настольная версия: `./gradlew :desktopApp:run`, папка с программой — `:desktopApp:createDistributable`,
  установщик — `:desktopApp:packageMsi`.
- Если на одной из платформ проверить не удалось (нет браузера, нет Windows, workflow не запускался),
  так и скажи пользователю.

## Подводные камни

- WiX (сборщик MSI) не принимает кириллицу в описании пакета, ошибка LGHT0311. Поэтому `description`
  в `desktopApp/build.gradle.kts` на латинице. Название окна и тексты интерфейса — на русском, как везде.
- Версия MSI — `1.0.<номер сборки>` (`-PdesktopVersion` в workflow): новая версия заменяет старую
  установку, а не ставится рядом. `upgradeUuid` в `desktopApp/build.gradle.kts` не меняй.
- Каталог компонентов `#showcase` открывается только на сайте: в настольной версии нет адреса страницы.
- Ссылка на установщик на сайте и имена файлов в `desktop.yml` (`BDO-Tools.msi`, `BDO-Tools-portable.zip`)
  должны совпадать. Меняешь одно — меняй другое.
