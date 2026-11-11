# Nth Desktop XP feature coverage — 0.7.0

This is an Android launcher with XP-inspired native tools. It does not run the
Windows XP kernel or Windows executables. This audit distinguishes implemented
features from Android integrations and remaining work. It is not a claim of
complete Windows XP compatibility.

## Added in this pass

All sixteen programs are registered in All Programs, Start search, Run, shortcut
organization, taskbar icons, portable shortcut backups and window restoration.
Accessories contains Accessibility, Entertainment and System Tools submenus.

| Program | Location / Run command | Working behavior | Scope or remaining detail |
|---|---|---|---|
| Paint | Accessories / `mspaint` | Pencil, brush, eraser, fill, line, rectangle, ellipse, text, palette, widths, bounded undo/redo, image import, PNG export and local draft | Bitmap editing; the complete XP selection/curve/airbrush toolbox remains future work |
| WordPad | Accessories / `write`, `wordpad` | Bold, italic, underline, font, size, alignment, bullets, undo/redo, clipboard, RTF/HTML/text open/export and saved rich draft | Text-only RTF subset; legacy DOC, OLE objects and embedded images are not supported |
| Character Map | System Tools / `charmap` | Six Unicode blocks, code/name search, character preview, collection and clipboard copy | Uses the bundled Tahoma-compatible and Android fallback glyphs |
| Command Prompt | Accessories / `cmd` | Help, clear, Nth directory listings, path selection, draft reading, echo, date/time/version, system report, task list, program launch and console colors | Nth command environment; no Windows executable or Android shell execution |
| Sound Recorder | Entertainment / `sndrec32` | Explicit microphone recording, input level, elapsed time, stop, playback and M4A export | Android AAC/M4A format; 10-minute / 8 MB limit; stops on leaving the launcher |
| Magnifier | Accessibility / `magnify` | Nth desktop capture, 2×/4×/8× zoom, panning and optional live refresh | App-only capture held in memory; private browser windows are excluded |
| Narrator | Accessibility / `narrator` | Read typed/pasted text or a visible Nth window, speech-rate control and Stop | Uses installed Android speech voices; full device screen reading remains Android accessibility |
| On-Screen Keyboard | Accessibility / `osk` | Floating letter/number keyboard, Shift, Space, Enter and code-point-aware Backspace | Types into visible Nth text fields; other Android apps use their system keyboard |
| System Information | System Tools / `msinfo32` | Real Android/device summary, RAM, volume space, display, network and sensors; copy/text export | No Windows hardware or driver information is fabricated |
| Disk Cleanup | System Tools / `cleanmgr` | Analyze catalog cache, choose catalog/browser temporary files, confirm cleanup, open Android Storage | Nth temporary data; document drafts, paintings and recordings are retained |
| System Restore | System Tools / `rstrui` | Six named local desktop configuration points, dated list, preview and validated restore flow | Nth appearance/layout/shortcuts/settings; current document drafts are preserved |
| Files and Settings Transfer Wizard | System Tools / `migwiz` | Old/new device choice, explanation and guided export/import via portable backups | Moves Nth settings/drafts; Android folder permissions are reconnected on the destination |
| Search Companion | Start Search / `search` | Recursive file/folder search in granted roots, wildcard patterns, media/document filters, program/settings search and cancellation | 5,000 scanned items, 200 results, 15-second scan budget; inaccessible folders are skipped |
| Minesweeper | Games / `winmine` | Beginner/intermediate/expert, first-click-safe generation, flood reveal, flags, number chording, win/loss, timer, counter and saved board | Tap, hold/right-click and keyboard controls; custom boards/best-time dialog remain future work |
| Solitaire | Games / `sol` | Klondike, draw one/three, stock recycling, legal sequences/foundations, tap/drag moves, hints, undo, victory and saved deal | Original card drawing; stock animation/scoring variants remain future work |
| FreeCell | Games / `freecell` | Eight cascades, four free cells/foundations, alternating sequences, capacity-limited multi-card moves, hints, undo and saved deal | Classic core rules; numbered Microsoft deal compatibility remains future work |

### Explorer and keyboard additions

| Feature | Behavior |
|---|---|
| Compressed (zipped) Folders | Create ZIPs from selected files/folders, inspect contents and Extract All into a new folder |
| ZIP validation | Reject traversal, absolute/drive paths, duplicates and conflicting file/folder paths; cap expansion and entry counts; keep source files |
| Image/text editing | Explorer offers Edit with Paint and Edit with WordPad for appropriate files |
| Run aliases | Names above accept `.exe` suffixes as aliases for Nth tools |
| Keyboard entry points | Win+R opens Run; Win+F/F3 opens Search Companion; F1 opens Help; Ctrl+Esc opens Start, when Android delivers those keys to the launcher |
| Control Panel links | System Information, Disk Cleanup, System Restore, Transfer Wizard and accessibility programs are discoverable from their related categories |

## Already implemented before this pass

