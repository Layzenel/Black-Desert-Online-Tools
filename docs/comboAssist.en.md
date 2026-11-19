[← BDO Tools](../README.en.md) · [Русский](comboAssist.md) · **English**

# Combo Assist

Your own combo for a class and an in-game hint showing which skill to press next.

The editor also works on the website, but only the Windows app can watch the game and show the hint on top of it
(see [Overlay](overlay.en.md)).

## Editor

- On the left — "Class" and "Saved combos". A combo belongs to a class and fits all of its characters.
- "Skills" — the class skills with search and the "Succession", "Awakening" or "All" stances.
- Select a cell and click a skill on the left: a new cell appears after it, and "Can follow" above the list shows
  what usually comes next, with chains marked.
- Drag an icon onto another cell to swap them. Right-click a cell for "Delete cell", "Insert left",
  "Insert right".
- **"Chain"** — the skill is pressed right after the previous one, or the chain breaks.
- **"Optional"** — the overlay does not suggest it, but if you use it the combo continues from there.
- **"Note"** — your own text for a step ("while jumping", "3 hits"): the tracker shows it when this step is next.
- **Forks** — "Make a fork" and "+ branch": several ways to continue, the hint picks one by cooldowns. A fork can
  be linked to the previous one: enter the second branch, and the hint follows the second branch there too.
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
  below;
- the step note, if there is one, is shown under the tracker;
- if you used a skill out of order, the hint continues from it; if a chain broke, it shows how to get back in.

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
