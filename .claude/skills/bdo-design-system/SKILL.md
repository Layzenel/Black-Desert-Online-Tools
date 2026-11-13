---
name: bdo-design-system
description: Дизайн-система сайта BDO Tools в стиле окон Black Desert Online (модуль platform/uikit). Использовать при любой работе с UI — новая страница или тулза, новый компонент, правка цветов, отступов, шрифтов, вёрстка формы или калькулятора.
---

# Дизайн-система BDO Tools

Интерфейс повторяет **стиль** игровых окон Black Desert Online: тёмные графитовые панели,
бронзовая шапка окна, бежевый текст, золотые акценты. Мы берём только стиль — цвета, пропорции, форму.
Игровые иконки, текстуры и логотипы сами не рисуем и не копируем: это чужие ассеты. Исключение — иконки классов и жизненных навыков,
которые пользователь положил сам (см. `icons/CharacterClassIcons.kt`, `icons/LifeSkillIcons.kt`).

Весь код находится в `platform/uikit/src/commonMain/kotlin/com/layzen/bdotools/uikit/`:

| Файл | Что внутри |
|---|---|
| `theme/AppColors.kt` | палитра `AppColors` и значения `DefaultAppColors` |
| `theme/AppTypography.kt` | текстовые стили `AppTypography` |
| `theme/AppDimens.kt` | отступы и размеры `AppDimens`, формы `AppShapes` |
| `theme/AppTheme.kt` | `AppTheme { }` и доступ к токенам через `AppTheme.colors / typography / dimens` |
| `components/*.kt` | готовые UI-компоненты |
| `icons/CharacterClassIcons.kt` | иконки и названия классов: `CharacterClass.icon` (SVG, белый силуэт `viewBox 0 0 64 64`, цвет — через `ColorFilter.tint`) и `CharacterClass.title`. Сам `CharacterClass` лежит в `platform/core` (`core.game`) |
| `icons/LifeSkillIcons.kt` | то же для жизненных навыков: `LifeSkill.icon` и `LifeSkill.title` (алхимия, бартер, изготовление, кулинария, охота, рыбалка, сбор, судоходство, торговля, укрощение, урожай). Сам `LifeSkill` лежит в `platform/core` (`core.game`) |
| `showcase/ComponentShowcase.kt` | живой каталог всех компонентов (`ShowcaseDimens.kt` — его размеры) |
| `../../composeResources/values/strings.xml`, `values-<tag>/strings.xml` | строки модуля, в том числе каталога, на русском и на остальных языках приложения |

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
6. **Кнопки всегда серые.** Главное действие выделяется не цветом, а местом: оно стоит последним —
   справа в строке действий или `WindowFooter` (порядок «Применить — Назад — OK»), внизу в `sidebarFooter` у `ToolPage`.
7. **Выделение = коричневая подложка + рамка.** Выбранный пункт меню или вариант получает `backgroundSelected` и
   `borderSelected`, выбранная плитка — рамку `accentGoldPale`.
8. **Жирный шрифт по умолчанию.** Почти весь текст в игре жирный. Обычный вес (`body`) — только для длинных абзацев.
9. **Без символов-иконок в тексте.** Шрифт web-сборки по умолчанию содержит латиницу и кириллицу, но не
   содержит `✕ ▼ ⚙ ♪` и эмодзи: они показываются пустыми квадратами. Иконки рисуй через `Canvas`/`Path`
   (пример — стрелка в `DropdownSelect`) или клади SVG в `composeResources/drawable` своего модуля
   (белый контур `stroke="#FFFFFF"`, `viewBox 0 0 24 24`; пример — `menu_*.svg` в `feature/portraitConstructor`).
10. **У страниц тулз нет шапки окна.** Бронзовая шапка `GameWindow` с крестиком остаётся только у модальных окон
    (`ModalWindow`). Страница бывает двух видов:
    - **редактору нужно всё место** (холст слотов) — `ToolPage` на всё окно (вкладка с `fillsScreen = true`
      в `AppTab`): параметры в меню слева, главные действия внизу меню (`sidebarFooter`), правка — в `ActionBar`;
    - **содержимое своего размера** (сетка портретов) — колонка по центру страницы с ограниченной шириной
      (`widthIn(max = …)`) на подложке `ContentFrame`: заголовок секции, содержимое и строка `MenuAction` под ним,
      каждому `Modifier.weight(1f)`,
      главное действие последним справа.
