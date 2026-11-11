# XP shell upgrade — 0.5.0

Reconstructed from the owner's screenshot of the stalled implementation pass.
The starting source is the verified XP 0.4.0 main branch at
`977625b0c168ed87176b04ce516dd19b3453c09e`.

## Wallpapers and typography

The user-supplied Windows XP HD Wallpaper Pack 5.0 replaces twelve existing
landscapes. Bliss, Ascent, Autumn, Azul and Radiance also have portrait assets.
`XpWallpapers` preserves all twenty wallpaper IDs, selects the matching orientation,
decodes images with a power-of-two sample and keeps a bounded byte-counted cache.
Thumbnails are separately sampled. Crystal, Home, Moon Flower, Peace, Power,
Purple Flower, Ripple and Windows XP keep their earlier originals. The archive's
unedited Moon Flower is a different composition and is not substituted.

The source names, dimensions and packed asset paths are recorded in
`third_party/xp-hd-wallpapers/asset-map.json`. Archive notes and provenance are
retained alongside the source; a short provenance notice is bundled in the APK.
The old Aurora WebPs and unused Selawik asset fonts are removed. Legacy Nth
background IDs continue to render their procedural fallback or original Nth scenery.

Captions load the separate bundled Wine Tahoma bold font. Window controls have
inactive and disabled treatments; property windows gain a working question-mark
help button. Shared dialogs use the current XP scheme rather than Vista glass.
The existing Wine and XP.css notices remain bundled.

## Sessions

Turn Off and Log Off now open XP-style blue panels with colored native controls.
Every command identifies its Android action. Stand By opens Android lock settings,
Turn Off and Switch User open Home selection, Restart recreates the launcher,
and Log Off opens the XP-style Welcome screen. The account returns to the existing
desktop. These controls do not power off Android, switch Android users or act as
device authentication. Panels resize on orientation changes and restore on Activity
recreation. Existing open Nth windows are retained.

## Taskbar and tray

Properties and the hold menu expose Lock and Auto-hide. These options default off,
preserving existing task and row behavior. Lock prevents task-button and row
reordering and hold-menu expansion changes; it does not disable launching apps.
The Properties sheet stages every option until Apply/OK and can configure the
row count along with the lock setting.

The taskbar sits in a root overlay above a work-area reservation. Normal mode
reserves its complete height; enabling auto-hide reserves a narrow reveal edge.
After the setting change, hiding and revealing affect only visibility, keeping
the work area, window bounds and both row offsets stable. Touch or mouse hover
reveals the bar. Open Start, tray panels, menus and row drags keep it visible.
Start and tray panels use an inset above the overlay; fixed controls remain in
the original bottom row. Row scrolling and the existing drag/drop implementation
are retained.

Optional tray controls include a language indicator opening Android's input-method
picker, and removable storage shown only when StorageManager reports a mounted
removable volume. Foreground media-state broadcasts refresh storage status.
A dismissible XP-style balloon reports a real low-battery condition, once per
discharge cycle, without requiring notification or overlay permissions.

`xp_taskbar_auto_hide`, `xp_taskbar_locked`, `tray_language`, `tray_removable`
and `tray_balloons` are validated booleans in `.nthdesktop` backups. Existing
wallpaper IDs, row keys, older backups and application/signing identity are kept.

## Validation

See the delivered build-verification record for final results. The test additions
cover HD/portrait assets and decode bounds, staged taskbar options and backup
round trips, fixed work-area geometry and independent offsets through auto-hide,
taskbar locking without blocking launches, caption states/help, and both
orientations of the actual launcher and session panels. Existing unit and
installed-app row suites remain required.

Physical Pixel 10 acceptance is separate from emulator/native-graphics checks.
