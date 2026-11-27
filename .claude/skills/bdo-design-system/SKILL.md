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
| `icons/CrowdControlIcons.kt` | значки контролей умений, как в игре: `CrowdControl.icon` (webp). Сам `CrowdControl` и разбор тултипа (`CrowdControl.pvpOf`) лежат в `platform/core` (`core.skill.domain`); значки — у навыков в редакторе интерфейса и под слотами перезарядки |
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
4. **Почти прямые углы.** `AppShapes.Sharp` (0dp) или `AppShapes.Small` (2dp); кнопки и варианты — `AppShapes.Control` (3dp, снято со скриншотов игры). `AppShapes.Pill` — только для
   плашек NEW/HOT и переключателя. `AppShapes.MenuItem` (4dp) — подсветка пункта в меню действий.
5. **Золото — это сигнал, а не украшение.** `accentGold` используется для активного пункта навигации, текущего
   значения, результата расчёта, «новых» разделов и имени персонажа. Обычный текст бежевый или кремовый.
6. **Вид кнопки — по смыслу действия** (`ActionButtonState`). Положительное действие (применить, сохранить, OK,
   установить, далее) — `ActionButtonState.Positive`: бронзовая кнопка, как «Выставить лот» в игре. Остальное
   (отмена, сброс, вспомогательные) — `Neutral`, серая; удаление того, что пользователь создал сам (комбо, пресет), — `Negative`, тёмно-красная, как корзина в игре. Главное действие к тому же стоит последним —
   справа в строке действий или `WindowFooter` (порядок «Применить — Назад — OK»), внизу в `sidebarFooter` у `ToolPage`.
   Новый вид кнопки — новый подкласс `ActionButtonState` и его ветка `when` в `ActionButton`, а не флаг.
   **Кнопка в приложении одна — `ActionButton`**: в окнах, в строке действий под страницей, в меню слева у `ToolPage`,
   в меню оверлея. Своих «кнопок-пунктов» с другой подложкой не заводи: два стиля одной роли уже были и разошлись.
   Включатель режима («Порядок» в портретах) — `ActionButtonState.Toggle`.
   **Кнопка в окне подтверждения удаления** («Удалить комбо», «Удалить пресет») — тоже `Negative`: вид кнопки задаёт
   смысл действия, а не место, где она стоит.
   **У каждой кнопки есть подсказка при наведении** — `tooltip` в `ActionButtonState` (у `ToolbarButton` — `description`):
   одна короткая фраза о том, что кнопка делает здесь, а не повтор подписи. Новая кнопка без подсказки — ошибка.
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
      (`widthIn(max = …)`) на подложке `ContentFrame`: заголовок секции, содержимое и строка `ActionButton` под ним,
      каждому `Modifier.weight(1f)`,
      главное действие последним справа.
11. **Подписи пунктов меню и кнопок — в одну строку.** Если подпись не влезает, её сокращают («Загрузить»
    вместо «Загрузить изображение»), а не переносят. `ActionButton` обрезает лишнее многоточием.
    Подробности действия — в описании секции (`SectionHeader(description = …)`).
12. **Переключатель — только с названием.** У `ToggleSwitch` под подписью нет текста: пояснение передаётся в
    `description` и показывается подсказкой при наведении. Так же и у секций, где пояснение относится к кнопкам
    (подложка в редакторе интерфейса): подробности — в подсказках кнопок, а не в `SectionHeader(description = …)`.
13. **Вкладки — `TabBar`, а не `OptionGroup`.** Вкладки, как на рынке игры («Продажа / Покупка»), стоят у левого края,
    ширина — по подписи, но не уже `tabMinWidth`; на всю ширину не растягиваются. `OptionGroup` — для выбора варианта
    настройки, а не для переключения содержимого.
14. **Подсветка пункта выпадающего списка** (`OptionsMenu`, `DropdownSelect`, `GroupedOptionsMenu`) под курсором — тёмный
    цвет акцента темы окон (`AppTheme.window.titleBarDark`), а не серая подсветка Material3 (`MenuOption`).
