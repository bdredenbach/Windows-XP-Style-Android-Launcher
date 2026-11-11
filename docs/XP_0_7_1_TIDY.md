# Nth Desktop XP 0.7.1 — final menu and control polish

## Changes

- Original 16-unit notification icons replace larger desktop/modern glyphs.
  Battery/charging, connected/disconnected/limited-network and muted speaker
  states reflect real Android status. Quick Launch/task glyphs also use 16dp
  artwork while retaining the existing touch targets and independent row sizes.
- Tray icons support touch holds, mouse context clicks and Menu-key commands.
  Volume, network, power, brightness, input language and removable-storage menus
  route to working Nth surfaces or the corresponding Android setting.
- Clock context commands open Date/Time, notification customization, taskbar
  locking and Properties. Tray menus participate in auto-hide pinning and close
  with the launcher, other surfaces or changed window bounds.
- The volume popup uses an XP vertical trackbar, Mute and Sound Properties.
  Touch, D-pad and accessibility changes affect the real media stream. Mute
  remembers the last nonzero setting when the popup is closed and reopened.
  Fixed-volume outputs disable adjustment. The popup never changes ringer/DND.
- Customize Notifications stages icon choices until OK; Cancel leaves settings
  alone. Existing portable backup keys retain the choices. It is available from
  tray/clock menus and Taskbar Properties, and extra icons can still be expanded.
- All tray panels use XP captions and themed property-sheet surfaces. Their
  dimensions respect the work area, taskbar, safe insets and orientation.
- Shared context menus use small artwork, checked gutters, separators, blue/white
  selection and readable disabled text. Program submenu arrows align at the edge.
- XP trackbars, segmented progress meters and radio/check controls replace
  Material styling in settings, media, the recorder and transfer wizard. Native
  Android control semantics and value listeners are retained.
- Built-in accessory toolbars use flat XP states. Their status bars follow the
  selected theme. Dialog captions remain readable in Classic, including white
  text on its blue caption, and commands have spaced XP buttons.
- Low-battery balloons have the pale yellow body/tail and remain within safe
  desktop bounds. No system overlay or background notification service is added.

## Verification

`XpTidyTest` adds checks for vertical-slider touch/keyboard/accessibility values,
native radio/check selection behavior, disabled menus and selected-check
contrast, notification OK/Cancel/backup, real audio mute restoration and Back,
status-dependent artwork, and 84 tray/menu/dialog renders across six themes in
portrait and landscape. The earlier shell, game/editor, ZIP/search, backup,
theme, taskbar-row and media suites remain enabled.

`XpTidySmoke` uses native touch holds, real volume drags and accessibility/key
actions, actual stream mute/restore, notification dialogs and backup state,
clock routing, brightness changes and safe flyout bounds. It opens twelve
existing programs in both orientations; the separate feature runner opens all
sixteen XP additions and exercises documents, games and provider ZIP/search.
CI runs all native suites on Android API 30 and 36.

Final commit, run IDs, results and signed APK identity are recorded in the build
verification notes delivered with the APK. Physical microphone/TTS/Pixel 10 and
device-specific Android settings remain separate acceptance checks.

## Visual reference

Microsoft's notification-area documentation identifies XP's collapsible tray
and small notification artwork. The existing archived XP sources listed in
`XP_FEATURE_COVERAGE.md` remain the accessory inventory. Later Windows Action
Center styling is not used as an XP visual reference.

- https://learn.microsoft.com/en-us/windows/win32/uxguide/winenv-notification
- https://learn.microsoft.com/en-us/windows/win32/menurc/about-icons
