---
name: bdo-design-system
description: Дизайн-система сайта BDO Tools в стиле окон Black Desert Online (модуль platform/uikit). Использовать при любой работе с UI — новая страница или тулза, новый компонент, правка цветов, отступов, шрифтов, вёрстка формы или калькулятора.
---

# Дизайн-система BDO Tools

Интерфейс повторяет **стиль** игровых окон Black Desert Online: тёмные графитовые панели,
бронзовая шапка окна, бежевый текст, золотые акценты. Мы берём только стиль — цвета, пропорции, форму.
Игровые иконки, текстуры и логотипы не копируем: это чужие ассеты.

Весь код находится в `platform/uikit/src/commonMain/kotlin/com/layzen/bdotools/uikit/`:

| Файл | Что внутри |
|---|---|
| `theme/AppColors.kt` | палитра `AppColors` и значения `DefaultAppColors` |
| `theme/AppTypography.kt` | текстовые стили `AppTypography` |
| `theme/AppDimens.kt` | отступы и размеры `AppDimens`, формы `AppShapes` |
| `theme/AppTheme.kt` | `AppTheme { }` и доступ к токенам через `AppTheme.colors / typography / dimens` |
| `components/*.kt` | готовые UI-компоненты |
| `showcase/ComponentShowcase.kt` | живой каталог всех компонентов (`ShowcaseDimens.kt` — его размеры) |
| `../../composeResources/values/strings.xml` | строки модуля, в том числе каталога |

Референсы (скриншоты окон меню, настроек и выбора персонажа) лежат локально в `.claude/references/ui/`
и в Git не попадают. Все значения из них уже перенесены в токены, поэтому для работы скриншоты не нужны.
Если они есть и задача про внешний вид, сверяйся с ними.

## Правила

1. **Не использовать `Bdo` в названиях.** Правило касается всего кода дизайн-системы: UI-компонентов,
   темы и её токенов, а также файлов, где они лежат.
   - Компонент называется по своей роли: `GameWindow`, `ActionButton`, `SectionHeader`, `TopNavigation`;
     файлы — `GameWindow.kt`, `Inputs.kt`.
   - Тема и токены в `theme/` начинаются с `App`: `AppTheme`, `AppColors`, `DefaultAppColors`,
     `AppTypography`, `appTypography()`, `AppDimens`, `AppShapes`, `LocalAppColors`; файлы — `AppTheme.kt`,
     `AppColors.kt`, `AppTypography.kt`, `AppDimens.kt`.
   - Не называй компоненты и тему так же, как в Material3 (`Button`, `Text`, `Slider`, `Switch`, `Divider`,
     `Badge`, `TextField`, `Typography`, `Shapes`, `ColorScheme`): иначе в файлах начнутся конфликты импортов.
2. **Только токены и ресурсы.** Никаких `Color(0xFF…)`, `16.dp`, `fontSize = …` и текстовых литералов в
   компонентах и фичах. Цвет берётся из `AppTheme.colors`, отступы и размеры — из `AppTheme.dimens` или из
   `*Dimens`-файла, текст — из `strings.xml`. Подробно — в скилле `resources-externalization`.
   Если нужного токена нет, добавь его в тему, а не в фичу.
3. **Сначала готовый компонент.** Material3-компоненты (`Button`, `Card`, `TextField`, `Switch`, `Slider`) в фичах
   не используются: они не похожи на игру. Исключение — `Text` со стилем из `AppTheme.typography`.
4. **Почти прямые углы.** `AppShapes.Sharp` (0dp) или `AppShapes.Small` (2dp). `AppShapes.Pill` — только для
   плашек NEW/HOT и переключателя. `AppShapes.MenuItem` (4dp) — подсветка пункта в меню действий.
5. **Золото — это сигнал, а не украшение.** `accentGold` используется для активного пункта навигации, текущего
   значения, результата расчёта, «новых» разделов и имени персонажа. Обычный текст бежевый или кремовый.
6. **Кнопки всегда серые.** Главное действие выделяется не цветом, а местом: оно стоит последним справа
   в `WindowFooter` (порядок «Применить — Назад — OK»).
7. **Выделение = коричневая подложка + рамка.** Выбранный пункт меню или вариант получает `backgroundSelected` и
   `borderSelected`, выбранная плитка — рамку `accentGoldPale`.
8. **Жирный шрифт по умолчанию.** Почти весь текст в игре жирный. Обычный вес (`body`) — только для длинных абзацев.
9. **Без символов-иконок в тексте.** Шрифт web-сборки по умолчанию содержит латиницу и кириллицу, но не
   содержит `✕ ▼ ⚙ ♪` и эмодзи: они показываются пустыми квадратами. Иконки рисуй через `Canvas`/`Path`
   (пример — стрелка в `DropdownSelect`) или клади векторные XML в `composeResources/drawable`.

## Палитра