11. **Подписи пунктов меню и кнопок — в одну строку.** Если подпись не влезает, её сокращают («Загрузить»
    вместо «Загрузить изображение»), а не переносят. `MenuAction` обрезает лишнее многоточием.
    Подробности действия — в описании секции (`SectionHeader(description = …)`).
12. **Вёрстка не зависит от языка.** Интерфейс переводится (скилл `resources-externalization`, раздел «Языки»),
    и перевод бывает длиннее русского текста. Не подгоняй ширину кнопки, колонки или окна под русскую подпись:
    пусть элемент растягивается по содержимому или обрезает его многоточием. Каждый экран смотри на всех
    языках приложения. Знаки новой письменности (не латиница и не кириллица) требуют своего шрифта — см. правило 9.

Расположение частей редактора берём с garmoth.com (сайт тулз для BDO): без шапки, меню слева, инструменты
над рабочей областью, рабочая область на всё оставшееся место. Цвета и стиль — свои, из этой системы.
Скриншоты страниц garmoth лежат локально в `.claude/references/ui/garmoth/` (в Git не попадают).

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
| `textNote` | `#E0783C` | заметка персонажа — оранжевая подпись над именем, как в окне «Персонажи аккаунта» игры |
| `accentGold` | `#F8C038` | активное, текущее значение, важное |
| `accentGoldPale` | `#D8B080` | рамка выбранной плитки |
| `danger` | `#D86048` | предупреждения, ошибки |
| `accentBlue` / `accentRed` | `#4F8CD8` / `#C83C3C` | цветовые метки категорий (группы элементов в редакторе слотов); не для кнопок и не для ошибок |
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
| `TopNavigation(tabs, selected, onSelect, label, icon, title, trailing)` | верхняя навигация сайта; используется один раз, в `shared/App.kt`. У вкладки иконка `icon` (белый силуэт, красится в цвет подписи) и справа от неё подпись до двух строк, выровненная по левому краю (перенос `\n` в строке ресурса). `trailing` — элементы у правого края (версия, выбор аккаунта, на сайте — `ToolbarButton` скачивания настольной версии) |
| `ToolPage(sidebar, sidebarWidth, sidebarFooter) { }` | страница тулзы без шапки: меню слева во всю высоту (`backgroundSurface`, прокручивается само, отступ `spaceL`, между частями `spaceM`) и содержимое справа на всё свободное место. `sidebarFooter` прижат к низу меню и не прокручивается — для главных действий. Годится и внутри большого `ModalWindow` (превью импорта в слотах) |
| `ActionBar { }` | строка инструментов правки над содержимым `ToolPage` (отменить, повторить — в подсказке горячая клавиша). Работа с файлами и главное действие — внизу меню, в `sidebarFooter` |
| `ToolbarButton(icon, description, onClick, enabled, selected)`, `ToolbarDivider()` | панель инструментов в `ActionBar`: кнопка-иконка (белый силуэт, цвет задаёт кнопка) с подсказкой `description` при наведении; `selected` — включённый режим (золотая иконка). `ToolbarDivider` отделяет группы кнопок. Подпись у иконки только в подсказке, поэтому она обязательна |
| `HoverTooltip(text) { }` | всплывающая подсказка под элементом при наведении курсора, как у `ToolbarButton` (на нём и построена). Для пояснения, которому не место под подписью: например, у `ToggleSwitch` в узком меню. Снизу не влезает — встаёт над элементом |
| `HoverTooltip(tooltip = { }, enabled) { }` | подсказка с любым содержимым (карточка навыка): появляется сразу, пропадает, как только курсор ушёл или элемент нажали, по таймеру не гаснет. `enabled = false` прячет её, не пересоздавая элемент — можно выключать посреди жеста или по клавише-модификатору |
| `TooltipPanel { }` | подложка подсказки, как тултип игры: тело `backgroundDeep` в двойной рамке (внешняя `borderSelected`, зазор `tooltipFrameGap`, внутренняя `border`) и тень `menuShadow` — для своей подсказки, например в `aside` у `ModalWindow` |
| `TooltipSection(title) { }` | блок внутри большой подсказки, как разделы тултипа навыка в игре: плашка `backgroundSurface` в тонкой рамке на всю ширину с бежевым заголовком |
| `ColorPalette(colors, selected, onSelect, defaultColor, defaultLabel)` | выбор цвета из готовых: квадраты `swatchSize` в одну строку (без места сжимаются, не переносятся), первый — цвет по умолчанию (`null`), выбранный в золотой рамке. Сами цвета — данные фичи (например, цвет заметки на портрете), а не токены темы |
| `ContextMenu<K>(expanded, onDismissRequest, submenu = { key -> ... }) { submenuTrigger -> ActionMenuItem(...) }`, `Modifier.onSecondaryClick(onClick)` | меню правого клика: `onSecondaryClick` отдаёт точку нажатия и поглощает его до дочерних элементов, `ContextMenu` ставится в пустой `Box` с `offset` в этой точке и раскрывается от его угла (у правого или нижнего края окна — влево или вверх), после чего стоит на месте. Вложенные панели выезжают справа (у правого края — слева), пока курсор над пунктом с `Modifier` из `submenuTrigger(ключ)` или над панелью (открыта одна); панель встаёт напротив своего пункта, у нижнего края сдвигается вверх сама, не двигая меню. Нажатие мимо меню закрывает его и доходит до страницы: правый клик в другом месте сразу открывает там новое меню (сбрасывай точку меню в `onDismissRequest`, только если она не сменилась). Пункты — `ActionMenuItem` (недоступные с `enabled = false`) и `ActionMenuDivider` |
| `Modifier.panCursor(cursor)`, `PanCursor` | курсор-рука над рабочей областью: `Open` — сдвиг возможен, `Grabbing` — область схвачена. На Desktop руки нарисованы в коде (`HandCursor.kt`), в вебе включается CSS `grab`/`grabbing` для всего холста Compose, поэтому не-`None` ставь, только пока курсор над областью |
| `Modifier.resizeCursor(dragging)` | курсор «диагональные стрелки» (↗↙) над ручкой растягивания окна за правый верхний угол; `dragging = true` держит его, пока ручку тянут. На Desktop — системный курсор, в вебе — CSS `nesw-resize` для холста Compose |
| `MenuAction(text, onClick, icon, hotkey, selected, enabled)` | действие-пункт: иконка (`Painter`, белый силуэт), подпись в одну строку с многоточием, горячая клавиша справа. В меню `ToolPage` — столбиком, под содержимым страницы — строкой (`Modifier.weight(1f)` каждому, как в портретах). `selected` — включённый режим (золотой текст), его пункт выключает режим |
| `ContentFrame { }` | подложка содержимого страницы без шапки: графитовое тело окна (`backgroundSurface`), чёрная рамка и золотая кромка `titleBarHighlight` сверху; внутри колонка с отступом `spaceL` и промежутками `spaceM`. Для страницы «по центру» (портреты): заголовок, сетка и строка `MenuAction` — внутри |
| `ToolIntro(points, image, imageDescription)` | что умеет тулза — под просьбой выбрать папку, пока данных нет: пункты с золотым тире и скриншот тулзы (`drawable/intro_*.jpg`, сжатый JPEG ~1100 px; перевод — в `drawable-<tag>/`). Пустой экран с одной кнопкой не оставляй |
| `GameWindow(title, actions, footer) { }` | окно с бронзовой шапкой: основа `ModalWindow` и примеров каталога. Для страниц тулз — `ToolPage` |
| `WindowTitleBar`, `WindowFooter`, `Panel` | части окна, если нужна нестандартная компоновка |
| `ModalWindow(title, onDismissRequest, dismissible, footer, aside) { }` | модальное окно поверх страницы: выбор, подтверждение, редактор. Пока идёт долгая операция, передавай `dismissible = false`. `aside` — панель слева от окна вровень с его верхом (описание пункта, на который навели); окно из-за неё не сдвигается |
| `CloseButton(onClick)` | крестик (нарисован линиями): в шапке окна и для очистки поля |
| `InfoButton(text)` | кружок с «!»; по клику над ним подсказка с `text` — ограничения поля, правила |
| `HoverActionsBox(actions = { ActionMenuItem(...) }) { }` | действия над элементом (плиткой) по наведению курсора: элемент размывается (`hoverBlurRadius`) и затемняется, поверх встаёт чёткая золотая рамка, пункты — по центру внутри элемента и не перекрывают соседей. Подсвечивать элемент самому не нужно. Всплывающих меню-тултипов в дизайн-системе нет: действия показываются внутри элемента |
| `RunningGoblin()`, `LoadingRow(text)` | индикатор загрузки — чёрный гоблин со светлой обводкой бежит на месте (цвета `silhouette`, `silhouetteOutline`); строка «гоблин + текст» для короткого ожидания внутри окна |
| `Skeleton(modifier)` | заглушка на месте содержимого, которое ещё грузится: пульсирует между `backgroundRaised` и `backgroundItem`. Для картинок и плиток вместо гоблина; размер задаёт `modifier` (`matchParentSize()` в плитке) |
| `Toast(text, onDismiss, isError, durationMillis)` | всплывающее сообщение внизу по центру окна о результате действия: появляется, когда `text` не `null`, и само зовёт `onDismiss` через `TOAST_DURATION_MS` (5 с) или по клику. Ошибка — красные текст и рамка. Не перехватывает фокус |
| `ProgressBar(progress)` | полоса прогресса 0…1 |
| `BusyOverlay(text, progress, details)` | окно ожидания поверх страницы на время долгой операции, закрыть нельзя. В браузере всё работает в одном потоке: между тяжёлыми шагами делай короткую паузу (`delay(16)`), иначе индикатор замрёт вместе со страницей |
| `ActionMenuItem(text, onClick, enabled, icon, trailingIcon, trailingText, toggled)`, `ActionMenuDivider()` | пункт меню действий (`icon` слева, `trailingIcon` справа — белые силуэты, `trailingText` — значение золотом, `toggled` — пункт-переключатель: включён золотой, выключен приглушённый) и разделитель между пунктами. Кликается вся строка, подсветка — скруглённая плашка `AppShapes.MenuItem` с отступом от разделителей. Длинный текст переносится на вторую строку |
| `ActionButton(text, onClick)` | любое действие |
| `SquareIconButton(onClick, selected) { }` | квадратная кнопка с иконкой (панель инструментов) |
| `MenuItem(text, hotkey, badge, selected, highlighted)` | список разделов или тулз, как главное меню игры. Слот `icon` — не меньше квадрата иконки и шире под содержимое (отметка и портрет) |
| `NavGroup(title, icon) { NavItem(...) }` | боковая навигация по разделам страницы |
| `SectionHeader(title, description, warning)` | заголовок блока настроек или параметров |
| `DescriptionText`, `WarningText`, `ValueText` | пояснение, предупреждение, крупное золотое значение |
| `LinkText(text, url)` | ссылка на внешний адрес (скачать файл, открыть страницу): подчёркнутый бежевый текст, при наведении светлеет до `textPrimary`. Открывает `url` через `LocalUriHandler` — в вебе новая вкладка, на Desktop системный браузер |
| `SectionDivider` | разделитель между секциями |
| `LabelBadge(text, kind)` | NEW / HOT (`LabelBadgeKind.New` / `Hot`) |
| `OptionGroup(options, selected, onSelect, label)` | выбор одного из 2–4 вариантов |
| `DropdownSelect(selected, options, onSelect, label, fieldText, trailing, icon, minHeight)` | выбор из длинного списка; `fieldText` — постоянная подпись поля вместо выбранного значения (кнопка-список «Готовые пресеты»), `trailing` — элемент справа от варианта (корзина удаления), его центр — на оси стрелки поля; `icon` — картинка слева от варианта в списке и у выбранного в поле (портрет персонажа); `minHeight` — наименьшая высота поля, чтобы соседние списки были одной высоты |
| `OptionsMenu(expanded, onDismissRequest, options, selected, onSelect, label, icon, trailing)` | всплывающий список вариантов, привязанный к любому элементу (ставится в тот же `Box`): например, выбор по клику на иконку. Не выше `optionsMenuMaxHeight`, длинный прокручивается с полосой прокрутки. `DropdownSelect` построен на нём |
| `GroupedOptionsMenu(expanded, onDismissRequest, groups, groupLabel, options, selected, onSelect, label, groupIcon, icon)` | двухуровневый список: слева группы со стрелкой, при наведении справа раскрываются их варианты (с прокруткой) и прячутся, когда курсор уходит и с группы, и с вариантов. У каждой колонки своя панель своей высоты |
| `Modifier.verticalScrollbar(scrollState)` | полоса прокрутки у правого края: тонкая, под курсором и при перетаскивании шире (`scrollbarHoverWidth`); бегунок тянется мышью, клик по дорожке переносит его. Ставится перед `verticalScroll(scrollState)` |
| `ValueSlider(value, onValueChange, range, onValueChangeFinished)` | целое число в диапазоне, где важна наглядность; колесо мыши над ним меняет значение на шаг (сотая диапазона, не меньше 1); `onValueChangeFinished` — конец перетаскивания или клик, чтобы правка по ходу жеста легла в историю отмены одним шагом |
| `NumberField(value, onValueChange, range)` | точный ввод числа (фейлстак, цена, количество) |
| `InputField(value, onValueChange, placeholder, leading, trailing, minLines)` | текст и поиск; `trailing` — кнопки в конце поля, например `CloseButton` для очистки и `InfoButton` с ограничениями. `minLines` больше 1 — многострочное поле (вставка текста файла) |
| `ToggleSwitch(checked, onCheckedChange, label, description)` | включить или выключить опцию |
| `CheckBox(checked, onCheckedChange)` | отметка элементов списка для массового действия (например, удаления). Внутри строки `MenuItem` передавай его в слот `icon` |
| `Tile(selected, onClick) { }` | плитка в сетке: предметы, классы, персонажи |

