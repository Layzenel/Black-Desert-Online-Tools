# BDO Tools

Сайт с тулзами для Black Desert Online на Kotlin Multiplatform Compose (wasmJs + js).
Деплоится на GitHub Pages при пуше в `main` (`.github/workflows/deploy.yml`). Общение — на русском.

## Скиллы — обязательно

Проектные скиллы лежат в `.claude/skills/`, `clean-code` и `subagent-delegation` — общие, в пользовательской папке скиллов.
Перед работой загружай подходящие через инструмент Skill, даже если правка маленькая:

| Когда | Скилл |
|---|---|
| Любой Kotlin-код: новый файл, правка, ревью | `clean-code` |
| Любая правка UI: экран, компонент, цвет, отступ, шрифт | `bdo-design-system` и `resources-externalization` |
| Новая фича или страница, перенос кода между модулями | `feature-module` |
| Широкий поиск по коду, сборка или тесты с длинным выводом, ревью, задача из независимых частей | `subagent-delegation` |

Если меняешь дизайн-систему (`platform/uikit`), обнови таблицу компонентов в `bdo-design-system`.

Уточнения к `clean-code` для этого проекта:
- Исключение из YAGNI — компоненты `platform/uikit`: это библиотека для будущих страниц, они показаны в каталоге `#showcase`.
- `FileSystemBridgeScript.kt` длиннее 200 строк намеренно: это исходник JS-моста в строковой константе.

## Модули

- `platform/core` — общая логика без UI.
- `platform/uikit` — дизайн-система (`App*` тема и компоненты).
- `platform/composable` — связка core + uikit + Koin + навигация.
- `feature/<имя>` — по модулю на тулзу, фичи друг от друга не зависят.
- `shared` — корень приложения и верхняя навигация.
- `webApp` — только `main()` и `index.html`.

## Сборка и проверка

```bash
./gradlew :webApp:compileDevelopmentExecutableKotlinWasmJs :webApp:compileKotlinJs
```

```bash
./gradlew :feature:portraits:wasmJsBrowserTest :feature:portraits:jsBrowserTest
```

- Dev-сервер — конфигурация `webApp-wasm` в `.claude/launch.json` (порт 8080, `--continuous`, пересобирает сам).
- Пока dev-сервер запущен, отдельный Gradle не запускай: смешанные сборки ломают klib, в рантайме появляется «No function found for symbol». Тогда останови сервер, сделай clean и пересобери.
- Изменения UI проверяй в браузере. Портреты работают через File System Access API — только Chromium.

## Git

- Работа идёт в ветках, в `main` — через PR с merge-коммитом.
- Коммить и пушь только по просьбе пользователя.