| Токен | HEX | Где используется |
|---|---|---|
| `backgroundDeep` | `#18181A` | фон страницы, тело окна |
| `backgroundSurface` | `#222226` | боковая навигация, невыбранные варианты |
| `backgroundRaised` | `#2B2B2F` | панели, пункты меню, футер |
| `backgroundItem` | `#34343A` | ховер пункта, иконки-кнопки, поле dropdown |
| `backgroundInput` | `#1E1E21` | поля ввода и поиска |
| `backgroundSelected` | `#4F433C` | выбранный пункт или вариант |
| `titleBar` → `titleBarDark` | `#775D3A` → `#5E4A2E` | градиент шапки окна |
| `titleBarHighlight` | `#C0A850` | золотая кромка над шапкой |
| `button` / `buttonHover` / `buttonPressed` | `#44444E` / `#52525D` / `#38383F` | кнопки |
| `buttonBorder` | `#4E4F56` | рамка кнопки |
| `border` / `borderSelected` | `#39393F` / `#685048` | рамки пунктов |
| `divider` | `#353536` | разделители секций |
| `track` | `#101011` | дорожка слайдера |
| `textPrimary` | `#F8F0D8` | заголовки окон, пункты меню, значения |
| `textBeige` | `#E0C8A0` | заголовки секций, навигация, горячие клавиши |
| `textMuted` | `#A89068` | пояснения под заголовками, плейсхолдеры |
| `textDisabled` | `#686868` | неактивные пункты и варианты |
| `textButton` | `#E8E8E8` | текст на кнопках |
| `accentGold` | `#F8C038` | активное, текущее значение, важное |
| `accentGoldPale` | `#D8B080` | рамка выбранной плитки |
| `danger` | `#D86048` | предупреждения, ошибки |
| `badgeNew` / `badgeHot` | `#D09820` / `#D05020` | плашки NEW / HOT |

## Типографика

| Стиль | Размер, вес | Для чего |
|---|---|---|
| `windowTitle` | 16sp bold | шапка окна |
| `menuItem` | 15sp bold | пункты главного меню, вкладки верхней навигации |
| `sectionTitle` | 14sp bold | заголовки секций, навигация |
| `value` | 16sp bold | крупные числа и результаты (`ValueText`) |
| `bodyBold` | 13sp bold | основной текст, значения в полях |
| `body` | 13sp normal | длинные абзацы |
| `button` | 13sp bold | кнопки |
| `caption` | 11sp bold | пояснения, предупреждения |
| `badge` | 10sp bold | NEW / HOT |

Цвет в стили не заложен: его задаёт компонент.

## Компоненты

Компоненты принимают уже готовый текст (`String`). Строку из ресурсов фича получает сама через
`stringResource(...)`. Параметры `label` у списков — `@Composable (T) -> String`, поэтому внутри них
тоже можно звать `stringResource`.

| Компонент | Когда использовать |
|---|---|
| `TopNavigation(tabs, selected, onSelect, label, title)` | верхняя навигация сайта; используется один раз, в `shared/App.kt` |
| `GameWindow(title, actions, footer) { }` | основной контейнер каждой тулзы: шапка, тело и необязательный футер |
| `WindowTitleBar`, `WindowFooter`, `Panel` | части окна, если нужна нестандартная компоновка |
| `ModalWindow(title, onDismissRequest, dismissible, footer) { }` | модальное окно поверх страницы: выбор, подтверждение, редактор. Пока идёт долгая операция, передавай `dismissible = false` |
| `CloseButton(onClick)` | крестик (нарисован линиями): в шапке окна и для очистки поля |
| `InfoButton(text)` | кружок с «!»; по клику над ним подсказка с `text` — ограничения поля, правила |
| `HoverActionsBox(actions = { ActionMenuItem(...) }) { }` | действия над элементом (плиткой) по наведению курсора: элемент размывается (`hoverBlurRadius`) и затемняется, поверх встаёт чёткая золотая рамка, пункты — по центру внутри элемента и не перекрывают соседей. Подсвечивать элемент самому не нужно. Всплывающих меню-тултипов в дизайн-системе нет: действия показываются внутри элемента |
| `RunningGoblin()`, `LoadingRow(text)` | индикатор загрузки — чёрный гоблин со светлой обводкой бежит на месте (цвета `silhouette`, `silhouetteOutline`); строка «гоблин + текст» для короткого ожидания внутри окна |
| `ProgressBar(progress)` | полоса прогресса 0…1 |
| `BusyOverlay(text, progress, details)` | окно ожидания поверх страницы на время долгой операции, закрыть нельзя. В браузере всё работает в одном потоке: между тяжёлыми шагами делай короткую паузу (`delay(16)`), иначе индикатор замрёт вместе со страницей |
| `ActionMenuItem(text, onClick)`, `ActionMenuDivider()` | пункт меню действий и разделитель между пунктами. Кликается вся строка, подсветка — скруглённая плашка `AppShapes.MenuItem` с отступом от разделителей. Длинный текст переносится на вторую строку |
| `ActionButton(text, onClick)` | любое действие |
| `SquareIconButton(onClick, selected) { }` | квадратная кнопка с иконкой (панель инструментов) |
| `MenuItem(text, hotkey, badge, selected, highlighted)` | список разделов или тулз, как главное меню игры |
| `NavGroup(title, icon) { NavItem(...) }` | боковая навигация по разделам страницы |
| `SectionHeader(title, description, warning)` | заголовок блока настроек или параметров |
| `DescriptionText`, `WarningText`, `ValueText` | пояснение, предупреждение, крупное золотое значение |
| `SectionDivider` | разделитель между секциями |
| `LabelBadge(text, kind)` | NEW / HOT (`LabelBadgeKind.New` / `Hot`) |
| `OptionGroup(options, selected, onSelect, label)` | выбор одного из 2–4 вариантов |
| `DropdownSelect(selected, options, onSelect, label)` | выбор из длинного списка |
| `OptionsMenu(expanded, onDismissRequest, options, selected, onSelect, label, icon)` | всплывающий список вариантов, привязанный к любому элементу (ставится в тот же `Box`): например, выбор по клику на иконку. `DropdownSelect` построен на нём |
| `ValueSlider(value, onValueChange, range)` | целое число в диапазоне, где важна наглядность |
| `NumberField(value, onValueChange, range)` | точный ввод числа (фейлстак, цена, количество) |
| `InputField(value, onValueChange, placeholder, leading, trailing)` | текст и поиск; `trailing` — кнопки в конце поля, например `CloseButton` для очистки и `InfoButton` с ограничениями |
| `ToggleSwitch(checked, onCheckedChange, label, description)` | включить или выключить опцию |
| `CheckBox(checked, onCheckedChange)` | отметка элементов списка для массового действия (например, удаления). Внутри строки `MenuItem` передавай его в слот `icon` |
| `Tile(selected, onClick) { }` | плитка в сетке: предметы, классы, персонажи |

