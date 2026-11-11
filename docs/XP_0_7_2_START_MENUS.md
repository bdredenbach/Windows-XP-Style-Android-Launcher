# XP 0.7.2 — Start menu context actions

## Research

XP program shortcuts offered Open, pin/unpin, Send To, Copy, removal, Rename
and Properties. All Programs shortcuts could be deleted independently of the
installed program. XP ordinarily uninstalled through Control Panel's Add or
Remove Programs or a vendor-provided uninstall shortcut. The launcher keeps
its explicit Android Uninstall extension for removable packages. Run as is not
shown because Android cannot launch another device user's app on demand.

Primary references:
- Microsoft, pin list versus frequently used list:
  https://devblogs.microsoft.com/oldnewthing/20050616-57/?p=35303
- University of Dubuque, original Windows XP desktop shortcut instructions:
  https://www.dbq.edu/media/CampusLife/OfficeOfTechnology/FAQ/WindowsXP-Desktop-Shortcuts.pdf
- University of Dubuque, original XP Start > Programs > Accessories > Explorer:
  https://www.dbq.edu/media/CampusLife/OfficeOfTechnology/FAQ/WindowsXP-Explorer.pdf
- Microsoft, XP Add or Remove Programs / Set Program Access and Defaults:
  https://learn.microsoft.com/en-us/windows/win32/shell/cpl-setprogramaccess

## Implemented behavior

- Home programs, All Programs, Accessories/Games and deeper groups use the same
  hold, secondary mouse button, context-click and Menu-key binding. A hold does
  not launch the selected program. Touch scrolling remains native.
- Send To > Desktop (create shortcut) is idempotent. Copy uses the existing
  launcher clipboard. Cut in All Programs or on a stored Start pin moves only that shortcut when pasted
  onto the desktop or into an app folder. Custom app folders also accept Paste.
- Pin adds/removes a Start pin. Unpin allows an ordinary app to remain in recent
  programs. Remove from This List suppresses it from the home list only.
- Delete in All Programs asks to remove only its Start shortcut. Desktop pins,
  installed apps and custom folder contents survive. Holding All Programs offers
  Restore deleted shortcuts. Sort by Name orders the program lists by display
  name. All Programs remains searchable even when a shortcut was removed.
- Right-side destinations use applicable Open/Explore, Send To, Copy, Properties
  and desktop visibility commands. My Computer Manage opens real device System
  Information; Properties opens System. Built-in virtual program groups offer
  Open, Sort by Name and Properties rather than unsupported filesystem edits.
- Recent documents offer Open, history-only removal and metadata Properties.
  Profile/account and session commands have applicable menus. Properties for an
  installed app show its real package/version and open Android App info.
- Built-in/system packages cannot use the removable-app Uninstall command.
  Android owns uninstall confirmation; dismissing it preserves app and shortcuts.
- Start exclusions, deleted shortcuts and sorting are included in validated
  .nthdesktop exports. Existing defaults and desktop settings are preserved.

## Verification

XpStartContextTest covers shortcut idempotence, pin/removal distinction, All
Programs and nested hold routing, folder-safe deletion/cancel/restoration, real
clipboard copy/cut destinations, system-link actions, package properties, Android
uninstall intents, recent-document removal and backup validation. Existing
StartUninstallMenuTest now uses the actual XP Start context handler.

XpStartMenuSmoke runs real touch holds on Activity and PopupWindow rows in
portrait and landscape, creates/repeats a desktop shortcut, removes only a Start
entry, pins a nested program, checks Properties/System routing and verifies a
removable installed fixture exposes Uninstall. Existing unit and Android API
30/36 native suites remain enabled. Final results are recorded with the APK.
