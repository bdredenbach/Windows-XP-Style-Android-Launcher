package com.nthexperience.desktop;

import android.content.*;
import android.widget.*;
import java.io.*;
import java.util.*;

final class XpDiskCleanup extends XpToolPanel {
    private final LinearLayout body;private CheckBox catalog,browser;private TextView total;
    XpDiskCleanup(Context c,DesktopStore store,Host host){super(c,store,host,"@cleanup");command("Analyze",this::analyze);command("Clean selected…",this::confirm);command("Android Storage",()->host.open("setting:android:storage"));body=column();body.addView(text("Disk Cleanup for Nth Desktop",19));body.addView(text("Select temporary data to remove. Desktop settings, pictures, recordings and document drafts are kept.",13));catalog=Aero.check(c,"Installed-app catalog cache",true);browser=Aero.check(c,"Browser temporary files",false);body.addView(catalog);body.addView(browser);total=text("",13);body.addView(total);scroll(body);analyze();}
    private File catalogFile(){return new File(getContext().getCacheDir(),"desktop-apps-v1.json");}
    private void analyze(){long size=catalogFile().length();total.setText("Catalog cache: "+android.text.format.Formatter.formatFileSize(getContext(),size)+"\nBrowser cache size is managed by Android WebView.\nUse Android Storage for other apps and device storage.");say("Ready to review selected categories");}
    private void confirm(){if(!catalog.isChecked()&&!browser.isChecked()){say("Select a category first");return;}Aero.dialog(getContext()).setTitle("Disk Cleanup").setMessage("Remove the selected Nth temporary files?").setPositiveButton("Clean",(d,w)->clean()).setNegativeButton("Cancel",null).show();}
    private void clean(){long freed=0;boolean error=false;if(catalog.isChecked()){File file=catalogFile();long size=file.length();if(!file.exists()||file.delete()){freed+=size;new File(file.getPath()+".bak").delete();}else error=true;}
        if(browser.isChecked())try{android.webkit.WebView view=new android.webkit.WebView(getContext());view.clearCache(true);view.destroy();}catch(RuntimeException|LinkageError unavailable){error=true;}
        analyze();say("Freed "+android.text.format.Formatter.formatFileSize(getContext(),freed)+(error?" • one selected cache was unavailable":" • cleanup finished"));host.shellChanged();}
}
