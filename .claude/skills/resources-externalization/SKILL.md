---
name: resources-externalization
description: Правило проекта BDO Tools — строки, размеры и отступы не пишутся прямо в коде, а выносятся в отдельные файлы (strings.xml, *Dimens.kt) и читаются оттуда. Использовать при написании или правке любого Compose-кода, при добавлении текста на экран, нового отступа или размера, и при ревью UI-кода.
---

# Строки, размеры и отступы — только из отдельных файлов

В Compose-коде нет текстовых литералов, видимых пользователю, и нет чисел с `.dp` / `.sp`.
Значения **записываются** в отдельные файлы и **читаются** оттуда:

| Что | Куда записывать | Как читать |
|---|---|---|
| Текст, который видит пользователь | `src/commonMain/composeResources/values/strings.xml` своего модуля | `stringResource(Res.string.<ключ>)` |
| Общие отступы и размеры компонентов | `platform/uikit/.../theme/AppDimens.kt` | `AppTheme.dimens.<имя>` |
| Размеры, нужные только одному экрану или фиче | `<Имя>Dimens.kt` рядом с экраном | `<Имя>Dimens.<имя>` |
| Размеры шрифтов | `platform/uikit/.../theme/AppTypography.kt` | `AppTheme.typography.<стиль>` |

Почему так:
- текст собран в одном месте, его легко вычитать и перевести (`values-en/strings.xml`);
- отступы одинаковые по всему сайту и меняются в одном месте;
- в коде экрана видна структура, а не цифры.

## Строки

### Запись

Каждый модуль с UI хранит свои строки сам: `platform/uikit`, `shared`, `feature/<имя>`.

```xml
<!-- feature/portraitConstructor/src/commonMain/composeResources/values/strings.xml -->
<resources>
    <string name="portraits_title">Портреты</string>
    <string name="portraits_level">Ур.%1$d %2$s</string>
</resources>
```

- Ключ в snake_case, начинается с имени фичи или экрана: `portraits_title`, `showcase_menu_recent`.
- Параметры — через `%1$d`, `%1$s`, а не склейкой строк в коде.
- Апостроф и кавычки экранируй: `\'`, `\"`.

Пакет класса `Res` задаётся в `build.gradle.kts` модуля:

```kotlin
compose.resources {
    packageOfResClass = "com.layzen.bdotools.<модуль>.resources"
    generateResClass = always
}
```

`generateResClass = always` обязателен в модулях фич: библиотека ресурсов приходит в них транзитивно
через `:platform:composable`, и без этой строки класс `Res` не генерируется (ошибка `Unresolved reference 'Res'`).

Уже настроено: `com.layzen.bdotools.uikit.resources`, `com.layzen.bdotools.resources` (shared),
`com.layzen.bdotools.feature.portraitconstructor.resources`. После добавления строки класс `Res` генерируется
при сборке; если IDE его не видит, запусти `./gradlew :<модуль>:generateComposeResClass`.

### Чтение

В `@Composable`:

```kotlin
SectionHeader(stringResource(Res.string.portraits_grid_title, state.portraits.size))
ValueText(stringResource(Res.string.portraits_level, state.level, state.name))
```

Импорты: `org.jetbrains.compose.resources.stringResource` и `com.layzen.bdotools.<модуль>.resources.*`
(`Res` и сгенерированные ключи, например `...resources.portraits_title`).

Вне `@Composable` (enum, State, ViewModel) хранится **ключ**, а не текст:

```kotlin
enum class AppTab(val route: String, val title: StringResource) {
    Portraits("portraits", Res.string.tab_portraits),
}
```

Текст получается в UI: `stringResource(tab.title)`. Если строка нужна в корутине — `getString(Res.string.x)`.

Компоненты дизайн-системы принимают готовый `String`, а параметры `label` у списков объявлены как
`@Composable (T) -> String`, поэтому в них можно писать `{ stringResource(it.title) }`.

### Что не выносится

- Технические строки, которые пользователь не видит: маршруты (`"portraits"`), ключи, логи, имена в `Destination`.
- Числа и данные из state, которые подставляются в шаблон строки.

## Размеры и отступы

### Запись

Общие значения — в `AppDimens`, сгруппированные комментариями:

```kotlin
@Immutable
data class AppDimens(
    // Отступы
    val spaceS: Dp = 8.dp,
    // Переключатель
    val toggleWidth: Dp = 28.dp,
    ...
)
```

Размеры одного экрана — в отдельном объекте рядом с ним:

```kotlin
// feature/portraitConstructor/.../presentation/PortraitsDimens.kt
internal object PortraitsDimens {
    val portraitSize = 92.dp
    val gridSpacing = 4.dp
}
```

Если значение понадобилось второму экрану или фиче, перенеси его в `AppDimens`.

### Чтение

```kotlin
Modifier.padding(AppTheme.dimens.spaceL)
Modifier.size(PortraitsDimens.portraitSize)
Modifier.widthIn(max = AppTheme.dimens.contentMaxWidth)
```

### Что можно писать прямо в коде

- `0.dp` и `Dp.Unspecified`.
- Безразмерные числа: `weight(1f)`, `alpha = 0.5f`, `fillMaxWidth(0.7f)`, угол `rotate(45f)`.
- Литералы `.dp` и `.sp` внутри самих файлов `*Dimens.kt` и `AppTypography.kt`.

## Проверка

Перед тем как закончить работу с UI, найди литералы, которые остались в коде:

```bash
grep -rnE "[0-9]+\.(dp|sp)\b" --include=*.kt platform shared feature | grep -vE "Dimens\.kt|Typography\.kt"
```

```bash
grep -rnE "Text\(\"|text = \"|title = \"|description = \"|placeholder = \"|label = \"" --include=*.kt platform shared feature
```

Оба поиска должны вернуть пустой результат или только места из списка «что можно писать прямо в коде».