15. **Порядок, который меняет пользователь** (сохранённые комбо, пресеты), меняется перетаскиванием: такие пункты
    помечены значком ⠿ (`DragHandle`) в начале, за него и тянут. Значок нарисован — символа ⠿ в шрифте web-сборки нет.
16. **Слайдеры компактные**: `ValueSlider` — одна строка, дорожка и значение справа, без подписей min/max.
17. **Вёрстка не зависит от языка.** Интерфейс переводится (скилл `resources-externalization`, раздел «Языки»),
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
| `titleBar` → `titleBarLight` | `#775D3A` → `#85693F` | подложка шапки окна и верхней навигации: светлеет книзу, как в игре |
| `titleBarDark` | `#5E4A2E` | тёмная рамка шапки |
| `titleBarHighlight` | `#C0A850` | золотая кромка над `ContentFrame` |
| `windowOutline` / `windowFrame` | `#151C24` / `#6A593E` | рамка `GameWindow`: тёмная снаружи, светлая бронзовая внутри |
| `button` / `buttonHover` / `buttonPressed` | `#44444E` / `#52525D` / `#38383F` | кнопки |
| `buttonBorder` | `#4E4F56` | рамка кнопки |
| `actionButtonTop` → `actionButtonBottom`, `actionButtonEdge` | `#4C4D57` → `#42434D`, `#686A73` | обычная кнопка (`ActionButtonState.Neutral`): тёмная рамка `windowOutline` снаружи, светлая кромка внутри |
| `actionButtonHoverTop` → `actionButtonHoverBottom`, `actionButtonHoverEdge` | `#5E4D44` → `#53443C`, `#806652` | она же под курсором — коричневая, как «Назад» в игре |
| `actionButtonDisabled`, `actionButtonDisabledEdge` | `#424243`, `#575758` | любая недоступная кнопка — плоская серая |
| `buttonPositiveTop` → `buttonPositiveMiddle` → `buttonPositiveBottom` | `#9F7B59` → `#704E38` → `#8C6144` | бронза `ActionButtonState.Positive`: темнеет к 65 % высоты и снова светлеет |
| `buttonNegativeTop` → `buttonNegativeBottom` | `#603730` → `#442620` | `ActionButtonState.Negative` (удалить): темнеет книзу |
| `tab` / `tabBorder` / `tabHover` / `tabSelected` / `tabSelectedBorder` / `textTab` | `#2B2B30` / `#3A3A40` / `#36363C` / `#3F3F45` / `#5A5A62` / `#8C8C93` | вкладки `TabBar`: невыбранная, под курсором, выбранная; серая подпись невыбранной |
| `option` / `optionBorder` | `#35353A` / `#4D4D55` | невыбранный `OptionButton` (внутри тёмной рамки `windowOutline`) |
| `optionHover` / `optionHoverBorder` | `#625244` / `#866550` | `OptionButton` под курсором |
| `toggleTrack` / `toggleTrackBorder` / `toggleThumbOff` | `#34343A` / `#4D4E55` / `#686868` | выключенный `ToggleSwitch`: серый кружок внутри дорожки |
| `toggleOn` / `toggleThumbOn` | `#926C2D` / `#FFEDD4` | включённый `ToggleSwitch`: тёмное золото и крупный кремовый кружок во всю высоту |
| `indicator` / `indicatorBorder` / `indicatorBorderSelected` | `#292929` / `#4C4E58` / `#7E6454` | квадратик-индикатор `OptionButton` |
| `indicatorMark` → `indicatorMarkShade` | `#FFF2E0` → `#E4C89F` | отметка выбранного индикатора (градиент сверху вниз) |
| `border` / `borderSelected` | `#39393F` / `#685048` | рамки пунктов |
| `divider` | `#353536` | разделители секций |
| `track` | `#101011` | дорожка слайдера |
| `textPrimary` | `#F8F0D8` | заголовки окон, пункты меню, значения |
| `textBeige` | `#E0C8A0` | заголовки секций, навигация, горячие клавиши |
| `textMuted` | `#A89068` | пояснения под заголовками, плейсхолдеры |
| `textDisabled` | `#686868` | неактивные пункты и варианты |
| `textButton` | `#E8E8E8` | текст на кнопках |
| `textSelected` | `#F3D7AD` | подпись выбранного `OptionButton` |
| `textNote` | `#E0783C` | заметка персонажа — оранжевая подпись над именем, как в окне «Персонажи аккаунта» игры |
| `accentGold` | `#F8C038` | активное, текущее значение, важное |
| `accentGoldPale` | `#D8B080` | рамка выбранной плитки |
| `danger` | `#D86048` | предупреждения, ошибки |
| `success` | `#7CC050` | хорошее значение показателя (низкий пинг) |
| `accentBlue` / `accentRed` | `#4F8CD8` / `#C83C3C` | цветовые метки категорий (группы элементов в редакторе слотов); не для кнопок и не для ошибок |
| `badgeNew` / `badgeHot` | `#D09820` / `#D05020` | плашки NEW / HOT |
| `rageDeep` / `rageDark` | `#0C0505` / `#3A0A0A` | подложка трекера комбо под гневом Чёрного духа (градиент сверху `rageDark` → вниз `rageDeep`) |
| `rageBorder` / `rageFlame` / `rageEmber` | `#7A1414` / `#B4140E` / `#E8501C` | его рамка и огонь: языки пламени `rageFlame`, угли и разгорающаяся рамка `rageEmber`. Только для гнева, не для ошибок |

