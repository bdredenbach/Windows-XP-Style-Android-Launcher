# Wine Tahoma font replacement

Unmodified Wine Tahoma regular and bold, from Debian fonts-wine 8.0~repack-4:
https://deb.debian.org/debian/pool/main/w/wine/fonts-wine_8.0~repack-4_all.deb
Upstream source: https://github.com/wine-mirror/wine/tree/wine-8.0/fonts

Copyright (c) 2004 Larry Snyder. Based on Bitstream Vera Sans,
Copyright (c) 2003 Bitstream, Inc. The font was renamed by its author.
This is Wine's replacement, not Microsoft's proprietary Tahoma font.
Licensed under LGPL 2.1 or later; full license and corresponding SFD sources
are included here. No changes have been made to either binary font.

The fonts are separate data assets. To substitute a font, replace xp_regular.ttf
and xp_bold.ttf in app/src/main/res/font, then rebuild using the public project
source and build instructions. The application does not modify these font files.