## Как собрать страницу тулзы

Корень приложения обёрнут в `AppTheme { }`. Вкладка без `fillsScreen` получает страницу с прокруткой и отступом
`spaceXl`, содержимое стоит по центру. Типовая страница-калькулятор — содержимое своего размера:

```kotlin
@Composable
fun EnhancementScreen(state: EnhancementState, onEvent: (EnhancementEvent) -> Unit) {
    Column(
        Modifier.widthIn(max = AppTheme.dimens.contentMaxWidth),
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
        ValueText(stringResource(Res.string.enhancement_chance_value, state.chance))
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            MenuAction(stringResource(Res.string.enhancement_reset), { onEvent(EnhancementEvent.Reset) }, Modifier.weight(1f))
            MenuAction(stringResource(Res.string.enhancement_calculate), { onEvent(EnhancementEvent.Calculate) }, Modifier.weight(1f))
        }
    }
}
```

Редактор, которому нужно всё место (как слоты), — вкладка с `fillsScreen = true` и `ToolPage(modifier = Modifier.fillMaxSize())`:
слева меню на `backgroundSurface` — параметры, секции разделены `SectionDivider`; в `sidebarFooter` — импорт и
экспорт, сохранение пресета, перечитать файл, откат и последним главное действие со счётчиком изменений, кнопки на всю
ширину меню, у каждой подсказка при наведении. Длинных
предупреждений в `sidebarFooter` не держи: им место в окне подтверждения действия. Секции меню идут от источника
данных (пресеты) через контекст (персонаж) и выбранный элемент к виду (сетка, подложка). Справа `ActionBar` с правкой
и под ним рабочая
область с отступом `spaceL` на всё оставшееся место. Расположение частей взято с garmoth.com.
Пока у фичи нет доступа к данным (выбор папки), показывай подсказку без `ToolPage`, в колонке шириной
`contentMaxWidth`; на вкладке с `fillsScreen` добавь ей отступ `spaceXl` сам, а под ней — `ToolIntro`.
Строки фичи грузи заранее (`preload*Strings()` из `App`), иначе в браузере кнопки на миг пустые.

