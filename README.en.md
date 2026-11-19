# BDO Tools

[Русский](README.md) · **English**

A set of tools for Black Desert Online players: your own character portraits, interface layout without
editing game files by hand and combo hints on top of the game. Runs in the browser and as a Windows app.

| | |
|---|---|
| 🌐 Website | https://layzenel.github.io/Black-Desert-Online-Tools/ |
| 💾 Windows app | [BDO-Tools.msi](https://github.com/Layzenel/Black-Desert-Online-Tools/releases/download/desktop-latest/BDO-Tools.msi) or [portable ZIP](https://github.com/Layzenel/Black-Desert-Online-Tools/releases/download/desktop-latest/BDO-Tools-portable.zip) |

> BDO Tools changes only the settings files in `Documents\Black Desert` and never touches the game client.
> It saves a copy of the previous files before every write.

> [!IMPORTANT]
> While the game is running, it overwrites its files on character switch, teleport and exit.
> So save your changes with the **game closed**.

## What it does

| | |
|---|---|
| **[Portrait Constructor](docs/portraitConstructor.en.md)** | Your own images, memos and badges instead of the portraits in the character selection window, favorite sets and backups. |
| **[Interface Editor](docs/uiEditor.en.md)** | Lay out cooldown slots, chats and other elements on a canvas, skills and labels in slots, presets, layout import and export. |
| **[Combo Assist](docs/comboAssist.en.md)** | Your own class combo with forks and fillers, training and an in-game hint showing which skill to press next. |
| **[Overlay](docs/overlay.en.md)** | Windows app windows on top of the game: the combo hint, skill details over cooldown slots, the ping. |
| **[Several accounts](docs/accounts.en.md)** | Picking an account if several have played on this PC, and where BDO Tools keeps its files. |

Interface language — Russian or English, the `RU` / `EN` button in the top bar.

---

## Website or app

| | Website | Windows app |
|---|---|---|
| Installation | not needed | MSI or portable ZIP |
| Browser | Chrome, Edge, Opera, Yandex Browser | not needed |
| Access to the game folder | via browser permission | direct, finds `Documents\Black Desert` by itself |
| Updates | when the page is reloaded | the app offers to update itself |

The website needs the File System Access API, which only Chromium-based browsers have. In Firefox and Safari
the website offers to download the Windows app. There is also a link to it in the website's top bar.

---

## For developers

Kotlin Multiplatform and Compose Multiplatform: one codebase builds into the website (`wasmJs` and `js`) and the
Windows app (JVM). The website is published to GitHub Pages, and the installer to the `desktop-latest` release on
every push to `main`.

Website with live rebuild:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun --continuous
```

Windows app:

```bash
./gradlew :desktopApp:run
```

Check that both platforms build:

```bash
./gradlew :webApp:compileDevelopmentExecutableKotlinWasmJs :webApp:compileKotlinJs :desktopApp:compileKotlinDesktop
```

Module layout and project rules are in [CLAUDE.md](CLAUDE.md) (in Russian).

---

This is an unofficial fan project and is not affiliated with Pearl Abyss. Genuine content that includes trademarks
and/or works whose intellectual property rights belong to Pearl Abyss is not official.
