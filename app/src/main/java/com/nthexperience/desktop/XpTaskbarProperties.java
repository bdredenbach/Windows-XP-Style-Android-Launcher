package com.nthexperience.desktop;

import android.content.Context;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;

/** Staged properties: Cancel keeps the current taskbar, Apply commits the draft. */
final class XpTaskbarProperties extends LinearLayout {
    private final DesktopStore store;private final VistaShell.Actions actions;private final XpPropertySheet sheet;
    private final JSONObject draft;private boolean dirty;
    private static final String[] KEYS={"taskbar_show_labels","taskbar_apps","tray_clock","taskbar_quick_launch","taskbar_expanded","taskbar_static",XpTaskbarAutoHide.KEY,XpTaskbarAutoHide.LOCK,"tray_language","tray_removable","tray_balloons"};
    XpTaskbarProperties(Context c,DesktopStore store,VistaShell.Actions actions,JSONObject saved,Runnable close){
        super(c);this.store=store;this.actions=actions;setOrientation(VERTICAL);setContentDescription("XP Taskbar and Start Menu Properties");draft=saved==null?new JSONObject():saved;
        for(String key:KEYS)if(!draft.has(key))GadgetLayout.put(draft,key,store.prefs.getBoolean(key,defaultValue(key)));
        if(!draft.has("taskbar_grouping"))GadgetLayout.put(draft,"taskbar_grouping",store.prefs.getString("taskbar_grouping","When full"));
        sheet=new XpPropertySheet(c,new String[]{"Taskbar","Start Menu"},draft.optString("tab","Taskbar"),this::render,this::apply,close);addView(sheet,new LayoutParams(-1,-1));
    }
    private void render(LinearLayout body,String tab){GadgetLayout.put(draft,"tab",tab);
        if(tab.equals("Taskbar")){
            TextView preview=Aero.text(getContext(),"start  │  My Computer             ◀  12:00 PM",11,0xffffffff);preview.setBackground(XpTheme.chrome(getContext(),AeroGlass.TASKBAR));preview.setPadding(dp(8),0,dp(5),0);preview.setGravity(Gravity.CENTER_VERTICAL);body.addView(preview,new LayoutParams(-1,dp(40)));
            heading(body,"Taskbar appearance");check(body,"Lock the taskbar",XpTaskbarAutoHide.LOCK);check(body,"Auto-hide the taskbar",XpTaskbarAutoHide.KEY);check(body,"Show taskbar button labels","taskbar_show_labels");CheckBox group=Aero.check(getContext(),"Group similar taskbar buttons",!draft.optString("taskbar_grouping").equals("Never"));group.setOnCheckedChangeListener((v,on)->{GadgetLayout.put(draft,"taskbar_grouping",on?"When full":"Never");changed();});body.addView(group);
            check(body,"Show Quick Launch","taskbar_quick_launch");heading(body,"Notification area");check(body,"Show the clock","tray_clock");check(body,"Show language indicator","tray_language");check(body,"Show removable storage when connected","tray_removable");check(body,"Show low-battery balloons","tray_balloons");heading(body,"Nth taskbar options");check(body,"Expand taskbar to two rows","taskbar_expanded");check(body,"Fit buttons without scrolling","taskbar_static");check(body,"Show Android app taskbar buttons","taskbar_apps");
            TextView more=Aero.button(getContext(),"Advanced…",()->actions.open("setting:page:taskbar-options"));body.addView(more,new LayoutParams(-1,dp(34)));
        }else{
            TextView intro=Aero.text(getContext(),"Start menu",14,Aero.INK);Aero.bold(intro);body.addView(intro);body.addView(Aero.text(getContext(),"Use the familiar two-column Start menu to open programs, documents, Control Panel and Search.",12,Aero.INK));
            TextView customize=Aero.button(getContext(),"Customize…",()->actions.open("@organize"));LayoutParams lp=new LayoutParams(-1,dp(34));lp.topMargin=dp(18);body.addView(customize,lp);body.addView(Aero.button(getContext(),"Choose Quick Launch shortcuts…",()->actions.open("@quicklaunch")),new LayoutParams(-1,dp(34)));
        }
    }
    private static boolean defaultValue(String key){return !key.equals("taskbar_expanded")&&!key.equals("taskbar_static")&&!key.equals(XpTaskbarAutoHide.KEY)&&!key.equals(XpTaskbarAutoHide.LOCK)&&!key.equals("tray_language");}
    private void check(LinearLayout body,String label,String key){CheckBox box=Aero.check(getContext(),label,draft.optBoolean(key));box.setTextSize(12);box.setOnCheckedChangeListener((v,on)->{GadgetLayout.put(draft,key,on);changed();});body.addView(box);}
    private void heading(LinearLayout body,String label){TextView t=Aero.text(getContext(),label,12,Aero.INK);Aero.bold(t);t.setPadding(0,dp(15),0,dp(5));body.addView(t);}
    private void changed(){dirty=true;if(sheet!=null)sheet.dirty(true);}
    private void apply(){android.content.SharedPreferences.Editor edit=store.prefs.edit();for(String key:KEYS)edit.putBoolean(key,draft.optBoolean(key));edit.putString("taskbar_grouping",draft.optString("taskbar_grouping","When full")).apply();dirty=false;actions.action("taskbar-refresh");}
    JSONObject state(){return draft;}boolean dirty(){return dirty;}
    private int dp(int n){return Aero.dp(getContext(),n);}
}
