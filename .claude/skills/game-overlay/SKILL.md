---
name: game-overlay
description: Окна поверх игры в настольной версии BDO Tools (общая основа в platform/composable и вклады фич — оверлей комбо, пинг, кнопка и меню оверлея, тултипы слотов перезарядки) и чтение экрана игры (история умений, слоты перезарядок, какой персонаж в игре). Использовать перед любой правкой оверлея, меню оверлея, слежения за игрой, тултипов над слотами, трея и окна программы, и когда что-то поверх игры мигает, не кликается, отстаёт или показывает не того персонажа.
---

# Оверлей поверх игры

Всё поверх игры — только настольная версия (`desktopMain`); на сайте те же `expect` ничего не показывают.
Программа только **читает экран и положение курсора**: никаких хуков ввода, чтения памяти игры и нажатий за игрока.

## Где что лежит

Общая основа — `platform/composable`, пакет `composable.overlay`: её видит любая фича (фичи зависят только от
`composable`). Фича добавляет своё поверх игры через **`OverlayContribution`** в своём Koin-модуле
(`single(named("<фича>")) { … }`), корень приложения собирает все вклады (`getAll`) и показывает их в `OverlayHost`.

| Что | Где |
|---|---|
| Контракт фичи | `OverlayContribution(id, windows, tabs, settings)`: `id` — маршрут вкладки программы для «открыть программу» (`OverlayScope.openApp`), `windows` — окна поверх игры, `tabs` — `OverlayTab(order, title, content)` в общем меню, `settings` — пункты фичи в общей вкладке «Настройки» (её первой собирает `OverlayHost`) |
| Окно поверх игры | `GameOverlayWindow(id, anchor, size)` (`expect`, desktop — `GameOverlayWindow.desktop.kt`); место по `id` в `Preferences` (`OverlayPositions.kt`, узел `bdo-tools/combo-overlay`), до первого перемещения — у `OverlayAnchor` |
| Размер и непрозрачность | `ScaledOverlay(scalePercent, opacityPercent)`: размер через `LocalDensity` — окно растёт целиком; допустимый размер любого окна — `OverlayScalePercent` (20–100 %) |
| Кнопка и меню | `OverlayHost` → `OverlayMenuWindow` + `OverlayMenu`: значок программы, окно «BDO Tools оверлей», вкладки всех фич по `order` |
| Игра на переднем плане | `isGameForeground()` (JNA, `BlackDesert64.exe`), в UI — `rememberGameActive()`; сквозные клики — `setClickThrough` (всё — `desktopMain`) |
| Корень в приложении | `shared/src/desktopMain/.../window/DesktopApp.kt`: `OverlayHost` стоит рядом с окном программы, а не внутри него — скрытое в трей окно не перерисовывается. Общая ViewModel — `ViewModelStoreOwner` приложения; подписка на состояние в окнах поверх игры — `collectAsState` (вне окна программы нет `LifecycleOwner`) |
| Помощник комбо | `feature/comboAssist/.../overlay/ComboOverlayContribution.kt`: два окна — трекер (`ComboOverlayContent` по `OverlayStyle`: `Next` — `NextSkillsOverlay`, `Focus` — `FocusOverlay`, у `BottomCenter`) и схема комбо (`SchemaOverlay`, окно `combo-schema` у `CenterLeft`, показ — `OverlaySettings.schemaShown`, ориентация `Combo.vertical` — только у схемы). Всё на подложке `TooltipPanel`, тот же вид — в предпросмотре вкладки «Настройка оверлея». Пометка шага (`ComboCell.note`) — под трекером (`StepNote`). Ещё тултипы слотов (`SlotTooltipWindow`, `SlotUnderCursor.kt`) и щиты защиты в правом верхнем углу иконки слота (`SlotShieldsWindow`: защита в PvP по английскому тултипу — `skillProtections`, строки «(PvE only)» пропускаются), вкладка «Комбо» (`OverlayComboTab`) и пункт в «Настройках»; настройки (`OverlaySettings`) — в `combos.json` |
| Чтение экрана (комбо) | `data/history/ScreenSkillHistorySource.kt`: история умений + `data/slots/CooldownSlotWatch`; персонаж — `data/slots/PlayedCharacterDetector` |
| Пинг | `feature/gameInfo`: окно пинга и вкладка «Информация» (`GameInfoOverlay.kt`), настройки — `Preferences` `bdo-tools/game-info` |
| Окно программы и трей | `shared/src/desktopMain/.../window/` (`DesktopApp`, `AppTray`, `CloseChoiceDialog`) |
## Правила окон

- Все окна поверх игры: `undecorated`, `transparent`, `alwaysOnTop`, **`focusable = false`** — клик по ним не уводит игру
  с переднего плана. Видны только пока игра на переднем плане (`rememberGameActive`).
