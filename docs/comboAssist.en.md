[← BDO Tools](../README.en.md) · [Русский](comboAssist.md) · **English**

# Combo Assist

Your own combo for a class and an in-game hint showing which skill to press next.

The tool is in the Windows app only; the website does not have it. For the in-game hint, see [Overlay](overlay.en.md).

## Editor

- On the left — "Choose class" and "Saved combos". A combo belongs to a class and fits all of its characters. Drag saved combos to change their order.
- A combo without a name is not saved: the "Combo name" window asks for it. The "Discard changes" button in the toolbar drops all unsaved edits.
- The buttons above the editor and Ctrl+Z / Ctrl+Y (Ctrl+Shift+Z) undo and redo combo edits; a dragged cell moves
  under the others, and a blue pointer at a cell edge shows where it will go; deleting a combo asks for confirmation. At the bottom of the menu — "How the tracker works" and removing the rage combo.
- "Skills" — the class skills with search (by name or keys: "shift lmb") and the "Succession" or "Awakening" stances; for Seraph —
  "Sacramentum" (without "Purga:" skills), "Purgatum" (without "Sacra:" skills).
- **Black Spirit Rage combo** — above the combo, switch between "Regular combo" and "Under Black Spirit Rage"
  ("Add Black Spirit Rage combo" if there is none yet; Seraph gets one right away); on the Rage tab, "Copy the normal
  combo" copies the steps, fillers and optional skills so you only change the differences. When the three Black Spirit Rage
  buffs appear in the buff list with a 59-second timer, the tracker follows the Rage combo for one minute on
  a black-and-red panel with fire, then goes back to the regular combo. The tracker has to see the Rage start: if the
  app was opened mid-Rage, it waits for the next one. The timer is read in the Russian game client.
- The keyboard button in the skill search ("Search by keys") records a key combination, for example Shift + F, and finds skills with those keys.
- A skill from the list can be dragged onto the combo canvas.
- Select a cell and click a skill on the left: a new cell appears after it, and "Can follow" above the list shows
  what usually comes next, with chains marked.
- Drag an icon to an edge of another cell: the left or right edge puts the skill before or after it, the top or
  bottom edge makes it a new fork branch above or below. A blue line shows where it goes.
  Drop a branch cell to the left or right of itself to take it out of the fork; a two-branch fork closes. Right-click a cell for "Delete cell", "Insert left",
  "Insert right".
- **"Chain"** — the skill is pressed right after the previous one, or the chain breaks.
- **"Note"** — your own text for a step ("while jumping", "3 hits"): the tracker shows it when this step is next.
- **Forks** — "Make a fork" and "+ branch": several ways to continue. The hint picks one by cooldowns, and when
  branches are ready at about the same time it takes them in turn: 1.1, then 1.2, then 1.3.
- **"Fillers"** — skills the overlay suggests while the next step is on cooldown.
- **"Optional skills"** — below the cell editing: skills the tracker always shows, apart from the steps (protection, buffs,
  dodges — what you use as the fight goes), in the order you set: "+" adds, dragging reorders, the corner cross or right-click removes. A ready skill has a
  gold frame, one on cooldown shows seconds. Optional cells of older combos move here on their own.
- The editor warns about empty cells and about a chain continuation without its starter.

## Overlay settings

"Tracker settings" mode (Windows app only) lets you pick the tracker style and see it right away; below it are the
combo layout and its orientation. Clicking an icon in the layout "uses" the skill, so you can see how the hint will
behave in game.

## Import and export

- "Export combo" — a `.json` file named after the combo, to share with other players.
- "Import combos" — add combos from such files.

## In game

The Windows app watches the game by itself while the game is in the foreground: it reads the Skill Usage History
on screen and guides you through the combo. The tracker shows what to press now, in one of the styles:

- **"Next"** — "Now" and the next two skills, with the character's keys; if a step is on cooldown —
  "In N s" and "While you wait" with a filler;
- **"Focus"** — a large icon of the current step with its keys under it, the next two beside it, a progress bar
  below; step changes are animated;
- optional skills: under the tracker in rows of 7 in "Next", on the left in at most two rows in "Focus";
- the step note, if there is one, is shown under the tracker;
- if you used a skill out of order or another fork branch, the hint continues from it; if a chain broke, it shows how to get back in;
- if you press no combo, filler or optional skills for longer than "Combo reset" (a slider in the overlay menu, "Combo" tab;
  5 s by default, not counting cooldown waits), the combo starts over.

A separate window can show the **combo layout** — the whole combo with the current step marked, vertical or
horizontal. Skill activation keys on the layout are turned on in the overlay menu, "Combo" tab → "Combo layout"; the combo preview on hover uses the same orientation.

> [!IMPORTANT]
> Turn on "Skill Usage History" in game and run the app **as administrator**: the game runs with administrator
> rights, and otherwise Windows does not show its key presses to the app. The first time you open the combo
> assistant, a window explains how the tracker works and what it cannot do.

The tracker learns what you used from three sources. "Skill Usage History" is the most accurate but up to 0.75 s
late, and the game skips some skills there (flow follow-ups and animation cancels, such as Seraph's Combo: Bone
Breaker). If the history skipped a skill that is on a cooldown slot, the tracker counts it when the slot goes on cooldown.
For skills that are neither in the history nor on a slot, clear "Seen in history or on a slot" in the editor's cell
panel (it is on by default and shared by every cell of the skill): only those skills count by key presses, by the
character's key bindings. In the cell's "Activation" you pick the command you use for the skill (any from the
description, one of its variants or your own keys — "Record", hold and release): the tracker shows it and listens only
to it. If "Skill Usage History" is hidden for the character (per `gamevariable.xml`), the tracker warns about it. A press
does not mean the skill went out (it may be pressed early, during the previous one), so a skill that spends a
noticeable amount of resource counts only when the resource bar under HP drops (any color, with or without Split HP).
Cheap skills and skills cast while a key is held count on the press itself; if a guess is wrong, the next history
line puts the tracker back. Where the history is missing (for example, in the Black Shrine), the tracker relies on
slots and keys and makes more mistakes.

The tracker, the layout and the marks over cooldown slots hide while the slots are covered by a game window; marks
disappear only from the covered slots.

Use Windowed Fullscreen. The app only reads the screen and key presses: it never touches the game, never intercepts
input and never presses keys.

> [!NOTE]
> The skill database comes from the open data of [bdolytics.com](https://bdolytics.com), so an internet
> connection is needed.

<details>
<summary>Where combos are kept</summary>

- Windows app — `Documents\Black Desert\BDO Tools\comboAssist\combos.json`, shared by all accounts: a combo is about
  a class, not an account. Next to it is `watch.log` — what the app saw on screen.
- Website — in the browser storage.

</details>
