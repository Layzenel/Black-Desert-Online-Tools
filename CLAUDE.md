# BDO Tools

Сайт с тулзами для Black Desert Online на Kotlin Multiplatform Compose (wasmJs + js) и настольная версия для Windows (JVM, таргет `desktop`).
Сайт деплоится на GitHub Pages при пуше в `main` (`.github/workflows/deploy.yml`), там же собирается MSI и
выкладывается в релиз `desktop-latest` (`.github/workflows/desktop.yml`). Общение — на русском.

## Скиллы — обязательно

Проектные скиллы лежат в `.claude/skills/`, `clean-code` и `subagent-delegation` — общие, в пользовательской папке скиллов.
Перед работой загружай подходящие через инструмент Skill, даже если правка маленькая:

| Когда | Скилл |
|---|---|
| Любой Kotlin-код: новый файл, правка, ревью | `clean-code` |
| Любая правка UI: экран, компонент, цвет, отступ, шрифт | `bdo-design-system` и `resources-externalization` |
| Новая фича или страница, перенос кода между модулями | `feature-module` и `platforms` |
| Файлы, сеть, диалоги, адрес страницы и другие платформенные API; новая зависимость; правка `build.gradle.kts`; код не собирается под одну из платформ | `platforms` |
| Новая или удалённая тулза, изменение того, что она умеет, платформ, установки, команд сборки; правка `README.md` | `readme-sync` |
| Широкий поиск по коду, сборка или тесты с длинным выводом, ревью, задача из независимых частей | `subagent-delegation` |

Если меняешь дизайн-систему (`platform/uikit`), обнови таблицу компонентов в `bdo-design-system`.

Уточнения к `clean-code` для этого проекта:
- Исключение из YAGNI — компоненты `platform/uikit`: это библиотека для будущих страниц, они показаны в каталоге `#showcase`.
- Файлы `*BridgeScript.kt` могут быть длинными: это исходник JS-моста в строковой константе.
  Общая часть моста к папке `Black Desert` (выбор, разрешение, IndexedDB, помощники) — `FolderBridge` в `platform/core`.

## Модули

- `platform/core` — общая логика без UI.
- `platform/uikit` — дизайн-система (`App*` тема и компоненты).
- `platform/composable` — связка core + uikit + Koin + навигация.
- `feature/<имя>` — по модулю на тулзу, фичи друг от друга не зависят.
- `shared` — корень приложения и верхняя навигация.
- `webApp` — только `main()` и `index.html`.
- `desktopApp` — только `main()` с окном и настройка установщика (`compose.desktop.application`).

## Платформы

Приложение работает на двух платформах, и любая фича должна собираться и работать на обеих
(подробно — скилл `platforms`):
- **сайт** — `wasmJs` и `js` в браузере, модуль запуска `webApp`;
- **настольная версия для Windows** — `jvm("desktop")`, модуль запуска `desktopApp`, MSI в релизе `desktop-latest`.

Код, общий для js и wasmJs, лежит в `webMain`, код настольной версии — в `desktopMain`. Платформенные части
(хранилище портретов, навигация по адресу страницы, тексты про браузер) — через `expect`/`actual`.
Сайт для браузеров без File System Access API показывает ссылку на установщик настольной версии.

## Сборка и проверка

```bash
./gradlew :webApp:compileDevelopmentExecutableKotlinWasmJs :webApp:compileKotlinJs :desktopApp:compileKotlinDesktop
```

```bash
./gradlew :feature:portraits:wasmJsBrowserTest :feature:portraits:jsBrowserTest :feature:portraits:desktopTest
```

- Настольная версия: `./gradlew :desktopApp:run`, сборка папки с программой — `:desktopApp:createDistributable`, MSI — `:desktopApp:packageMsi`.

- Dev-сервер — конфигурация `webApp-wasm` в `.claude/launch.json` (порт 8080, `--continuous`, пересобирает сам).
- Пока dev-сервер запущен, отдельный Gradle не запускай: смешанные сборки ломают klib, в рантайме появляется «No function found for symbol». Тогда останови сервер, сделай clean и пересобери.
- Изменения UI проверяй в браузере. Портреты работают через File System Access API — только Chromium;
  в остальных браузерах — через настольную версию.
- Перед завершением собирай и прогоняй тесты обеих платформ (команды выше).

## Git

- Работа идёт в ветках, в `main` — через PR с merge-коммитом.
- Коммить и пушь только по просьбе пользователя.