| XP area | Existing coverage |
|---|---|
| Desktop | Icons, labels, spacing, arrangement, custom app folders, context menus and portable `.nthdesktop` settings |
| Start | Profile header, two columns, pinned/recent programs, places, All Programs flyouts, Run, logoff and power entry points |
| Taskbar | Quick Launch, task labels/grouping, fixed bottom controls, expanded rows, separate row scrolling/reordering, Lock and Auto-hide |
| Windows | XP captions and controls, move/resize, maximize/minimize/restore, system menu, saved geometry, task switching and XP-style caption motion |
| Themes | Luna Blue, Olive and Silver plus Royale, Zune and Classic choices; readable themed chrome and property sheets |
| Desktop appearance | Twenty stock wallpaper IDs, HD replacements and portrait alternatives, personal backgrounds and screen-saver framework |
| Explorer | Permission-scoped folder browsing, breadcrumbs/address navigation, Back/Forward/Up, details/icon/list views, sorting/grouping and folder options |
| File operations | New folder, rename, cut/copy/paste/move, selection, properties, Android sharing, linked folders and search links |
| Recycle Bin | Recovery copies, restore, permanent deletion, capacity options and properties for files deleted through Nth Explorer |
| Notepad | Plain-text open/save, UTF-8/UTF-16/ANSI decoding, find, wrap/font-size options, status bar, clipboard and local draft |
| Media | Shared audio/video session, local media library, playlists, seek, volume, album art, four audio-driven visuals and Media Center view |
| Pictures | Picture and Fax Viewer-style photo browsing, orientation/view controls, slideshow and Android sharing/printing entry points |
| Control Panel | Ten XP category groups, classic list, Display, System, Taskbar and Date/Time property surfaces |
| Task Manager | Nth windows/tasks, Android app handoffs, app/process-scoped CPU and memory information |
| Accounts/session | Desktop name/avatar, Welcome, Log Off and power panels with explicit Android actions |
| Backup/workspaces | Validated portable settings backups, preview/undo restore and named window-layout workspaces |
| Help | Native welcome/help content and permission guidance |

## Android integrations

| XP concept | Current Android treatment |
|---|---|
| Network Connections / Internet setup | Android network and Wi-Fi settings; real connectivity status |
| Bluetooth / printer setup / device hardware | Android's corresponding system settings and supported intents |
| Security Center / firewall / antivirus / updates | Android security, privacy and update controls; no invented protection status |
| User accounts / credentials / screen lock | Android account and security controls; the Nth profile remains a launcher identity |
| Outlook Express / mail | Default Android mail application; no embedded mail or newsgroup client |
| Filesystem permissions / device-wide file access | Android document-provider grants and optional supported shared-storage access |
| Accessibility across other apps | Android accessibility and input-method settings |
| Shutdown, sleep and OS user switching | The session panel identifies the real Android action that is available |
| Application installation/removal/defaults | Android package and default-application flows |

## Remaining substantial XP features

| Feature | Why it is not reported as implemented |
|---|---|
| Hearts, Spider Solitaire, 3D Pinball and Internet games | Separate game logic, opponents/physics or network services are still needed |
| Address Book / Outlook Express / Windows Messenger | Contact/mail/message data models and account/protocol integrations are not present |
| Windows Movie Maker | Timeline editing, effects, audio mixing and video export are a separate media-editing project |
| Remote Desktop / Remote Assistance / NetMeeting | A real remote-session protocol and consent/session lifecycle are required |
| Scheduled Tasks | A durable Android scheduling/notification design is still needed; no fake Windows task scheduler is exposed |
| Offline Files / Synchronize / network file sharing | Requires explicit remote-storage protocols and conflict-aware synchronization |
| Full WordPad DOC/embedded-object support | The current editor intentionally supports text/HTML and a text-formatting RTF subset |
| Full Paint toolbox | Selection, cropping, curves, airbrush and richer canvas-size controls remain |
| Original XP screen-saver binaries, boot implementation and every stock sound | Android rendering/lifecycle replacements are used; Windows binaries are not executed |
| Disk Defragmenter / Check Disk / Device Manager drivers | Android owns flash storage, device drivers and system maintenance |
| Registry Editor / Group Policy / domain join / NTFS EFS | These depend on Windows services, filesystems and the Windows security model |
| Win32 application compatibility / DOS execution | Requires an emulation/compatibility environment beyond this native launcher |

## Research sources

The inventory was checked against Microsoft's XP accessibility/program testing
list, the Microsoft Press XP Registry Guide's program aliases, Microsoft's
archived Games knowledge-base article, and Microsoft's ZIP/System Restore docs.
Current Windows 10/11 support pages were not used as proof of XP behavior.

- Microsoft: Testing Assistive Technology for Compatibility with Windows XP,
  including its Accessories/System Tools table:
  https://learn.microsoft.com/en-us/previous-versions/windows/desktop/dnacc/testing-assistive-technology-for-compatibility-with-microsoft-windows-xp
- Microsoft Press: Windows XP Registry Guide, archived full book with accessory
  executable names:
  https://ebooks.hgs.name/microsoft%20press%20windows%20xp%20registry%20guide.pdf
- Archived Microsoft KB 307768, default XP games:
  https://ftp.zx.net.nz/pub/mirror/ftp.microsoft.com/MISC/KB/en-us/307/768.HTM
- Microsoft: Compressed (zipped) Folders description in MS04-034:
  https://learn.microsoft.com/en-us/security-updates/securitybulletins/2004/ms04-034
- Microsoft: About System Restore, including XP checkpoint behavior:
  https://learn.microsoft.com/en-us/windows/win32/sr/about-system-restore
- Archived Microsoft KB 307773, XP accessibility/Utility Manager tools:
  https://ftp.zx.net.nz/pub/archive/ftp.microsoft.com/MISC/KB/en-us/307/773.HTM

Physical Pixel 10 acceptance remains separate from emulator/native-graphics tests.
