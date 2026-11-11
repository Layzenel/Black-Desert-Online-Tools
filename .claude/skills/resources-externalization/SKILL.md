---
name: resources-externalization
description: Правило проекта BDO Tools — строки, размеры и отступы не пишутся прямо в коде, а выносятся в отдельные файлы (strings.xml, *Dimens.kt) и читаются оттуда; приложение многоязычное (русский, английский, список расширяемый), и каждая строка переводится на все его языки. Использовать при добавлении, правке или удалении строки интерфейса, при добавлении нового языка, при написании или правке любого Compose-кода, при добавлении текста на экран, нового отступа или размера, и при ревью UI-кода.
---

# Строки, размеры и отступы — только из отдельных файлов

В Compose-коде нет текстовых литералов, видимых пользователю, и нет чисел с `.dp` / `.sp`.
Значения **записываются** в отдельные файлы и **читаются** оттуда:

| Что | Куда записывать | Как читать |
|---|---|---|
| Текст, который видит пользователь | `src/commonMain/composeResources/values/strings.xml` своего модуля и такой же файл в `values-<язык>/` для каждого языка приложения (раздел «Языки») | `stringResource(Res.string.<ключ>)` |
| Общие отступы и размеры компонентов | `platform/uikit/.../theme/AppDimens.kt` | `AppTheme.dimens.<имя>` |
| Размеры, нужные только одному экрану или фиче | `<Имя>Dimens.kt` рядом с экраном | `<Имя>Dimens.<имя>` |
| Размеры шрифтов | `platform/uikit/.../theme/AppTypography.kt` | `AppTheme.typography.<стиль>` |

Почему так:
- текст собран в одном месте, его легко вычитать и перевести: приложение многоязычное, и строка
  в коде осталась бы без перевода;
- отступы одинаковые по всему сайту и меняются в одном месте;
- в коде экрана видна структура, а не цифры.

## Строки

### Запись

Каждый модуль с UI хранит свои строки сам: `platform/uikit`, `shared`, `desktopApp`, `feature/<имя>`.
Русский текст — в `values/strings.xml`, перевод с тем же ключом — в `values-en/strings.xml` и в папке
каждого следующего языка:

```xml
<!-- feature/portraitConstructor/src/commonMain/composeResources/values/strings.xml -->
<resources>
    <string name="portraits_title">Портреты</string>
    <string name="portraits_level">Ур.%1$d %2$s</string>
</resources>
```

```xml
<!-- feature/portraitConstructor/src/commonMain/composeResources/values-en/strings.xml -->
<resources>
    <string name="portraits_title">Portraits</string>
    <string name="portraits_level">Lv.%1$d %2$s</string>
</resources>
```

- Ключ в snake_case, начинается с имени фичи или экрана: `portraits_title`, `showcase_menu_recent`.
- Параметры — через `%1$d`, `%1$s`, а не склейкой строк в коде.
- Апостроф и кавычки пиши как есть (`don't`, `"Documents"`), без `\'` и `\"`: ресурсы Compose их не
  разэкранируют, и обратный слэш попадёт на экран. Сам обратный слэш — `\\`, угловые скобки — `&lt;` `&gt;`.

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

- Технические строки, которые пользователь не видит: маршруты (`"portraits"`), ключи, логи, имена в `Destination`,
  сообщения `require`/`check`.
- Числа и данные из state, которые подставляются в шаблон строки.
- Названия языков на них самих (`Русский`, `English`): они не переводятся и лежат в `AppLanguage.nativeName`.
- Имена файлов и папок игры и тулзы (`FaceTexture`, `gamevariable.xml`, `BDO Tools`) — одинаковы на всех языках
  и вписываются в строку как есть.

## Языки

Язык интерфейса пользователь выбирает кнопкой в верхней навигации (`LanguageSwitcher` в `shared`). Сейчас языков
два — русский и английский, но их число не зашито: код не должен знать, сколько их и какие они.

