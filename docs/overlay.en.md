[← BDO Tools](../README.en.md) · [Русский](overlay.md) · **English**

# Overlay

BDO Tools windows on top of the game: the combo tracker and layout, skill details over cooldown slots, the ping and FPS.
Windows app only.

- The overlay shows only while the game is in front. Use Windowed Fullscreen.
- Clicks on the combo hint and skill details go through to the game, and overlay windows never take focus.
- The app only reads the screen and the cursor position: it never touches the game and never presses keys.

## Button and menu

The BDO Tools icon on top of the game opens the "BDO Tools overlay" menu. The icon can be dragged, and its place
is remembered.
Right-click the icon to turn the overlay on or off: when it is off, the icon is faded and overlay windows are hidden,
but the icon stays. A shield inside the icon instead of the diamond means the game is running as administrator.
Any overlay window (tracker, layout, ping) can be dragged with the left mouse button, and its place is remembered.
While the game hides the cursor (combat mode), the button and windows pass clicks to the game.

**"Settings"**

- "Skill tooltip over a slot" — the skill's name, effects and description next
  to the slot in game.
- "Skill protection on slots" — shields in the top-right corner of the icon: yellow — Super
  Armor, blue — Forward Guard, red — Invincible. Protection against monsters only (PvE) is not shown.
  Shields and tooltips disappear from slots covered by a game window (skills, settings, hidden UI), each slot on its own; when all are covered, the tracker and the layout hide too.
- "Crowd controls on slots" — crowd control icons (stun, knockdown and so on) at the
  bottom of the icon, as in game. Crowd controls against monsters only are not shown; they hide together with the shields.
  Hover a shield or a crowd control icon to see its name.

**"Combo"** — saved combos: "Show combo" (the schema of the combo under the cursor appears next to the menu), "Hide
combo" (game watching stops too) and "Edit combo" (opens the app). In the right half of the screen the menu opens to
the left, and overlay windows stay inside the screen.
How to build a combo — [Combo Assist](comboAssist.en.md).

- "Tracker" — the style: "Next" or "Focus".
- "Combo layout" — "Show layout" and "Vertical": the orientation applies only to the layout.
- "Overlay size" (20 to 100 %) and "Overlay opacity" — for the tracker and the layout.

**"Info"** — "Show game ping", "Show FPS" (style "Compact" or "Detailed"), "Info panel size" (20 to 100 %) and "Info panel opacity".

## Ping

Latency to the game server in milliseconds, as in Windows Resource Monitor. The server is looked up again when you
change channel or reconnect.

> [!IMPORTANT]
> After a loading screen the game reconnects, and Windows lets only an administrator turn on latency tracking for
> the new connection. Without it the ping shows "— ms" — run BDO Tools as administrator.

## FPS

How many frames per second the game puts on screen — to the right of the ping, in the same window. The app counts
frames by Windows system events about frame output, like Xbox Game Bar, and does not touch the game.

- **"Compact"** — FPS over the last second and "1%" (1% low): the FPS you would get if the whole second ran as slowly
  as the worst 1 % of frames over the last 5 seconds. The closer it is to FPS, the smoother the game.
- **"Detailed"** — also "freezes" (frames longer than twice the average over 5 seconds, orange when there were any)
  and a frame time graph below the line: a flat line is smooth, a spike up is a stutter. The dashed line is the
  average frame time.

> [!IMPORTANT]
> Windows shares these events with administrators only, so "Show FPS" can be turned on only when BDO Tools runs as
> administrator. Without the rights the switch is unavailable.

## Tray

When you close the window, the app asks: "Minimize to tray" or "Close". In the tray it keeps running and the
overlay stays on top of the game. Click the tray icon to open the window.
