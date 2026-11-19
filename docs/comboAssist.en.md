[← BDO Tools](../README.en.md) · [Русский](comboAssist.md) · **English**

# Combo Assist

Your own combo for a class and an in-game hint showing which skill to press next.

The tool is in the Windows app only; the website does not have it. For the in-game hint, see [Overlay](overlay.en.md).

## Editor

- On the left — "Class" and "Saved combos". A combo belongs to a class and fits all of its characters.
- "Skills" — the class skills with search and the "Succession", "Awakening" or "All" stances.
- Select a cell and click a skill on the left: a new cell appears after it, and "Can follow" above the list shows
  what usually comes next, with chains marked.
- Drag an icon to an edge of another cell: the left or right edge puts the skill before or after it, the top or
  bottom edge makes it a new fork branch above or below. A blue line shows where it goes.
  Drop a branch cell to the left or right of itself to take it out of the fork; a two-branch fork closes. Right-click a cell for "Delete cell", "Insert left",
  "Insert right".
- **"Chain"** — the skill is pressed right after the previous one, or the chain breaks.
- **"Optional"** — the overlay keeps it out of the combo path and shows it aside in a dashed frame; if you use it,
  the combo continues from there.
- **"Note"** — your own text for a step ("while jumping", "3 hits"): the tracker shows it when this step is next.
- **Forks** — "Make a fork" and "+ branch": several ways to continue. The hint picks one by cooldowns, and when
  branches are ready at about the same time it takes them in turn: 1.1, then 1.2, then 1.3.
- **"Fillers"** — skills the overlay suggests while the next step is on cooldown.
- The editor warns about empty cells and about a chain continuation without its starter.

## Overlay settings

"Overlay settings" mode (Windows app only) lets you pick the tracker style and see it right away; below it are the
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
- optional skills you can slot in now are shown aside in dashed frames (on the left in "Focus");
- the step note, if there is one, is shown under the tracker;
- if you used a skill out of order or another fork branch, the hint continues from it; if a chain broke, it shows how to get back in;
- if you press no combo skills or fillers for 5 seconds (not counting cooldown waits), the combo starts over.

A separate window can show the **combo layout** — the whole combo with the current step marked, vertical or
horizontal.

> [!IMPORTANT]
> The tracker works only when "Skill Usage History" is turned on in game. In some places, such as the Black Shrine,
> the history is not shown, so the tracker does not work there.

Use Windowed Fullscreen. The app only reads the screen: it never touches the game and never presses keys.

> [!NOTE]
> The skill database comes from the open data of [bdolytics.com](https://bdolytics.com), so an internet
> connection is needed.

<details>
<summary>Where combos are kept</summary>

- Windows app — `Documents\Black Desert\BDO Tools\comboAssist\combos.json`, shared by all accounts: a combo is about
  a class, not an account. Next to it is `watch.log` — what the app saw on screen.
- Website — in the browser storage.

</details>