- **Сквозные клики** (`setClickThrough`, `WS_EX_TRANSPARENT`): тултипы, щиты и значки контролей — всегда. Окна
  `GameOverlayWindow` (трекер, схема, пинг) и кнопка меню ловят мышь, пока курсор показан (`rememberCursorShown`,
  `isCursorShown` — `GetCursorInfo`): окна тянутся зажатой ЛКМ (`WindowDraggableArea`), отдельного режима перемещения
  нет. Пока игра прячет курсор (режим боя), клики сквозь них уходят в игру — атака не достаётся оверлею. Тултип слота
  тоже не показывается при спрятанном курсоре.
- **Размер — по содержимому**: `wrapContentSize(unbounded = true)` + `onSizeChanged` → `state.size`. Внутри такого окна
  нельзя `verticalScroll`/`fillMax*` без ограничения (бесконечная высота роняет программу) — поэтому у `ComboOverlay`
  есть `inGame = true`: без прокрутки и без тултипов умений при наведении.
- **Не показывать окно, пока оно не встало на место**: сначала замер и позиция, потом `visible = true` (иначе мелькает
  в левом верхнем углу — см. `SlotTooltipWindow`).
- **Порядок окон**: кнопка меню всегда над оверлеем — оверлей при каждом появлении увеличивает
  `OverlayLayers.overlayShown`, окно кнопки в ответ делает `toFront()` (с паузой, чтобы оверлей успел показаться).
- **Перетаскивание кнопки**: свой `pointerInput` с `detectDragGestures` раньше `clickable` (иначе клик съедает нажатие),
  и окно ставится по `MouseInfo` на экране напрямую через `window.setLocation` — сдвиги внутри окна отстают.
  Позиция в состоянии и в `Preferences` (`bdo-tools/combo-overlay`) — когда отпустили.
- Всплывающие списки (`DropdownSelect`, `OptionsMenu`) в маленьких окнах обрезаются — внутри меню оверлея только
  обычные списки (`MenuItem`) и кнопки.
- Координаты: `RelativePosX/Y` элементов игры — доли экрана; Java отдаёт курсор и окна в логических пикселях,
  снимок экрана — в настоящих (`captureScreen` берёт лучший вариант `createMultiResolutionScreenCapture`).

## Чтение экрана

- Слежение за игрой включено всегда, пока открыто комбо (настольная версия). Пока игра не на переднем плане, программа
  за ней не следит: экран не снимается, файлы слотов не читаются, иконки не качаются (`ScreenSkillHistorySource`).
  20 кадров/с: история умений (≈240×54 px) и слоты (≈620×290 px).
- История — главный источник; слот, ушедший на перезарядку (`SlotDarkening`: цветность падает с ≈55 до ≈13), дописывает
  пропущенное через `HistoryMerge` (ждёт строку истории 0,6 с, не даёт связке обогнать запуск).
- **Раскладку слотов брать только у персонажа с экрана**, не по файлам: файл персонажа игра пишет, когда с него уходят,
  а у двух персонажей одного класса слоты бывают разложены по-разному (так «шаг 3» читался как «5.2»).
- Всё, что видно в слежении, пишется в `Документы\Black Desert\BDO Tools\comboAssist\watch.log` — сначала смотри его.
  Нераспознанная строка: в журнале лучший кандидат со счётом и отрывом, рядом картинка иконки `miss-<мс>.png`.
  Пороги совпадения — `accepts` в `IconMatch.kt` (иконка в истории бывает темнее справочной).


## Пинг игры

- `feature/gameInfo/.../data/DesktopGamePingSource` раз в секунду находит процесс `BlackDesert64.exe` по снимку списка процессов
  (`CreateToolhelp32Snapshot`: путь процесса, запущенного от администратора, Java не видит) и его TCP-подключения
  (`GetExtendedTcpTable`, `TcpTable.kt`). Сервер — подключение к порту 8889, иначе любое не веб-подключение;
  ищется заново при каждой проверке: адрес меняется при смене канала и переподключении.
- Задержка — `SmoothedRtt` из `GetPerTcpConnectionEStats` (как «Монитор ресурсов»). Читать её можно без прав, а
  включить сбор — только администратору (`SetPerTcpConnectionEStats` → 5, нет доступа). Программа включает сбор
  для каждого нового подключения сама (после загрузочного экрана игра подключается заново); без прав —
  `PingReading(needsAdmin = true)`, «— мс» и подсказка во вкладке «Информация».
- Не мерить время установки своего TCP-соединения: VPN и ускорители (у игрока туннель `tun0`, локальный адрес
  `172.19.0.1`) отвечают на него сами — выходит 1 мс. ICMP-пинг сервер режет.
- Фича `feature/gameInfo`. Замер идёт, только пока пинг показывают (`GameInfoViewModel`); окно — `GameOverlayWindow` с id `ping`.

## Перед тем как закончить

- Собери обе платформы (сайт должен собираться, хоть окон там и нет) и прогони `:feature:comboAssist:desktopTest`.
- Если программа запущена — закрой её перед пересборкой: перезапись файлов под работающим окном роняет его
  (`ZipException: invalid LOC header`).
- Новые знания о том, как игра рисует свои элементы, — в скилл `game-files`.

