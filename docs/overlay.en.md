[← BDO Tools](../README.en.md) · [Русский](overlay.md) · **English**

# Overlay

BDO Tools windows on top of the game: the combo hint, skill details over cooldown slots and the ping. Windows app
only.

- The overlay shows only while the game is in front. Use Windowed Fullscreen.
- Clicks on the combo hint and skill details go through to the game, and overlay windows never take focus.
- The app only reads the screen and the cursor position: it never touches the game and never presses keys.

## Button and menu

The BDO Tools icon on top of the game opens the "BDO Tools overlay" menu. The icon can be dragged, and its place
is remembered.

**"Settings"**

- "Show overlay" / "Hide overlay".
- "Overlay style" — "Combo layout" or "Next skills"; "Position" — vertical or horizontal.
- "Overlay size" and "Overlay opacity".
- "Move overlay" — drag it with the mouse, then click "Lock overlay".
- "Show skill details when the cursor rests on a cooldown slot" — the skill's name, effects and description next
  to the slot in game.

**"Combo"** — saved combos: "Show combo", "Hide combo" and "Edit combo" (opens the app).
How to build a combo — [Combo Assist](comboAssist.en.md).

**"Info"** — "Show game ping", "Move ping", its size and opacity.

## Ping

Latency to the game server in milliseconds, as in Windows Resource Monitor. The server is looked up again when you
change channel or reconnect.

> [!IMPORTANT]
> After a loading screen the game reconnects, and Windows lets only an administrator turn on latency tracking for
> the new connection. Without it the ping shows "— ms" — run BDO Tools as administrator.

## Tray

When you close the window, the app asks: "Minimize to tray" or "Close". In the tray it keeps running and the
overlay stays on top of the game. Click the tray icon to open the window.
