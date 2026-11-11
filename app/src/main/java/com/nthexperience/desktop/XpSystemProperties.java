package com.nthexperience.desktop;

import android.app.ActivityManager;
import android.content.Context;
import android.os.*;
import android.view.*;
import android.widget.*;

final class XpSystemProperties {
    static final String[] TABS={"General","Computer Name","Hardware","Advanced","System Restore","Automatic Updates","Remote"};
    static View create(Context c,DesktopStore store,VistaShell.Actions actions,Runnable close){
        return new XpPropertySheet(c,TABS,"General",(body,tab)->{
            if(tab.equals("General")){
                LinearLayout identity=new LinearLayout(c);identity.setGravity(Gravity.CENTER_VERTICAL);identity.addView(new AeroIcon(c,"computer"),new LinearLayout.LayoutParams(Aero.dp(c,76),Aero.dp(c,76)));LinearLayout brand=Aero.column(c);brand.setPadding(Aero.dp(c,15),0,0,0);TextView title=Aero.text(c,"Nth Desktop XP",18,Aero.INK);Aero.bold(title);brand.addView(title);brand.addView(Aero.text(c,"Version "+AppInfo.version(c),12,Aero.INK));brand.addView(Aero.text(c,"Android launcher",11,Aero.INK));identity.addView(brand,new LinearLayout.LayoutParams(0,-2,1));body.addView(identity);
                heading(body,"Registered to:");text(body,store.prefs.getString("desktop_name",c.getString(R.string.app_name)));
                heading(body,"Computer:");text(body,Build.MANUFACTURER+" "+Build.MODEL);String processor=Build.VERSION.SDK_INT>=31&&!Build.SOC_MODEL.equals(Build.UNKNOWN)?Build.SOC_MODEL:Build.HARDWARE;text(body,processor);text(body,Runtime.getRuntime().availableProcessors()+" logical processors");ActivityManager am=c.getSystemService(ActivityManager.class);if(am!=null){ActivityManager.MemoryInfo memory=new ActivityManager.MemoryInfo();am.getMemoryInfo(memory);text(body,android.text.format.Formatter.formatFileSize(c,memory.totalMem)+" of RAM");}text(body,"Android "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")");
            }else if(tab.equals("Computer Name")){text(body,"This name identifies your desktop in the Start menu.");heading(body,"Full computer name:");text(body,store.prefs.getString("desktop_name",c.getString(R.string.app_name)));button(body,"Change…",()->actions.open("setting:page:accounts"));}
            else if(tab.equals("Hardware")){heading(body,"Device Manager");text(body,"View information about your Android hardware and connected devices.");button(body,"Device information",()->actions.action("android:device"));button(body,"Bluetooth Devices",()->actions.action("android:bluetooth"));button(body,"Printers and Faxes",()->actions.action("android:print"));}
            else if(tab.equals("Advanced")){heading(body,"Performance");text(body,"View the launcher’s CPU and memory usage.");button(body,"Task Manager",()->actions.open("@taskmanager"));heading(body,"Startup and Recovery");button(body,"Startup settings",()->actions.open("setting:page:startup"));button(body,"Android app information",()->actions.action("taskbar-usage-app-info"));}
            else if(tab.equals("System Restore")){heading(body,"Desktop backup");text(body,"Save your Nth desktop to a .nthdesktop file or restore an existing backup. Android system restore is managed by the device.");button(body,"Backup…",()->actions.open("@backup"));}
            else if(tab.equals("Automatic Updates")){text(body,"Android manages device and app updates. This launcher cannot enable or disable Windows Update.");button(body,"Android device information",()->actions.action("android:device"));}
            else{text(body,"Remote assistance and Remote Desktop services are not provided by this Android launcher.");button(body,"Help and Support",()->actions.open("@welcome"));}
        },null,close);
    }
    private static void text(LinearLayout body,String value){TextView t=Aero.text(body.getContext(),value,12,Aero.INK);t.setPadding(0,Aero.dp(body.getContext(),3),0,Aero.dp(body.getContext(),4));body.addView(t);}
    private static void heading(LinearLayout body,String value){TextView t=Aero.text(body.getContext(),value,12,Aero.INK);Aero.bold(t);t.setPadding(0,Aero.dp(body.getContext(),18),0,Aero.dp(body.getContext(),4));body.addView(t);}
    private static void button(LinearLayout body,String label,Runnable click){TextView b=Aero.button(body.getContext(),label,click);b.setTextSize(11);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Aero.dp(body.getContext(),34));lp.topMargin=Aero.dp(body.getContext(),8);body.addView(b,lp);}
}
