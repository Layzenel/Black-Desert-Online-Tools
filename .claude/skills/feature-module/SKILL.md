---
name: feature-module
description: Правило проекта BDO Tools — каждая новая фича (тулза, страница, калькулятор) создаётся в отдельном Gradle-модуле feature/<имя>. Использовать перед началом любой новой фичи или страницы, при переносе кода фичи и когда непонятно, куда положить новый код.
---

# Каждая фича — отдельный модуль

Новая фича **всегда** живёт в своём модуле `:feature:<имя>` в папке `feature/<имя>/`.
Код фичи нельзя класть в `shared`, `webApp` или `platform/*`, даже если он маленький.

Почему так:
- фичи не видят друг друга и не обрастают случайными связями;
- Gradle пересобирает только изменённый модуль;
- фичу легко найти, удалить или переписать целиком.

## Куда что класть

| Модуль | Что там | Чего там быть не должно |
|---|---|---|
| `feature/<имя>` | всё, что относится к одной тулзе: формулы, данные, ViewModel, экраны, DI-модуль | код другой фичи |
| `platform/core` | общая логика без UI, нужная двум и более фичам (форматирование серебра, общие модели) | Compose, код одной фичи |
| `platform/uikit` | дизайн-система (см. скилл `bdo-design-system`) | бизнес-логика |
| `platform/composable` | связка core + uikit + Koin + навигация, общие Compose-утилиты | код одной фичи |
| `shared` | корень приложения: `AppTheme`, `KoinApplication`, верхняя навигация, список фич | экраны и логика фич |
| `webApp` | только `main()` и `index.html` | всё остальное |
| `desktopApp` | только `main()` с окном и настройка установщика | всё остальное |

Код, нужный только одной фиче, остаётся в ней. Переносить его в `platform/*` можно только тогда,
когда он реально понадобился второй фиче.

## Правила зависимостей

- Фича зависит **только** от `:platform:composable`. Через него транзитивно (`api`) доступны `core`, `uikit`,
  coroutines, Koin, lifecycle-viewmodel, а в `webMain` ещё и navigation3-browser (на Desktop его нет).
- Код, который различается в браузере и на Desktop, — `expect` в `commonMain`, `actual` в `webMain` и `desktopMain`
  (пример — `createGameFiles()` и `PlatformTexts.kt` в фиче портретов).
- **Фича никогда не зависит от другой фичи.** Если двум фичам нужен общий код, перенеси его в `platform/core`
  (логика) или `platform/uikit` (UI).
- От фич зависит только `shared`: он собирает их в приложение.

## Именование

- Папка и Gradle-путь в kebab-case: `feature/enhancement-calculator` → `:feature:enhancement-calculator`.
- Пакет — то же имя без дефисов: `com.layzen.bdotools.feature.enhancementcalculator`.
- Классы с префиксом фичи: `EnhancementViewModel`, `EnhancementScreen`, `enhancementModule`.

## Структура модуля

```
feature/<имя>/
├── build.gradle.kts
└── src/
    ├── commonMain/composeResources/values/
    │   └── strings.xml    все строки фичи на русском (ключи с префиксом фичи)
    ├── commonMain/composeResources/values-en/
    │   └── strings.xml    те же ключи на английском; такая же папка `values-<tag>` — для каждого языка
    │                      из `AppLanguage` (скилл resources-externalization, раздел «Языки»)
    ├── commonMain/kotlin/com/layzen/bdotools/feature/<пакет>/
    │   ├── domain/        чистый Kotlin без Compose; ничего не импортирует из data и presentation.
    │   │   └── <тема>/    папка на тему предметной области (`backup/`, `favorite/`, `layout/`): её модели
    │   │                  и формулы, **интерфейс** `<Тема>Repository` и `<Тема>Interactor`; ошибки — в `error/`.
    │   │                  В корне `domain/` файлов нет
    │   ├── data/          `<Тема>RepositoryImpl` — реализации интерфейсов из domain — и интерфейс источника
    │   │                  данных `<Имя>Storage`; его платформенные реализации — в webMain и desktopMain
    │   ├── presentation/  <Имя>ViewModel получает только интеракторы; state/, event/, ui/ (экран и его части
    │   │                  по папкам), controller/ — если ViewModel разрастается (см. скилл clean-code)
    │   └── di/            <имя>Module (Koin): реализации репозиториев, интеракторы, ViewModel
    └── commonTest/kotlin/com/layzen/bdotools/feature/<пакет>/
        ├── domain/<тема>/ тесты формул — обязательны; лежат в той же папке-теме, что и код
        └── data/          тесты `…RepositoryImpl` на хранилище в памяти (`MemoryStorage`)
```

