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
| Иконки | `platform/composable/.../drawable/key_shift.svg`, `key_lmb.svg`, `key_rmb.svg`: белый контур, `viewBox 0 0 24 24` |

Правила:
- Клавишу выводишь так: `rememberKeyIcon(...)(action)?.let { KeyIcon(...) } ?: Text(label(action))`. Там, где картинку не вставить (строка `OptionButton`, текст тултипа), остаётся текст из `rememberKeyLabel`.
- Не пиши «Shift», «ЛКМ», «ПКМ» литералами в фичах: подписи — `rememberKeyLabel`.
- Новая клавиша с картинкой: поле в `KeyHints` (и `encode`/`decode`, тест), иконка, ветка в `keyIconOf`, `OptionGroup` в `DisplaySettings` (`feature/settings`), строки на всех языках.
