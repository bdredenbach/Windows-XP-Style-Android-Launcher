package com.nthexperience.desktop;

import android.content.*;
import android.widget.*;

final class XpTransferWizard extends XpToolPanel {
    private final RadioButton oldDevice;private boolean summary;
    XpTransferWizard(Context c,DesktopStore store,Host host){super(c,store,host,"@transfer");command("Back",()->{summary=false;render();});command("Next",this::next);LinearLayout body=column();body.addView(text("Files and Settings Transfer Wizard",19));body.addView(text("Transfer your Nth desktop with a .nthdesktop backup. Android document-folder access is reconnected on the destination device.",13));RadioGroup choices=new RadioGroup(c);oldDevice=XpWidgets.radio(c);oldDevice.setText("This is the old device — export desktop settings");oldDevice.setId(ViewIds.OLD);oldDevice.setChecked(true);choices.addView(oldDevice);RadioButton newer=XpWidgets.radio(c);newer.setText("This is the new device — import a backup");newer.setId(ViewIds.NEW);choices.addView(newer);body.addView(choices);body.addView(text("Includes appearance, shortcuts, app folders, Notepad and WordPad drafts. Pictures, recordings and other documents can be moved with Nth Explorer or Android sharing.",13));scroll(body);say("Step 1 of 2 • choose a device");}
    private void render(){say(summary?"Step 2 of 2 • Next opens "+(oldDevice.isChecked()?"Export":"Import"):"Step 1 of 2 • choose a device");}
    private void next(){if(!summary){summary=true;render();return;}host.open(oldDevice.isChecked()?"xp:exportbackup":"xp:importbackup");}
    private static final class ViewIds{static final int OLD=701,NEW=702;}
}
