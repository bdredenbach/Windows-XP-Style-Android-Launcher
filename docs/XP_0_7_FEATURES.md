# XP feature expansion — 0.7.0

See [the coverage audit](XP_FEATURE_COVERAGE.md) for the feature-by-feature
comparison, sources, Android mappings and substantial remaining gaps.

## Integration

`XpPrograms` is the common catalog for sixteen new programs. It supplies Start
flyouts, search, Run aliases, labels/icons, shortcut organization, backup
validation and session restoration. `XpToolPanel` supplies the shared utility
frame and lifecycle hooks. The Activity owns document picking, microphone
permission, app-local keyboard input, magnifier snapshots and restore previews.
No extra application permissions were added.

Paint and Sound Recorder retain their local working files in private app storage.
WordPad's text formatting/draft is included in portable configuration backups.
Game states use a separate private preference file. Restore points keep the
current Notepad/WordPad/gadget drafts when preparing a configuration restore.
The transfer wizard uses the existing validated portable-backup pipeline.

`XpDocuments` stages an export before showing Android's picker and stores the
pending tool/operation independently of a View. Reads and writes run on its
worker. The input/export limit is 8 MB; individual parsers impose smaller limits.
Recorder output is AAC in M4A, capped to 10 minutes/8 MB, and stopped/released on
leaving the Activity or closing the tool. Narrator also stops on backgrounding.
Magnifier images remain in memory and omit private browser windows.

`XpZip` rejects unsafe or colliding paths and validates/decompresses an archive
before creating its extraction directory. ZIP creation/extraction use new
provider documents, never overwrite existing user files, and keep originals.
Operations are bounded to 2,000 entries, 16 path levels and 128 MB. A failed
operation attempts to remove its newly created incomplete output. Cancellation
uses the existing Explorer operation controls.

## Checks

The feature unit/native-graphics suite covers registration, new tool shortcut
backups, bitmap fill/undo, Unicode RTF formatting, minefield safety/wins/state,
Klondike/FreeCell moves and undo, hostile ZIP paths, wildcards, preserved drafts,
and renders of all programs in portrait and landscape. The native feature runner
opens all sixteen actual programs in both orientations, exercises edits and game
rules, creates/extracts a real provider ZIP and searches the resulting nested tree.
Its synthetic document provider is declared only in the instrumentation APK.

The existing shell, theme, caption-motion and independent-row suites remain part
of CI. Final run IDs, APK identity and physical-device acceptance are recorded in
the delivered verification notes.