## Как собрать страницу тулзы

Корень приложения обёрнут в `AppTheme { }`. Типовая страница-калькулятор:

```kotlin
@Composable
fun EnhancementScreen(state: EnhancementState, onEvent: (EnhancementEvent) -> Unit) {
    GameWindow(
        title = stringResource(Res.string.enhancement_title),
        modifier = Modifier.widthIn(max = AppTheme.dimens.contentMaxWidth),
        footer = {
            ActionButton(stringResource(Res.string.enhancement_reset), onClick = { onEvent(EnhancementEvent.Reset) })
            ActionButton(stringResource(Res.string.enhancement_calculate), onClick = { onEvent(EnhancementEvent.Calculate) })
        },
    ) {
        Column(
            Modifier.padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            SectionHeader(
                stringResource(Res.string.enhancement_item),
                description = stringResource(Res.string.enhancement_item_description),
            )
            DropdownSelect(state.item, state.items, { onEvent(EnhancementEvent.SelectItem(it)) }, { it.name })
            SectionDivider()
            SectionHeader(stringResource(Res.string.enhancement_failstack))
            NumberField(state.failstack, { onEvent(EnhancementEvent.Failstack(it)) }, range = 0..400)
            SectionDivider()
            SectionHeader(stringResource(Res.string.enhancement_chance))
            ValueText(stringResource(Res.string.enhancement_chance_value, state.chance))
        }
    }
}
```

Компоновка как в окне настроек игры: слева `NavGroup` шириной `AppTheme.dimens.sidebarWidth` на
`backgroundSurface`, справа контент с отступом `spaceL`, секции разделены `SectionDivider`.
Между элементами внутри секции — `spaceM`.

## Как добавить или изменить компонент

1. Положи его в `platform/uikit/.../components/`, назови по роли без `Bdo` (см. правило 1)
   и собирай только из токенов темы. Новые размеры — в `AppDimens`.
2. Состояние наружу (`value` + `onValueChange`), внутри — только визуальное: hover, expanded.
3. Курсор-рука на всём кликабельном: `Modifier.pointerHoverIcon(PointerIcon.Hand)`. Ripple отключён
   (`indication = null`), а реакция на наведение — через `collectIsHoveredAsState()` (подложка или рамка светлеет).
4. Добавь пример в `ComponentShowcase.kt`, его тексты — в `strings.xml` модуля uikit с префиксом `showcase_`.
5. Проверь в браузере: запусти конфигурацию `webApp-wasm` из `.claude/launch.json`
   (`:webApp:wasmJsBrowserDevelopmentRun --continuous`, сервер на `http://localhost:8080`) и открой каталог
   по адресу `http://localhost:8080/#showcase` (во вкладках его нет). Сделай скриншот и сравни со скриншотами
   в `.claude/references/ui/`, если они есть. Пока dev-сервер запущен, не запускай отдельную сборку Gradle:
   они пишут в одни и те же папки `build/` и ломают друг другу результат.
6. Если меняешь токен, обнови таблицу в этом файле.
