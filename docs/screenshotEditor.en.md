[← BDO Tools](../README.en.md) · [Русский](screenshotEditor.md) · **English**

# Screenshot Editor

Get a game screenshot ready to share in a minute: crop it, hide names and chat, point at what matters, fix the colors.
Works on the website and in the Windows app. The original stays untouched: all edits go to a copy.

### Gallery

- The tab opens with a gallery of screenshots from `Documents\Black Desert\ScreenShot`, newest first. Click a tile
  to open it in the editor.
- The app finds the folder by itself. On the website choose it once ("Choose folder"; Chrome, Edge, Opera,
  Yandex Browser only) — the same folder the other website tools use.
- In the app, hovering a tile also offers "Show in folder" and "Move to bin" (the Windows Recycle Bin — the
  screenshot can be restored from there).
- "Open file" — any picture from disk, in any browser.

### A new screenshot straight from the game

Windows app only. When you take a screenshot in the game, "New screenshot — Edit" appears on the right over the game
for 8 seconds. The button opens the app on this tab with the screenshot already open.

- The button can be clicked only while the cursor is shown (Ctrl); in combat clicks go through the window to the game.
- The window can be dragged, its place is remembered.
- To turn it off — "Offer to edit screenshots" in the overlay menu, "Settings" tab.

### Tools

Above the screenshot: undo (Ctrl+Z), redo (Ctrl+Y), the tools, rotation, flip and comparison with the original.
The selected tool's settings are in the menu on the left.

- **Crop** — drag over the area that should stay. Aspect ratio: free, 16:9, 1:1, 9:16. Until you save, the whole
  picture stays visible with the rest dimmed; "Reset crop" brings back the whole picture.
- **Hide** — the selected area gets pixelated: a name, family, guild or chat.
- **Arrow**, **Frame**, **Brush** — color and thickness in the menu. Thickness follows the screenshot size: a line
  on 4K looks the same as on Full HD.
- **Text** — type the text in the menu and click the screenshot where it should start.
- **Rotate** 90° clockwise and **flip** horizontally — you can keep drawing on the rotated picture.
- **Compare with the original** — while the button is on, the screenshot is shown without edits.

### Retouch

- Ready-made filters: "Warm", "Cold", "Vivid", "Cinema", "B&W".
- Sliders: exposure, brightness, contrast, saturation, temperature, tint, vignette.
- Retouch changes only the screenshot itself: arrows, frames and captions keep their colors. Each slider move is
  undone with a single Ctrl+Z.

### Save

- Size — 100, 75, 50 or 25 %; the resulting size in pixels is shown under "Saving".
- PNG is lossless, JPG is several times lighter (handy for Discord).
- "Copy" — the edited picture goes to the clipboard to paste into a chat right away. The browser may ask for permission.
- "Save copy" — the website downloads the file to the browser's downloads, the app saves it to
  `Documents\Black Desert\BDO Tools\screenshotEditor\` as `<screenshot>_edited.png` (or `.jpg`);
  a taken name gets a number.
- "Gallery" closes the screenshot: unsaved edits are lost.
