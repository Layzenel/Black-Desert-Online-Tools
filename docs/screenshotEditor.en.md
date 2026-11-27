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

Windows app only. When you take a screenshot in the game, "New screenshot" with three buttons appears on the right
over the game for 8 seconds:

- "Edit" opens the app on this tab with the screenshot already open;
- "Copy" puts the screenshot on the clipboard;
- "Crop" shows the screenshot in the middle of the screen: drag over an area and click the green check mark, and the
  cut-out part goes to the clipboard. The cross in the window corner closes it without copying.


- The buttons can be clicked only while the cursor is shown; in combat clicks go through the window to the game.
- The window can be dragged, its place is remembered.
- To turn it off — "Offer to edit screenshots" in the overlay menu, "Settings" tab.

### Tools

Above the screenshot: "Discard changes", undo (Ctrl+Z), redo (Ctrl+Y), the tools, rotation, flip, comparison with the
original and zoom. The mouse wheel over the screenshot zooms in at the cursor and moves the same zoom slider; holding
the wheel down pans the picture.
The selected tool's settings are in the menu on the left.

- **Crop** — drag over the area that should stay and click the green check mark under it (or Enter); until then the
  frame can be dragged. The red cross, a right click, a click outside and Esc cancel the crop. After the check mark only
  the selected part stays on the canvas; Ctrl+Z brings back the whole picture. Aspect ratio: free, 16:9, 1:1, 9:16.
- **Hide** — the selected area gets pixelated: a name, family, guild or chat.
- **Arrow**, **Frame**, **Brush** — color and thickness in the menu. Thickness follows the screenshot size: a line
  on 4K looks the same as on Full HD.
- **Text** — type the text in the menu and click the screenshot where it should start.
- **Rotate** 90° clockwise and **flip** horizontally — you can keep drawing on the rotated picture.
- **Compare with the original** — while the button is on, the screenshot is shown without edits.
- **Select and move** — a click on an arrow, frame, line, caption or hidden area outlines it with a dashed line: drag
  it to move it, the cross above it (or Delete) removes it.

### Retouch

- The section is collapsed: click the "Retouch" header to open it.
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
- "Use as portrait background" opens the edited picture in the [Portrait Constructor](portraitConstructor.en.md), as
  after "Upload": fit it to the mask and click "Apply".
- "Gallery" closes the screenshot; with unsaved edits it asks first.