Как это выглядит в готовых фичах:

| Фича | Папки `domain/` | Интеракторы | Репозитории (интерфейс → реализация) |
|---|---|---|---|
| `portraitConstructor` | `portrait`, `decoration`, `backup`, `favorite`, `memo`, `error` | `PortraitsInteractor`, `BackupsInteractor`, `FavoritesInteractor`, `MemosInteractor` | `PortraitsRepository` → `PortraitsRepositoryImpl`, `CharacterMemosRepository` → `CharacterMemosRepositoryImpl` |
| `uiEditor` | `game`, `layout`, `arrangement`, `screen`, `file`, `preset`, `error` | `GameSettingsInteractor`, `PresetsInteractor`, `LayoutFilesInteractor` | `SlotsRepository` → `SlotsRepositoryImpl` |
| `shared/update` (не фича, но слои те же) | `domain` | `UpdateInteractor` (Desktop), `SiteUpdateInteractor` (сайт) | `UpdateRepository` → `UpdateRepositoryImpl`, `SiteVersionRepository` → `SiteVersionRepositoryImpl` |

## Как создать фичу

### 1. `feature/<имя>/build.gradle.kts`

Копируется из шаблона:

```kotlin
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
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    packageOfResClass = "com.layzen.bdotools.feature.<пакет>.resources"
    generateResClass = always
}
```

Единственное, что меняется в шаблоне, — `<пакет>` в `packageOfResClass`.
Сторонние библиотеки, нужные только этой фиче, добавляются сюда через `libs.versions.toml`.

### 2. Подключить модуль в `settings.gradle.kts`

```kotlin
include(":feature:<имя>")
```

### 3. Логика в `domain/`

Только чистые функции и классы, без Compose и без Koin-аннотаций. Здесь лежат формулы игры,
и на каждую формулу пишется тест в `commonTest`. Файлы раскладываются по папкам-темам, а не по виду класса.

Если фиче нужны данные (файлы игры, сеть), в папке темы появляются интерфейс репозитория и интерактор,
а в `data/` — реализация (подробно — скилл `clean-code`, раздел «Clean Architecture»):

```kotlin
// domain/preset/PresetsRepository.kt — контракт с KDoc
interface PresetsRepository {
    /** Пресеты аккаунта [account]. */
    suspend fun presets(account: String): List<Preset>
}

// domain/preset/PresetsInteractor.kt — сценарии; только его видит ViewModel
class PresetsInteractor(private val repository: PresetsRepository) {
    /** См. [PresetsRepository.presets]. */
    suspend fun presets(account: String): List<Preset> = repository.presets(account)
}

// data/PresetsRepositoryImpl.kt — работа с файлами через интерфейс хранилища
class PresetsRepositoryImpl(private val storage: SampleStorage) : PresetsRepository {
    override suspend fun presets(account: String): List<Preset> = storage.presetFiles(account).map(::decode)
}
```

ViewModel и контроллеры `presentation/` получают в конструктор только интеракторы: ни репозиторий,
ни `*Storage` в `presentation` не импортируются. Фиче без данных (чистый калькулятор) репозиторий
и интерактор не нужны — ViewModel зовёт классы `domain` напрямую, как `SampleCalculator` ниже.

### 4. Состояние и ViewModel в `presentation/`

```kotlin
data class SampleState(val price: Int = 0, val count: Int = 1, val total: Long = 0)

sealed interface SampleEvent {
    data class Price(val value: Int) : SampleEvent
    data class Count(val value: Int) : SampleEvent
}

class SampleViewModel(private val calculator: SampleCalculator) : ViewModel() {
    private val _state = MutableStateFlow(SampleState())
    val state: StateFlow<SampleState> = _state.asStateFlow()

    fun onEvent(event: SampleEvent) {
        _state.update { old ->
            val new = when (event) {
                is SampleEvent.Price -> old.copy(price = event.value)
                is SampleEvent.Count -> old.copy(count = event.value)
            }
            new.copy(total = calculator.total(new.price.toLong(), new.count))
        }
    }
}
```

### 5. Экран: Route с ViewModel и Screen без неё

