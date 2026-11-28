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
original and zoom. Right after opening no tool is selected: a click only selects and moves marks.
The selected tool's settings are in the menu on the left.

- **Crop** — drag over the area that should stay and click the green check mark under it (or Enter); until then the
  frame can be dragged. The red cross, a click outside and Esc cancel the crop. After the check mark only the selected
  part stays on the canvas; Ctrl+Z brings back the whole picture. Aspect ratio: free, 16:9, 1:1, 9:16.
- **Hide** — the selected area gets pixelated or blurred: a name, family, guild or chat. The method and strength are in the menu.
- **Arrow** — hovering the button opens a list of types: arrow, double, dashed, curved, line.
- **Frame** — shapes in the list on hover: rectangle, circle, diamond, triangle, star and "Highlighter" —
  a translucent fill, like a marker.
- **Brush** — draw while holding the mouse button.
- **Text** — click the screenshot and type right on it; Enter to finish, Esc to cancel. Styles in the list on hover:
  with shadow, on a dark plate, outlined. Double-click a caption to edit its text.
- **Number** — each click places a circle with the next number to show the order of steps.
- Color and thickness are in the menu on the left. The eyedropper next to the palette takes a color from the
  screenshot, and picked colors stay in the palette. Thickness follows the screenshot size: a line on 4K looks the
  same as on Full HD.
- **Rotate** 90° clockwise and **flip** horizontally — you can keep drawing on the rotated picture.
- **Compare with the original** — while the button is on, the screenshot is shown without edits.

**Marks on the screenshot**

- A new mark is selected right away; a click on a mark selects it: it gets a dashed outline and squares.
- Drag a mark to move it — its edges and center snap to other marks and to the screenshot center, shown with gold lines.
- The corner squares (the ends of an arrow) resize it. The mouse wheel over the selected mark changes its thickness
  (strength for a hidden area), Shift+wheel changes its size.
- Color, thickness and type in the menu and the lists change the selected mark. Several such changes in a row are
  undone with one Ctrl+Z.
- Ctrl+C and Ctrl+V copy and paste a mark, Ctrl+D duplicates it; Ctrl+C without a selected mark copies the picture.
  The cross above a mark or Delete removes it.

**Mouse and menu**

- The mouse wheel over the screenshot zooms in at the cursor; holding the wheel or Space with the left button pans the picture.
- The right button opens a menu: edit a caption, copy, duplicate or delete the mark under the cursor, paste, undo,
  redo and the tools; arrow, frame and text show their types.

### Retouch

- The section is collapsed: click the "Retouch" header to open it.
- Ready-made filters: "Warm", "Cold", "Vivid", "Cinema", "B&W".
- Sliders: exposure, brightness, contrast, saturation, temperature, tint, vignette.
- Retouch changes only the screenshot itself: arrows, frames and captions keep their colors. Each slider move is
  undone with a single Ctrl+Z.

### Save

- The "Saving" block is at the bottom of the menu, above the buttons. Size — 100, 75, 50 or 25 %; the resulting size in pixels is shown under the title.
- PNG is lossless, JPG is several times lighter (handy for Discord).
- "Copy" — the edited picture goes to the clipboard to paste into a chat right away. The browser may ask for permission.
- "Save copy" — the website downloads the file to the browser's downloads, the app saves it to
  `Documents\Black Desert\BDO Tools\screenshotEditor\` as `<screenshot>_edited.png` (or `.jpg`);
  a taken name gets a number.
- "Use as portrait background" opens the edited picture in the [Portrait Constructor](portraitConstructor.en.md), as
  after "Upload": fit it to the mask and click "Apply".
- "Gallery" closes the screenshot; with unsaved edits it asks first.
