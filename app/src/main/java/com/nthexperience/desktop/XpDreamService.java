package com.nthexperience.desktop;

import android.service.dreams.DreamService;

/** Android chooses the start conditions; no launcher idle timer or security bypass. */
public final class XpDreamService extends DreamService {
    private XpScreenSaver screen;
    @Override public void onAttachedToWindow(){super.onAttachedToWindow();setInteractive(false);setFullscreen(true);setScreenBright(false);screen=new XpScreenSaver(this);screen.running(false);setContentView(screen);}
    @Override public void onDreamingStarted(){super.onDreamingStarted();if(screen!=null)screen.running(true);}
    @Override public void onDreamingStopped(){if(screen!=null)screen.running(false);super.onDreamingStopped();}
    @Override public void onDetachedFromWindow(){if(screen!=null)screen.running(false);screen=null;super.onDetachedFromWindow();}
}