| Что | Где |
|---|---|
| Список языков | `AppLanguage` в `platform/core` (`core.language`): код `tag` и название `nativeName` |
| Текущий язык и его выбор | `AppLanguages` (Koin, один на приложение): сохранённый → язык системы → `AppLanguage.Fallback` |
| Где помнится выбор | `LanguageSettings`: на сайте `localStorage`, на Desktop `Preferences` (`GameFolderSettings`) |
| Применение к ресурсам | `LanguageEnvironment` в `shared`: ставит язык платформе и пересобирает содержимое |
| Строки языка | `composeResources/values-<tag>/strings.xml` в каждом модуле с UI |

Правила:

- **`values/` — русский и он же запасной.** Строки пишутся сначала туда; ключ, которого нет в `values-<tag>/`,
  покажется по-русски. Поэтому новая или изменённая строка в `values/` — в том же изменении правка во **всех**
  `values-<tag>/` этого модуля. Удалил или переименовал ключ — сделай это во всех языках.
- **Файлы языков зеркальны:** те же ключи в том же порядке, те же плейсхолдеры (`%1$d`, `%2$s`) с теми же
  номерами и типами, те же `\n`. Комментарии в файле — на языке файла.
- **Не склеивай фразы из кусков** и не полагайся на порядок слов: `"Удалить " + name` в другом языке не сложится.
  Вся фраза — одна строка с плейсхолдерами; переводчик сам переставит их.
- **Количества — через `<plurals>`** и `pluralStringResource`. У языков разные формы: в русском `one`, `few`,
  `many`, `other`, в английском только `one` и `other`. В каждом файле — формы своего языка.
- **Не выбирай текст по языку в коде** (`if (language == Russian)`). Различия — только в ресурсах.
  Формат дат, чисел и единиц, который зависит от языка, — тоже строка-шаблон в `strings.xml`.
- **Длина текста.** Перевод бывает на треть длиннее. Подписи кнопок, вкладок и пунктов меню должны влезать
  в одну строку на каждом языке (правило 11 в `bdo-design-system`): не задавай ширину «под русский текст»,
  а слишком длинный перевод сокращай.
- **Термины игры** — как в клиенте BDO на этом языке (названия классов, окон, настроек), а не дословный перевод.
- **Состояние и смена языка.** При смене языка всё внутри `LanguageEnvironment` собирается заново, и состояние
  из `remember` сбрасывается. Данные экрана держи во ViewModel; готовый текст в state не храни — только ключ
  (`StringResource`) и параметры, иначе после смены языка он останется на старом.

### Как добавить язык

1. Элемент в `AppLanguage`: `German("de", "Deutsch")`. Кнопка выбора и сохранение подхватят его сами.
2. Папка `values-<tag>/strings.xml` во всех модулях, где есть `values/strings.xml`
   (`platform/uikit`, `shared`, `desktopApp`, каждый `feature/<имя>`), с полным переводом.
3. Шрифт: встроенный шрифт web-сборки содержит латиницу и кириллицу. Для языка с другой письменностью
   (корейский, японский, китайский) сначала подключи шрифт с его знаками, иначе вместо текста будут квадраты.
4. Проверь в браузере и в настольной версии: переключение, длинные подписи, окна с параметрами в строках.
5. Допиши язык в README (скилл `readme-sync`).

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

Ключи во всех языках совпадают — команда ничего не печатает (иначе показывает ключи, которые есть только
в одном из файлов):

```bash
for base in $(git ls-files '*/composeResources/values/strings.xml'); do for tr in "${base%/values/strings.xml}"/values-*/strings.xml; do diff <(grep -oE 'name="[^"]+"' "$base" | sort) <(grep -oE 'name="[^"]+"' "$tr" | sort) > /dev/null || { echo "$tr"; diff <(grep -oE 'name="[^"]+"' "$base" | sort) <(grep -oE 'name="[^"]+"' "$tr" | sort); }; done; done
```
