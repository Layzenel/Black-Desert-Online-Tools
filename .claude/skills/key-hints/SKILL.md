---
name: key-hints
description: Правило проекта BDO Tools — как подписывать клавиши умений (Shift, ЛКМ, ПКМ) в трекере, схеме и редакторе комбо — словом, сокращением или картинкой по выбору пользователя в «Настройки → Отображение». Использовать перед любым кодом, который выводит клавишу умения на экран, и при добавлении клавиши с картинкой.
---

# Подписи клавиш умений

Вид клавиш выбирает пользователь (Настройки → Отображение): Shift — «Shift», «Sh» или картинка; ЛКМ и ПКМ — слово или картинка мыши.

| Что | Где |
|---|---|
| Модель, выбор, сохранение | `platform/core`, `core.keyhint`: `KeyHints` (`styleOf(код клавиши)`), `KeyHintStyle` (`Text`, `Short`, `Icon`), `AppKeyHints` (Koin, `keyHintModule` в `App.kt`), `KeyHintSettings` (сайт — `localStorage`, Desktop — `GameFolderSettings.keyHints`, `Preferences`) |
| Подпись текстом | `rememberKeyLabel(keys)` в `platform/composable` — уже учитывает «Sh»; всегда бери подпись только через неё |
| Картинка | `rememberKeyIcon(keys)(action)` — не `null`, только если выбрана картинка; рисуй `KeyIcon(icon, textStyle, color)` — высота по тексту, красится `ColorFilter.tint` |
| Иконки | `platform/composable/.../drawable/key_shift.svg` — широкая клавиша с ⇧ в левом углу, `viewBox 0 0 36 24` (стрелку в квадрате путали с W); `key_lmb.svg`, `key_rmb.svg` — `viewBox 0 0 24 24`. Белый контур; в строке текста ширина — по пропорциям картинки |

Правила:
- Клавишу выводишь так: `rememberKeyIcon(...)(action)?.let { KeyIcon(...) } ?: Text(label(action))`. Текст с клавишами (тултип навыка, «Shift + ПКМ») — `SkillMarkupText(markup, keys, style, color)`: `{KeyBind:…}` в нём — подпись или картинка в строке текста; набор клавиш в разметку — `keyVariantLine`. В `OptionButton` — через его `content`. Текстом из `rememberKeyLabel` остаётся только то, что не рисуется: текст в поле ввода, памятка игры, подпись для экранного диктора.
- Не пиши «Shift», «ЛКМ», «ПКМ» литералами в фичах: подписи — `rememberKeyLabel`.
- Новая клавиша с картинкой: поле в `KeyHints` (и `encode`/`decode`, тест), иконка, ветка в `keyIconOf`, `OptionGroup` в `DisplaySettings` (`feature/settings`), строки на всех языках.
