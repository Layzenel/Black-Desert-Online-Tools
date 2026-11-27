[← BDO Tools](../README.en.md) · [Русский](screenshotEditor.md) · **English**

# Screenshot Editor

Get a game screenshot ready to share in a minute: crop it, hide names and chat, point at what matters.
Works on the website in any browser and in the Windows app.

### Open

- "Open" — choose a screenshot. The game saves them to `Documents\Black Desert\ScreenShot`; in the app the file
  dialog opens right there.
- The original stays untouched: all edits go to a copy.

### Tools

Above the screenshot: undo (Ctrl+Z), redo (Ctrl+Y) and the tools; the selected tool's settings are in the menu on the left.

- **Crop** — drag over the area that should stay. Aspect ratio: free, 16:9, 1:1, 9:16. Until you save, the whole
  picture stays visible with the rest dimmed; "Reset crop" brings back the whole picture.
- **Hide** — the selected area gets pixelated: a name, family, guild or chat.
- **Arrow**, **Frame**, **Brush** — color and thickness in the menu. Thickness follows the screenshot size: a line
  on 4K looks the same as on Full HD.
- **Text** — type the text in the menu and click the screenshot where it should start.

### Save

- Size — 100, 75, 50 or 25 %; the resulting size in pixels is shown under "Saving".
- PNG is lossless, JPG is several times lighter (handy for Discord).
- "Copy" — the edited picture goes to the clipboard to paste into a chat right away. The browser may ask for permission.
- "Save copy" — the website downloads the file to the browser's downloads, the app saves it to
  `Documents\Black Desert\BDO Tools\screenshotEditor\` as `<screenshot>_edited.png` (or `.jpg`);
  a taken name gets a number.