## Как добавить или изменить компонент

1. Положи его в `platform/uikit/.../components/`, назови по роли без `Bdo` (см. правило 1)
   и собирай только из токенов темы. Новые размеры — в `AppDimens`.
2. Состояние наружу (`value` + `onValueChange`), внутри — только визуальное: hover, expanded.
3. Курсор-рука на всём кликабельном: `Modifier.pointerHoverIcon(PointerIcon.Hand)`. Ripple отключён
   (`indication = null`), а реакция на наведение — через `collectIsHoveredAsState()` (подложка или рамка светлеет).
4. Добавь пример в `ComponentShowcase.kt`, его тексты — в `strings.xml` модуля uikit с префиксом `showcase_`,
   на всех языках приложения.
5. Проверь в браузере: запусти конфигурацию `webApp-wasm` из `.claude/launch.json`
   (`:webApp:wasmJsBrowserDevelopmentRun --continuous`, сервер на `http://localhost:8080`) и открой каталог
   по адресу `http://localhost:8080/#showcase` (во вкладках его нет). Сделай скриншот и сравни со скриншотами
   в `.claude/references/ui/`, если они есть. Пока dev-сервер запущен, не запускай отдельную сборку Gradle:
   они пишут в одни и те же папки `build/` и ломают друг другу результат.
6. Если меняешь токен, обнови таблицу в этом файле.
