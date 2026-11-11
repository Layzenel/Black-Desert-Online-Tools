---
name: worktree-cleanup
description: Очистка ненужных git worktree в .claude/worktrees (копии репозитория от сессий и субагентов, каждая весит до 1,5 ГБ из-за build/ и .gradle/). Использовать, когда пользователь просит освободить место, почистить worktree или ветки, когда .claude/worktrees разрослась, и после слияния PR, сделанного в отдельном worktree.
---

# Очистка worktree

В `.claude/worktrees/` лежат git worktree: отдельные рабочие копии для параллельных сессий
(`EnterWorktree`, сессии приложения) и субагентов (`isolation: "worktree"`). Сами они не удаляются,
а сборки Kotlin делают каждую копию тяжёлой.

## 1. Собрать сведения

```bash
git worktree list --porcelain
```

Для каждого worktree внутри `.claude/worktrees/` (основную папку не трогать никогда):

```bash
git -C <путь> status --porcelain                                   # пусто — нет незакоммиченных правок
git -C <путь> status --porcelain --ignored | grep '^!!'            # игнорируемое, кроме build/, .gradle/, .kotlin/, kotlin-js-store/
git merge-base --is-ancestor <HEAD worktree> main && echo merged   # проект вливает merge-коммитом, проверки предка достаточно
du -sh <путь>
```

Если `main` мог отстать, сначала `git fetch origin` и проверять ещё и `origin/main`.

## 2. Решить, что удалять

Удалять можно, только если **всё** сразу:
- нет незакоммиченных правок;
- среди игнорируемого только результаты сборки;
- HEAD уже в `main` (или `origin/main`);
- worktree не помечен `locked` в `git worktree list`;
- это не папка текущей сессии и в ней не работает другая сессия
  (`mcp__ccd_session_mgmt__list_sessions`).

Не трогать руками worktree, которыми управляет приложение (их показывает
`mcp__ccd_host__get_storage_usage`; обычно имя вида `прилагательное-существительное-hex`, ветка `claude/<имя>`).
Для них есть `mcp__ccd_host__clean_up_worktrees` или Settings › Storage › «Clean up inactive sessions».

Всё, что не прошло проверку, показать пользователю с причиной и оставить.

## 3. Подтвердить и удалить

Удаление необратимо, поэтому сначала показать таблицу «папка, размер, ветка, почему можно удалить»
и дождаться «да» (кроме случая, когда пользователь уже прямо попросил удалить).

```bash
git -c core.longpaths=true worktree remove <путь>   # без --force: если git отказывает, значит что-то не так
git branch -d <ветка>                               # -d, не -D: удалит только влитую ветку
git worktree prune                                  # подчистить записи о папках, стёртых вручную
```

`core.longpaths=true` нужен на Windows: пути в `build/` длиннее 260 символов.
Папку не удалять через проводник или `rm -rf`, иначе git продолжит считать worktree существующим.

В конце снова `git worktree list` и сказать, сколько места освободилось.
