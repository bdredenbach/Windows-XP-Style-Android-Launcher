# XP supplied desktop icons (0.1.1)

The owner supplied `windows-xp-desktop.zip`. Its five original ICO files and README are preserved under `third_party/xp-desktop-icons/` with SHA-256 metadata. The supplied README declares “Released to Public Domain” and identifies https://www.rw-designer.com/icon-set/windows-xp-desktop as its source. This records the supplied provenance; no ownership or authorship has been inferred.

The unmodified embedded 16/24/32/48 pixel representations, where present, are decoded to transparent PNGs under `app/src/main/assets/xp-icons/`. My Computer, Network, Documents and the empty/full Recycle Bin states use these assets throughout shared icon surfaces. The small tray network button uses the same supplied Network asset. The nearest available size at or above the requested pixel size is selected, capped at the original 48-pixel representation.

The N XP app identity, Start button, procedural Nth hill wallpaper and other XP-era drawings remain Nth artwork. The sections below document the shared foundation's earlier artwork history.

# Nth Desktop artwork

The wallpaper is bundled at `app/src/main/assets/wallpapers/aero-aurora.webp` (941 × 1672 pixels). It is encoded as **lossless WebP**: a pixel comparison against the generated PNG confirmed identical RGB pixels. Desktop chrome, icons and live gadget faces are drawn natively and remain sharp at the device's screen resolution.

The wallpaper was produced using the built-in image-generation tool. It is original artwork rather than an extracted Microsoft wallpaper. The prompt requested a higher resolution; the tool returned the dimensions listed above, which are preserved without upscaling.

## Final wallpaper prompt

Use case: stylized-concept. Asset type: high resolution wallpaper for an actual Android launcher, image only, absolutely no UI or gadget mockup. Create an original aurora desktop wallpaper extremely close in mood and visual sophistication to the Windows Vista 2006 Aero era: dark midnight navy and deep teal upper region, luminous emerald green and electric cyan aurora flowing diagonally from lower left toward upper right in large graceful folded ribbons of light, subtle golden yellow glow far lower right, silky translucent light beams and atmospheric depth, clean luxurious photorealistic light rendering with fine smooth gradients, gently illuminated curved wisps. Strong dark space upper left so desktop icons read, jewel tones rather than pastel, rich black-blue corners. It should feel like a premium 2006 operating system wallpaper, not a vector illustration, no flat cut-paper waves. Portrait 9:16 composition, highest available resolution ideally 2160x3840. No text, no logos, no Windows symbol, no border, no devices, no mountains, no stars, no actual user interface. Full bleed.

## Supplied XP wallpapers (0.2.0)

The 20 JPEGs under `app/src/main/assets/wallpapers/xp/` are unmodified files from
the user-supplied storef/windows-xp.themepack repository, revision
`ec4fb5578d78783c5aed10f39a9917132b6d60ea`. Most are 800x600; Autumn is 940x705.
This includes Bliss. The repository's MIT notice is retained in
`third_party/xp-wallpapers/LICENSE` and `assets/wallpapers/xp/LICENSE.txt` inside
the APK. Its source credit is https://tjkelly.com/blog/windows-xp-desktop-backgrounds/.
These photographs and Windows branding are not claimed as original Nth artwork.
No audio files from that collection are bundled. The optional XP screen saver is
original Nth text/clock artwork rendered in code.

## Landscape companion

`app/src/main/assets/wallpapers/aero-aurora-landscape.webp` is 1672 × 940 pixels, preserving the generator's output dimensions. A pixel comparison against its generated PNG confirmed identical RGB pixels after lossless WebP encoding. The original portrait asset was provided as a style reference; the companion was recomposed for a wide screen, not stretched.

Prompt: Create an original landscape 16:9 desktop wallpaper matching this reference's calm luminous blue teal and green aurora ribbons, soft glasslike flowing light and deep blue atmosphere. Recompose for a wide desktop: gentle luminous arcs across the lower third and right, tranquil darker blue open space across upper left for desktop icons and gadgets. Full bleed, high quality smooth gradients and fine delicate glow, crisp but soft. No text, no logos, no interface, no windows, no Microsoft or Windows artwork. This is a companion to the reference wallpaper, not a stretch or crop of it.

## Typeface

Selawik Regular and Semibold are bundled without modification from Microsoft's official 1.01 release, under SIL OFL 1.1. The complete license is in `third_party/selawik/LICENSE.txt`. The Nth branding and code-drawn icons remain distinct from Microsoft's product branding.

## Control Panel and Explorer icons

The 0.4.0 folder, drive, Control Panel, network, shield, accounts, accessibility, clock and program icons are original Android Canvas drawings with native gradients and highlights. They remain sharp at the screen resolution and add no bitmap downloads or extracted Windows resources. The lossless portrait and landscape wallpapers are unchanged.

## Original startup experience (0.6.0)

The loading sweep, pearl halo and Nth orb are native Canvas drawings, with no frame sequence or extracted Windows graphics. They are rendered at device resolution in portrait and landscape. The existing two lossless WebP wallpapers and two Selawik font files are unchanged.

The optional original chime is synthesized on the device as 22,050 Hz, 16-bit mono PCM. Four overlapping sine tones at 523.251, 659.255, 783.991 and 1046.502 Hz enter 120 ms apart with exponential attack/decay envelopes. It lasts 1.3 seconds; no Microsoft sound recording is included.

The 0.7.0 battery, network and media icons are original Canvas paths. Profile pictures are user-selected images copied into a small private WebP portrait; no default portrait photograph is bundled.

0.8.0 adds original Canvas symbols for Calendar, sticky notes, Weather, the Gadget Gallery and the eight new native gadget faces. No `.gadget` files, third-party artwork, code or Microsoft gadget binaries are bundled. World-clock data comes from Android time zones. City search uses Photon/OSM; forecast attribution remains MET Norway. User-selected RSS/Atom feeds provide their own headline metadata and article links.

The 0.9.0 individual gadget faces and Compass/Tile Puzzle caption icons are original Android Canvas artwork. The added Network Meter and game category were inspired by gadget categories in the reference galleries, without importing their artwork or code. Compass uses Android SensorManager; network readings use TrafficStats. No new bitmap assets or permissions are required.

The Explorer Cut, Copy, Paste and Delete menu icons are original native Canvas artwork in `AeroIcon.java`; no Windows icon files were imported.

## XP visual controls and fonts (0.3.0)

Caption controls use MIT-licensed XP.css source artwork by Adam Hammad and Jordan
Scales, revision 376f42eb274361939eaad3b0943204c053ad26c4. Original SVGs, license and
conversion provenance are in third_party/xp-css; the app uses 3x PNG rasterizations.
Other scheme controls, the Nth Start mark, XP navigation signs and other control
surfaces are native Android drawing code.

UI text uses Wine Tahoma regular/bold, LGPL 2.1 or later, from Debian fonts-wine
8.0~repack-4. This is Wine's independent replacement, not Microsoft's proprietary
font. Corresponding Wine 8.0 SFD sources, notices, license and substitution/build
instructions are in third_party/wine-tahoma. Both third-party notices are also
bundled under assets/notices inside the APK.

Microsoft's archived XP visual guidelines and GUIdebook captures were consulted
as visual references. Their screenshots and documentation are not bundled.