## Темы окон

Как «Настройки темы интерфейса» в игре: Классика, Сезон: Лето, Сезон: Зима, Сезон: Весна, Страна Утра.
Тема (`WindowTheme` в `platform/core`, `core.theme`; выбор — `AppThemes`, помнится как язык) меняет только окна:
цвет шапки и узор у её правого края (`titleBarBackground`, в классике — шеврон), украшение над окном
(`windowDecoration`) и кнопку закрытия (`WindowCloseButton`). Вид темы — `WindowTheme.style` (`WindowThemeStyle`
в `theme/WindowThemeStyles.kt`), текущая — `AppTheme.window`, корень передаёт её в `AppTheme(windowTheme = …)`.
Картинки `drawable/theme_*` вырезаны из скриншотов игры, присланных пользователем: левый край узора шапки
растворяется, фон украшений прозрачный. Остальная палитра от темы не зависит.

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
| `TopNavigation(tabs, selected, onSelect, label, icon, title, titleIcon, trailing)` | верхняя навигация сайта; используется один раз, в `shared/App.kt`. `titleIcon` — значок перед названием (в настольной версии — щит «запущено от имени администратора»). У вкладки иконка `icon` (белый силуэт, красится в цвет подписи) и справа от неё подпись до двух строк, выровненная по левому краю (перенос `\n` в строке ресурса). `trailing` — элементы у правого края (версия, выбор аккаунта, на сайте — `ToolbarButton` скачивания настольной версии) |
| `ToolPage(sidebar, sidebarWidth, sidebarFooter) { }` | страница тулзы без шапки: меню слева во всю высоту (`backgroundSurface`, прокручивается само, отступ `spaceL`, между частями `spaceM`) и содержимое справа на всё свободное место. `sidebarFooter` прижат к низу меню и не прокручивается — для главных действий. Годится и внутри большого `ModalWindow` (превью импорта в слотах) |
| `ActionBar { }` | строка инструментов правки над содержимым `ToolPage` (отменить, повторить — в подсказке горячая клавиша). Работа с файлами и главное действие — внизу меню, в `sidebarFooter` |
| `ToolbarButton(icon, description, onClick, enabled, selected)`, `ToolbarDivider()` | любая кнопка-иконка без подписи (панель инструментов в `ActionBar`, верхняя навигация, инструменты над сеткой): квадратный `ActionButton` без подписи (белый силуэт, цвет задаёт кнопка) с подсказкой `description` при наведении; `selected` — включённый режим (`ActionButtonState.Toggle`: коричневая, золотая иконка). `ToolbarDivider` отделяет группы кнопок. Подпись у иконки только в подсказке, поэтому она обязательна |
| `HoverTooltip(text) { }` | всплывающая подсказка под элементом при наведении курсора, как у `ToolbarButton` (на нём и построена). Для пояснения, которому не место под подписью: например, у `ToggleSwitch` в узком меню. Снизу не влезает — встаёт над элементом |
| `HoverTooltip(tooltip = { }, enabled, beside) { }` | подсказка с любым содержимым (карточка навыка): появляется сразу, пропадает, как только курсор ушёл или элемент нажали, по таймеру не гаснет. `enabled = false` прячет её, не пересоздавая элемент — можно выключать посреди жеста или по клавише-модификатору. `beside = true` — справа от элемента (строки списка в меню), иначе под ним |
| `TooltipPanel { }` | подложка подсказки, как тултип игры: тело `backgroundDeep` в двойной рамке (внешняя `borderSelected`, зазор `tooltipFrameGap`, внутренняя `border`) и тень `menuShadow` — для своей подсказки, например в `aside` у `ModalWindow` |
| `TooltipSection(title) { }` | блок внутри большой подсказки, как разделы тултипа навыка в игре: плашка `backgroundSurface` в тонкой рамке на всю ширину с бежевым заголовком |
| `ColorPalette(colors, selected, onSelect, defaultColor, defaultLabel)` | выбор цвета из готовых: квадраты `swatchSize` в одну строку (без места сжимаются, не переносятся), первый — цвет по умолчанию (`null`), выбранный в золотой рамке. Сами цвета — данные фичи (например, цвет заметки на портрете), а не токены темы |
| `ContextMenu<K>(expanded, onDismissRequest, submenu = { key -> ... }) { submenuTrigger -> ActionMenuItem(...) }`, `Modifier.onSecondaryClick(onClick)` | меню правого клика: `onSecondaryClick` отдаёт точку нажатия и поглощает его до дочерних элементов, `ContextMenu` ставится в пустой `Box` с `offset` в этой точке и раскрывается от его угла (у правого или нижнего края окна — влево или вверх), после чего стоит на месте. Вложенные панели выезжают справа (у правого края — слева), пока курсор над пунктом с `Modifier` из `submenuTrigger(ключ)` или над панелью (открыта одна); панель встаёт напротив своего пункта, у нижнего края сдвигается вверх сама, не двигая меню. Нажатие мимо меню закрывает его и доходит до страницы: правый клик в другом месте сразу открывает там новое меню (сбрасывай точку меню в `onDismissRequest`, только если она не сменилась). Пункты — `ActionMenuItem` (недоступные с `enabled = false`) и `ActionMenuDivider` |
| `Modifier.panCursor(cursor)`, `PanCursor` | курсор-рука над рабочей областью: `Open` — сдвиг возможен, `Grabbing` — область схвачена. На Desktop руки нарисованы в коде (`HandCursor.kt`), в вебе включается CSS `grab`/`grabbing` для всего холста Compose, поэтому не-`None` ставь, только пока курсор над областью |
| `Modifier.resizeCursor(dragging)` | курсор «диагональные стрелки» (↗↙) над ручкой растягивания окна за правый верхний угол; `dragging = true` держит его, пока ручку тянут. На Desktop — системный курсор, в вебе — CSS `nesw-resize` для холста Compose |
| `ContentFrame { }` | подложка содержимого страницы без шапки: графитовое тело окна (`backgroundSurface`), чёрная рамка и золотая кромка `titleBarHighlight` сверху; внутри колонка с отступом `spaceL` и промежутками `spaceM`. Для страницы «по центру» (портреты): заголовок, сетка и строка `ActionButton` — внутри |
| `ToolIntro(points, image, imageDescription)` | что умеет тулза — под просьбой выбрать папку, пока данных нет: пункты с золотым тире и скриншот тулзы (`drawable/intro_*.jpg`, сжатый JPEG ~1100 px; перевод — в `drawable-<tag>/`). Пустой экран с одной кнопкой не оставляй |
| `BulletList(points)` | список пунктов с золотым тире (ограничения, советы, что умеет тулза); на нём построен `ToolIntro`, в каталоге виден в его примере |
| `GameWindow(title, titleBarModifier, actions, footer) { }` | окно с шапкой в цвете темы окон и украшением темы над ней: основа `ModalWindow` и примеров каталога. Рамка как у окон игры: тёмная `windowOutline` снаружи, светлая бронзовая `windowFrame` внутри, сверху кромка `titleBarHighlight`. `titleBarModifier` — для шапки (перетаскивание окна за неё). Для страниц тулз — `ToolPage` |
| `WindowTitleBar`, `WindowFooter`, `Panel` | части окна, если нужна нестандартная компоновка. Шапка и `TopNavigation` рисуются одной подложкой `titleBarBackground` (как окна игры: бронза светлеет книзу, тёмная рамка, у правого края крупный бледный шеврон «^» с ореолом и тёмным треугольником внутри, слева от него диагональные штрихи); новая бронзовая полоса — тоже через неё |
| `ModalWindow(title, onDismissRequest, dismissible, footer, aside) { }` | модальное окно поверх страницы: выбор, подтверждение, редактор. Пока идёт долгая операция, передавай `dismissible = false`. `aside` — панель слева от окна вровень с его верхом (описание пункта, на который навели); окно из-за неё не сдвигается |
| `CloseButton(onClick)` | крестик (нарисован линиями): в шапке окна и для очистки поля |
| `WindowCloseButton(onClick)` | кнопка закрытия в шапке окна (`ModalWindow`, меню оверлея): рисунок зависит от темы окон — крестик, морская звезда, снежинка, клевер, цветок |
| `Modifier.windowDecoration()`, `windowDecorationHeight()` | украшение темы окон над шапкой (картинки над левым и правым краем, за пределами элемента); у `GameWindow` уже есть. Отдельному окну ОС оставь над окном `windowDecorationHeight()` места |
| `WindowThemePreview(theme, title)` | превью окна в теме `theme` — в подсказке при наведении на тему, как в игре |
| `InfoButton(text)` | кружок с «!»; по клику над ним подсказка с `text` — ограничения поля, правила |
| `HoverActionsBox(actions = { ActionMenuItem(...) }) { }` | действия над элементом (плиткой) по наведению курсора: элемент размывается (`hoverBlurRadius`) и затемняется, поверх встаёт чёткая золотая рамка, пункты — по центру внутри элемента и не перекрывают соседей. Подсвечивать элемент самому не нужно. Всплывающих меню-тултипов в дизайн-системе нет: действия показываются внутри элемента |
| `RunningGoblin()`, `LoadingRow(text)` | индикатор загрузки — чёрный гоблин со светлой обводкой бежит на месте (цвета `silhouette`, `silhouetteOutline`); строка «гоблин + текст» для короткого ожидания внутри окна |
| `Skeleton(modifier)` | заглушка на месте содержимого, которое ещё грузится: пульсирует между `backgroundRaised` и `backgroundItem`. Для картинок и плиток вместо гоблина; размер задаёт `modifier` (`matchParentSize()` в плитке) |
| `Toast(text, onDismiss, isError, durationMillis)` | всплывающее сообщение внизу по центру окна о результате действия: появляется, когда `text` не `null`, и само зовёт `onDismiss` через `TOAST_DURATION_MS` (5 с) или по клику. Ошибка — красные текст и рамка. Не перехватывает фокус |
| `ProgressBar(progress)` | полоса прогресса 0…1 |
| `BusyOverlay(text, progress, details)` | окно ожидания поверх страницы на время долгой операции, закрыть нельзя. В браузере всё работает в одном потоке: между тяжёлыми шагами делай короткую паузу (`delay(16)`), иначе индикатор замрёт вместе со страницей |
| `ActionMenuItem(text, onClick, enabled, icon, trailingIcon, trailingText, toggled)`, `ActionMenuDivider()` | пункт меню действий (`icon` слева, `trailingIcon` справа — белые силуэты, `trailingText` — значение золотом, `toggled` — пункт-переключатель: включён золотой, выключен приглушённый) и разделитель между пунктами. Кликается вся строка, подсветка — скруглённая плашка `AppShapes.MenuItem` с отступом от разделителей. Длинный текст переносится на вторую строку |
| `ActionButton(state, modifier)`, `ActionButtonState` | любое действие, единственная кнопка приложения. `state` — sealed-состояние: подпись, `icon` (белый силуэт слева), `hotkey` (приглушённый текст справа), `enabled`, `onClick`, `tooltip` (подсказка при наведении — обязательна по смыслу, правило 6) и вид по подклассу — `ActionButtonState.Neutral(text, onClick = …)` (серая, под курсором коричневая), `ActionButtonState.Toggle(text, checked, …)` (включатель режима: включённый — коричневый с золотой подписью), `ActionButtonState.Positive(…)` (бронзовая с градиентом: применить, сохранить, OK) или `ActionButtonState.Negative(…)` (тёмно-красная: удалить). Ширину задаёт место: в строке — `Modifier.weight(1f)`, в столбце — `Modifier.fillMaxWidth()`; длинная подпись обрезается многоточием. Подложки — `ActionButtonSurfaces.kt` |
| `MenuItem(text, hotkey, badge, selected, highlighted)` | список разделов или тулз, как главное меню игры. Слот `icon` — не меньше квадрата иконки и шире под содержимое (отметка и портрет) |
| `NavGroup(title, icon) { NavItem(...) }` | боковая навигация по разделам страницы |
| `SectionHeader(title, description, warning)` | заголовок блока настроек или параметров |
| `ExpandableHeader(title, open, onToggle, description)` | заголовок свёрнутого раздела меню: треугольник ▸/▾ и `SectionHeader`, клик переключает; пояснение видно, только пока раскрыт, содержимое показывает вызывающий («Филлеры» и «Пул» в помощнике комбо, «Ретушь» в редакторе скриншотов) |
| `DescriptionText`, `WarningText`, `ValueText` | пояснение, предупреждение, крупное золотое значение |
| `LinkText(text, url)` | ссылка на внешний адрес (скачать файл, открыть страницу): подчёркнутый бежевый текст, при наведении светлеет до `textPrimary`. Открывает `url` через `LocalUriHandler` — в вебе новая вкладка, на Desktop системный браузер |
| `SectionDivider` | разделитель между секциями |
| `LabelBadge(text, kind)` | NEW / HOT (`LabelBadgeKind.New` / `Hot`) |
| `TabBar(tabs, selected, onSelect, label, fill)` | вкладки: ряд у левого края, ширина по подписи (не уже `tabMinWidth`, высота `tabHeight`); `fill = true` — делят ширину ряда поровну (узкое окно: меню оверлея); невыбранная тёмная с серой подписью, под курсором светлее с белой, выбранная светлая графитовая. Вкладки редактора комбо, частей комбо (обычное / под гневом), меню оверлея |
| `rememberReorderState(onMove)`, `Modifier.reorderItem(state, index)`, `DragHandle(state, index)` | перестановка пунктов столбца мышью: пункт тянут за значок ⠿ `DragHandle` в его начале, он едет поверх соседей, отпустили — `onMove(откуда, куда)`. Каждому пункту — `reorderItem` с его номером. В `DropdownSelect`/`OptionsMenu` то же включается параметром `onMove` |
| `OptionGroup(options, selected, onSelect, label)`, `OptionButton` | выбор одного из 2–4 вариантов, как в настройках игры: квадратик-индикатор и подпись, `icon` — картинка вместо подписи высотой `iconSize`, ширина по её пропорциям (широкая клавиша не сжимается в квадрат), (или `content` — своё содержимое цвета подписи, например клавиши с картинками). Невыбранный — серая подложка `option`, тёмный индикатор; выбранный — `backgroundSelected`, светлая отметка `indicatorMark` в индикаторе, текст `textSelected` |
| `DropdownSelect(selected, options, onSelect, label, fieldText, trailing, icon, minHeight, searchPlaceholder)` | выбор из длинного списка; `fieldText` — постоянная подпись поля вместо выбранного значения (кнопка-список «Готовые пресеты»), `trailing` — элемент справа от варианта (корзина удаления), его центр — на оси стрелки поля; `icon` — картинка слева от варианта в списке и у выбранного в поле (портрет персонажа); `minHeight` — наименьшая высота поля, чтобы соседние списки были одной высоты; `searchPlaceholder` — строка поиска в списке (как у `OptionsMenu`); `query` + `onQueryChange` — набор с клавиатуры при открытом списке уходит в своё поле поиска над списком (выбор класса в комбо); `onMove` — пункты переставляются за ⠿ |
| `OptionsMenu(expanded, onDismissRequest, options, selected, onSelect, label, icon, trailing, searchPlaceholder)` | всплывающий список вариантов, привязанный к любому элементу (ставится в тот же `Box`): например, выбор по клику на иконку. Не выше `optionsMenuMaxHeight`, длинный прокручивается с полосой прокрутки. С `searchPlaceholder` сверху строка поиска: печатать можно сразу, крестик очищает, видны только подходящие варианты. `DropdownSelect` построен на нём |
| `GroupedOptionsMenu(expanded, onDismissRequest, groups, groupLabel, options, selected, onSelect, label, groupIcon, icon)` | двухуровневый список: слева группы со стрелкой, при наведении справа раскрываются их варианты (с прокруткой) и прячутся, когда курсор уходит и с группы, и с вариантов. У каждой колонки своя панель своей высоты |
| `Modifier.verticalScrollbar(scrollState)` | полоса прокрутки у правого края: тонкая, под курсором и при перетаскивании шире (`scrollbarHoverWidth`); бегунок тянется мышью, клик по дорожке переносит его. Ставится перед `verticalScroll(scrollState)` |
| `ValueSlider(value, onValueChange, range, onValueChangeFinished)` | целое число в диапазоне, где важна наглядность; компактный — одна строка: дорожка и значение золотом справа (`sliderValueWidth`); колесо мыши над ним меняет значение на шаг (сотая диапазона, не меньше 1); `onValueChangeFinished` — конец перетаскивания или клик, чтобы правка по ходу жеста легла в историю отмены одним шагом |
| `NumberField(value, onValueChange, range)` | точный ввод числа (фейлстак, цена, количество) |
| `InputField(value, onValueChange, placeholder, leading, trailing, minLines, clearable)` | текст и поиск; у непустого поля в конце сам рисует крестик очистки (`clearable = false` — без него, как у `NumberField`), свой `CloseButton` не добавляй; `trailing` — другие кнопки в конце поля, например `InfoButton` с ограничениями. `minLines` больше 1 — многострочное поле (вставка текста файла) |
| `ToggleSwitch(checked, onCheckedChange, label, description, enabled)` | включить или выключить опцию; рядом только `label`, `description` — подсказкой при наведении; `enabled = false` — опция недоступна (серая подпись, не нажимается), причину пиши рядом (`WarningText`) |
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
            ActionButton(ActionButtonState.Neutral(stringResource(Res.string.enhancement_reset), onClick = { onEvent(EnhancementEvent.Reset) }), Modifier.weight(1f))
            ActionButton(ActionButtonState.Positive(stringResource(Res.string.enhancement_calculate), onClick = { onEvent(EnhancementEvent.Calculate) }), Modifier.weight(1f))
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