`Screen` получает только `state` и `onEvent`, поэтому его легко показать в превью и в каталоге.
UI строится из компонентов дизайн-системы (скилл `bdo-design-system`), строки и размеры — из ресурсов (скилл `resources-externalization`).

```kotlin
@Composable
fun SampleRoute(viewModel: SampleViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SampleScreen(state, viewModel::onEvent)
}

@Composable
fun SampleScreen(state: SampleState, onEvent: (SampleEvent) -> Unit) {
    // Страница без шапки (скилл bdo-design-system): колонка по центру или, если нужно всё место, ToolPage с fillsScreen = true
    Column(Modifier.widthIn(max = AppTheme.dimens.contentMaxWidth)) { /* ... */ }
}
```

Импорты: `org.koin.compose.viewmodel.koinViewModel`,
`androidx.lifecycle.compose.collectAsStateWithLifecycle`.

### 6. DI-модуль в `di/`

```kotlin
val sampleToolModule = module {
    factoryOf(::SampleCalculator)
    viewModelOf(::SampleViewModel)
}
```

Импорты: `org.koin.dsl.module`, `org.koin.core.module.dsl.factoryOf`, `org.koin.core.module.dsl.viewModelOf`.

### 7. Подключить фичу к приложению в `shared`

Фичи открываются из верхней навигации (`TopNavigation`). Навигация связана с адресом страницы:
вкладка `Portraits` с маршрутом `portraits` открывается по `#portraits`, кнопки браузера «Назад» и «Вперёд» работают.

1. `shared/build.gradle.kts` → `commonMain.dependencies`: добавь `implementation(project(":feature:<имя>"))`.
2. `shared/.../App.kt` → список `featureModules`: добавь `<имя>Module`.
3. `shared/.../navigation/AppTab.kt` → `enum class AppTab`: добавь вкладку (редактору на всё окно — `fillsScreen = true`) или переименуй
   свободную заглушку (`Tab2`…`Tab5`). `route` пиши в kebab-case, после релиза его не меняй: он попадает в ссылки.
   Название вкладки — строка `tab_<имя>` в `shared/src/commonMain/composeResources/values/strings.xml`
   и её перевод в `values-<tag>/strings.xml` каждого языка.
   Иконка вкладки обязательна (параметр `icon` в `AppTab`): нарисуй SVG `shared/src/commonMain/composeResources/drawable/tab_<имя>_icon.svg`
   по смыслу тулзы — белый контур `stroke="#FFFFFF"`, `stroke-width="2"`, `viewBox 0 0 24 24`, без игровых ассетов
   (пример — `tab_portraits_icon.svg`). `TopNavigation` ставит её слева от подписи и красит в цвет текста вкладки.
4. `shared/.../App.kt` → `TabContent`: в ветке своей вкладки вызови `<Имя>Route()` вместо `PlaceholderScreen`.

Если фиче нужны внутренние экраны (список → детали), добавь их в `sealed interface Destination` и
`destinationFromFragment`, а параметры передавай через `buildBrowserHistoryFragment(name, mapOf(...))`.

### 8. Проверка

```bash
./gradlew :feature:<имя>:compileKotlinWasmJs :feature:<имя>:compileKotlinJs :feature:<имя>:wasmJsBrowserTest
```

Затем запусти приложение (конфигурация `webApp-wasm` в `.claude/launch.json`) и открой вкладку фичи
в браузере: `http://localhost:8080/#<route>`. Пока dev-сервер запущен, отдельную сборку Gradle не запускай.

## Чек-лист перед тем, как считать фичу готовой

- [ ] код лежит в `feature/<имя>`, в `shared` добавлены только зависимость, Koin-модуль, вкладка с иконкой и ветка в `TabContent`;
- [ ] модуль зависит только от `:platform:composable`, без других фич;
- [ ] формулы в `domain/` покрыты тестами, и тесты проходят;
- [ ] UI собран из компонентов дизайн-системы и токенов `AppTheme`, строки лежат в `strings.xml`, размеры — в `*Dimens`;
- [ ] каждая строка переведена на все языки приложения (`values-<tag>/strings.xml`), экран проверен на каждом языке;
- [ ] слои по `clean-code`: в `domain` нет импортов из `data` и `presentation`; репозиторий — интерфейс
      в `domain` и `…RepositoryImpl` в `data`; ViewModel и контроллеры ходят только в интеракторы;
      файлы `domain` лежат в папках-темах;
- [ ] приложение собирается под wasmJs и js, страница открывается в браузере.
