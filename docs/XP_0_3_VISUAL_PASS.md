# Nth Desktop XP 0.3.0 — visual pass

## Reference sources

- Microsoft Windows XP Visual Guidelines, archived original:
  https://www.retrospace.net/download/WebApplications/WindowsXPDesignGuidelines/controls.htm
  https://www.retrospace.net/download/WebApplications/WindowsXPDesignGuidelines/whatsnew.htm
- Original XP captures: https://guidebookgallery.org/screenshots/winxppro/
- XP.css implementation/artwork: https://github.com/botoxparty/XP.css
- Wine font source: https://github.com/wine-mirror/wine/tree/wine-8.0/fonts
- User's training material and wallpaper references remain in XP_0_2_FIDELITY.md.

The documentation and screenshots are references only. MIT caption source artwork
and LGPL replacement fonts are attributed in ASSETS.md and third_party notices.

## Changes

| Area | Implemented treatment |
| --- | --- |
| Typography | Wine Tahoma replacement throughout native text/controls, with regular and bold faces |
| Windows | 34dp caption, 3dp frame, smaller icon, 32dp caption touch targets with 25dp artwork, blue Luna contours, classic bevels, smaller resize-grip decoration |
| Controls | Beveled command buttons, orange selected/hover tab edge, green checks, XP scrollbar colors, solid blue menu highlight with white text |
| Start | Compact two-column proportions, Internet/E-mail slots, pinned/frequent grouping, compact Places, colored footer navigation signs |
| Taskbar | 40dp app rows, 44dp base bar, single-line clock, lighter notification area, visible network/volume icons, Show Desktop next to Quick Launch |
| Desktop | 36dp default icon; explicit custom size retained; tighter vertical spacing |
| Explorer | Green Back/Forward, folder Up, Search/Folders/View toolbar, separate Address row, toggled inline search, existing file commands retained in menus |
| Notepad | XP menu bar, white plain-text editor, line/column status, Undo/edit commands, Find, Word Wrap, font sizes, real Open/Save/Save As |
| Browser | IE6-style menu, beige toolbar and separate address row; optional modern tabs hidden for a single page; modern WebView engine retained |
| Media Player | XP-era blue side navigation and menu bar, silver-blue transport deck; existing library, queue and playback engine |
| Control Panel | Blue task pane, category wording and path address; Display Properties remains its five-tab applet |
| Picture viewer | XP-neutral surfaces, smaller toolbar glyphs and Picture and Fax Viewer naming |

## Behavior and adaptation

Desktop apps still open with a tap. Android touch targets are larger than the
original desktop pixel sizes. Resizing, multi-window state, installed-app launch,
backup compatibility, shortcut hold/right-click and independent taskbar-row
scrolling/reordering are retained. Window/taskbar geometry uses shared constants
so hit testing and painting follow the same row sizes.

Notepad keeps an unfinished draft in the existing note setting. Open accepts UTF-8,
UTF-16 BOM and Windows-1252 text; imported CRLF line endings become LF internally.
Imports over 1MB or 200,000 characters are rejected instead of truncated. Explicit
Save/Save As writes UTF-8 through Android's document provider. File associations
are held for the current launcher session; after a launcher recreation, Save may
ask for a destination again. A closed document invalidates pending read callbacks.

E-mail and Printers and Faxes open actual Android handlers/settings. Device power,
sign-in, screen resolution and other privileged actions remain Android-owned.
Media Center retains its blue ten-foot Nth layout. Optional multi-tab browsing,
extra tools and Android-specific settings are Nth extensions.

## Verification

The local unit/render checks and installed-app workflow exercise these changes.
Native captures cover Start, Explorer, Display Properties, Notepad, browser,
Media Player, Control Panel and all six schemes in both orientations. The app-row
suite checks real swipes, drags, fixed controls, rotation, persistence and backup
using the new shared row dimensions. The final run and results are recorded below
when the workflow completes.
