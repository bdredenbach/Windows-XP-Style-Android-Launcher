# XP training-guide polish — 0.4.0

## Visual references

- [LearningComputer XP training](https://www.learningcomputer.com/microsoftwindows/windowsxp/): the actual My Documents, icon view, Control Panel and Display Properties pictures. Viewed their full-size images under `blog/wp-content/uploads/2015/12/`, particularly `Windows_xp_My_Documents_icons.jpg`, `windows_xp_control_panel.jpg`, and `windows_xp_desktop.jpg`.
- [Introduction to Windows XP](https://www.slideshare.net/slideshow/introduction-to-windows-xp/128171926): viewed pages 5, 6, 10–12, including the folder tree, menu order, address row and file columns. These screenshots are references, not bundled assets.
- Existing licensed XP caption artwork, supplied wallpaper pack and Tahoma-compatible fonts retain the notices in ASSETS.md.

## Changes

Control Panel uses the guide's blue home, white icon labels, Pick a category heading and ten XP categories. Later applet names are replaced with Add or Remove Programs, Network Connections, Set Program Access and Defaults, Sounds and Audio Devices, and Accessibility Options. The sidebar has inset groups with white headers, light blue bodies and a 16dp gap. Category selection, Classic View, search and Android destinations still work. New folder/Control Panel windows use a 720dp cap so landscape can show the guide’s two category columns; saved sizes retain priority.

Explorer's common tasks, app folders and Recycle Bin receive separated groups, inner padding, wrapping links, classic menus and beige navigation/address chrome. Folders retains a white hierarchy with small boxed expand controls, explicit selected rows and deeper indentation. The reading pane and Include in library actions are removed. Saved library routes open the corresponding My folder; old library records remain accepted for backup compatibility.

Task Manager has Applications, Processes, Performance, Networking and Users. It uses compact column lists, blue selected rows, XP tabs, black/green history grids and segmented status fields. Performance places CPU/RAM usage meters beside histories instead of stacking modern dashboard sections. Processes shows real Nth-owned image name, PID and memory; Android apps remain recent task entries and use App Info. Users displays the current Android user. No foreign processes, Windows services, device-wide CPU or synthetic counters are claimed.

System, Taskbar and Date/Time open tabbed property sheets without the Control Panel navigation frame. Taskbar choices are staged until Apply or OK; Cancel keeps current settings. Its draft survives tab changes and saved UI state. Display Properties gains a pale CRT frame and smaller tabs. The wallpaper list shows supplied XP backgrounds and retained personal/current selections; Center/Tile/Stretch are offered. A legacy Fill/Fit choice is proposed as Stretch in the draft and is only changed if applied.

Removed from reachable UI: Jump Lists and upward-flick commands; live taskbar thumbnails/media preview controls; Peek; Flip 3D; automatic edge Snap and shake; Meta+Arrow/Meta+Home placement shortcuts; Libraries and Include in library; Gadget Gallery/desktop gadget commands; Action Center tray flag; the Windows 7 View/Sort by desktop context menu (replaced with Arrange Icons By), Organize/Favorite Links presentation and later Control Panel applet names. Alt+Tab is an icon switcher; taskbar groups are a caption list; holds show system/shortcut menus. Old Flip 3D identifiers redirect to the icon switcher, Gadget Gallery identifiers to Help and Support, and Action Center identifiers to Android security.

## Adaptations

Touch targets, scrolling app rows, .nthdesktop portability, saved Nth workspaces, built-in utilities, Android file access, modern WebView and supported media engines are retained. These are launcher features rather than Windows services. Imported gadget data remains in backups, but its desktop surface is hidden and has no XP shell entry point. Legacy class/preferences names do not select a Seven theme. Wallpaper IDs remain stable for cross-variant restore.

System tabs show actual Android information or route to real Android settings. Date/Time previews the current device clock and does not claim to change protected time settings. Task Manager's CPU is the Nth process relative to one core; RAM and traffic are device-wide where Android permits. Foreign processes/users and protected settings remain unavailable. This is an XP-adjacent Android implementation, not Windows emulation.

## Verification

Meaningful automated coverage includes task-group gaps/collapse, staged taskbar Apply/Cancel across tabs, Task Manager task-end/App-Info routes and meter/list separation, XP catalog names/search exclusions, and System/Date-Time tabs. The real Activity render test covers thirteen screens across all six schemes in portrait and landscape. Native checks cover the new applets, sidebar groups, staged taskbar changes, screenshots, and the existing independent-row gesture suite. Final run and results are recorded after verification.

A final Android 16 run exposed a timing failure during edge-drag verification. Taskbar reorder now blocks asynchronous focus-reveal scrolling until drop/cancel, so an earlier focus request cannot move the other row during a drag. The test still compares every opposite-row button's screen bounds and now snapshots them on the UI thread with before/after diagnostics. Initial Start-menu native taps resolve the current attached row on the UI thread and wait for the launched window rather than retaining an off-thread view reference. Native event injection and all opposite-row assertions remain.
