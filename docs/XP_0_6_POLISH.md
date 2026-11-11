# Nth Desktop XP — 0.6.0 polish

## Shell geometry

`XpTaskbar` paints one notification well behind the tray and clock across the
complete bottom row and up to the right edge. It replaces the two 40dp child
backgrounds that left an inset step inside the 44dp bar. No row assignments or
scroll positions are changed. Start docks flush with the bar and has a 610dp
portrait target, capped to the available safe work area. Both columns have a
clear divider, roomier places rows and an intact 40dp Run touch target.

## Motion and task switching

`XpWindowMotion` moves the caption between the window and its task button,
including grouped tasks and a clipped/off-screen-task fallback. Live content is
never scaled. Maximize/restore commits the final saved geometry once, then draws
the moving caption. Opening a built-in window has a short caption reveal. Close
remains immediate, as in the classic shell. Animations last 210ms and respect
both Reduce motion and Android's animator setting.

Canceling a minimize completes its hidden state; restore explicitly makes it
visible again. Closing, rotating or replacing a transition cancels its overlay
and stale callbacks. No screenshot of browser or media content is retained.

The switcher captures the current window order when opened. Tab and Shift+Tab
cycle; releasing Alt activates; Escape cancels; Enter and touch also work. The
classic popup has immediate icon highlighting, with no platform dialog animation.
A minimized selection restores with the caption effect. Touch Switch Windows
remains available from the taskbar menu on devices without a keyboard.
Android may reserve physical Alt+Tab for its own system switcher; these keys are
handled when delivered to Nth Desktop. It cannot animate other Android apps.

## XP-era media presentation

The player uses a blue left navigation rail, silver/blue transport deck, dark
Now Playing area and a Now Playing side list when the window is wide enough.
Transport glyphs are readable on the light deck; Stop pauses and resets the
current item. The playback session and Media Center integration are retained.

The visualization chooser offers Bars and Waves — Bars, Bars and Waves — Scope,
Ambience, Particle Rings and Album Art. Selection is remembered locally.
The effects are original Android renderers inspired by XP-era visualizations;
they do not load Windows DLLs or claim to reproduce Microsoft plug-ins exactly.

The player exposes 256 contiguous PCM samples with stereo downmixing and
24 log-spaced DFT bins. The renderers respond to those samples and energy,
limit redraws to about 30fps while visible and playing, and freeze when paused.
Reduced motion disables continuous animation. No microphone or audio-capture
permission is requested for the effects. Video keeps its existing TextureView.

## Research basis

- Microsoft DrawAnimatedRects describes the moving caption for icon opening,
  minimize and maximize, with support dating to Windows 2000:
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-drawanimatedrects
- Microsoft ANIMATIONINFO describes the minimize/restore animation preference:
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-animationinfo
- Raymond Chen describes classic Alt+Tab as an icon/Z-order switcher, contrasting
  the Vista Flip model. We found no primary evidence of an XP animated carousel;
  direct icon selection is the fidelity choice used here:
  https://devblogs.microsoft.com/oldnewthing/20080701-00/?p=21793
- The Windows Media Player SDK describes visualizations consuming snapshots of
  waveform and frequency data. Our effects use the same audio-driven principle:
  https://learn.microsoft.com/en-us/previous-versions/windows/desktop/wmp/developer-overview

## Verification

`XpExperiencePolishTest` adds six unit/native-graphics checks for the flush well,
interrupted motion, reduced motion, keyboard cycling, PCM/DFT and the rendered
Start/player/visualizer layouts. Existing shell and independent-row suites remain
required. `XpExperienceSmoke` adds native motion, keyboard, real WAV playback,
visualization movement/pause freeze, Stop and wide/portrait layout checks on both
CI APIs. Build and final CI details are included in the delivered verification
record. Physical Pixel 10 / Android 17 acceptance remains separate.
