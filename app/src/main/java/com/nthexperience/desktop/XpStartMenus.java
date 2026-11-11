package com.nthexperience.desktop;

import android.content.*;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import org.json.*;
import java.util.*;

/** XP shortcut commands with Start-only removal and Android-owned uninstall. */
final class XpStartMenus {
    static final String HOME="start-home", PROGRAMS="start-programs", PLACE="start-place", DOCUMENT="start-document";
    static void show(Context c,View anchor,float x,float y,DesktopStore store,String id,String source,String fallback,ShortcutMenus.Host host){
        String name=store.name(id,title(id,fallback));
        DesktopContextMenu menu=new DesktopContextMenu(c,"Start item context menu");
        menu.item("Open","empty",()->host.open(id));
        if(source.equals(DOCUMENT)){recent(menu,c,store,id,name,host);ContextMenus.show(menu,anchor,x,y);return;}
        boolean folder=store.isFolder(id)||FolderLinks.id(id)||Arrays.asList("@computer","@documents","@pictures","@music","@files","@control").contains(id);
        boolean place=source.equals(PLACE);
        if(folder)menu.item("Explore","empty",()->host.open(id));
        if(id.equals("@computer"))menu.item("Manage","empty",()->host.open("@msinfo"));
        if(!place){
            menu.separator();
            boolean pinned=pinned(store,id);
            menu.item(pinned?"Unpin from Start menu":"Pin to Start menu","empty",pinned||store.startPins().size()<32,()->{pin(store,id,!pinned);host.changed();});
        }
        menu.separator();
        menu.submenu("Send To","empty",()->{
            DesktopContextMenu send=new DesktopContextMenu(c,"Send To");
            send.item("Desktop (create shortcut)","shortcut",()->{
                List<String> desktop=store.desktop();boolean exists=desktop.contains(id);
                if(!exists){if(desktop.size()>=512){Toast.makeText(c,"The desktop has no room for another shortcut.",Toast.LENGTH_LONG).show();return;}desktop.add(id);store.desktop(desktop);host.changed();}
                Toast.makeText(c,exists?"This shortcut is already on the desktop":"Desktop shortcut created",Toast.LENGTH_SHORT).show();
            });
            ContextMenus.show(send,anchor,x,y);
        });
        if(!place&&source.equals(HOME)&&store.startPins().contains(id))menu.item("Cut","empty",()->{ExplorerClipboard.shortcuts(Collections.singletonList(id),"start-pins",true);});
        menu.item("Copy","empty",()->{ExplorerClipboard.shortcuts(Collections.singletonList(id),"start-pins",false);});
        if(store.isFolder(id))menu.item("Paste","empty",!ExplorerClipboard.shortcuts.isEmpty(),()->ShortcutMenus.paste(c,store,"contents:"+id,host));
        if(!place){
            menu.separator();
            if(source.equals(HOME))menu.item("Remove from This List","empty",()->{remove(store,id,false);host.changed();});
            else menu.item("Delete","empty",()->Aero.dialog(c).setTitle("Confirm Shortcut Delete").setMessage("Remove "+name+" from the Start menu? The app, desktop shortcut and folder contents will be kept.").setPositiveButton("Delete",(d,w)->{remove(store,id,true);host.changed();}).setNegativeButton("Cancel",null).show());
            menu.item("Rename…","empty",()->host.rename(id));
        }
        if(place&&Arrays.asList("@computer","@documents","@pictures","@music").contains(id))menu.checked("Show on Desktop",store.pinned(id),()->{store.toggle(id);host.changed();});
        menu.separator();
        menu.item("Properties","empty",()->{
            if(id.equals("@computer")){host.open("@system");return;}
            if(id.equals("@browser")&&place){host.open("setting:android:defaults");return;}
            properties(c,store,id,name);
        });
        // A named Android extension: XP normally used Control Panel > Add or Remove Programs.
        if(AppUninstaller.available(c,id)){menu.separator();AppUninstaller.add(menu,c,id);}
        ContextMenus.show(menu,anchor,x,y);
    }
    static boolean pinned(DesktopStore store,String id){return !store.startExcluded().contains(id)&&(store.startPins().contains(id)||id.equals("@browser")||id.equals("xp:mail"));}
    static void pin(DesktopStore store,String id,boolean pinned){
        List<String> pins=store.startPins(),excluded=store.startExcluded(),deleted=store.startDeleted();
        pins.remove(id);
        if(pinned){if(pins.size()<32)pins.add(id);excluded.remove(id);deleted.remove(id);}
        else if(id.equals("@browser")||id.equals("xp:mail")){if(!excluded.contains(id))excluded.add(id);}
        store.startPins(pins);store.startExcluded(excluded);store.startDeleted(deleted);
    }
    static void remove(DesktopStore store,String id,boolean deleted){
        pin(store,id,false);List<String> excluded=store.startExcluded();if(!excluded.contains(id))excluded.add(id);store.startExcluded(excluded);
        List<String> recents=store.recents();recents.remove(id);store.prefs.edit().putString("recents",new JSONArray(recents).toString()).apply();
        if(deleted){List<String> hidden=store.startDeleted();if(!hidden.contains(id))hidden.add(id);store.startDeleted(hidden);}
    }
    static String title(String id,String fallback){
        switch(id){case "xp:mail":return "E-mail";case "@documents":return "My Documents";case "@pictures":return "My Pictures";case "@music":return "My Music";case "@computer":return "My Computer";case "@control":return "Control Panel";case "@recent":return "My Recent Documents";case "setting:android:defaults":return "Set Program Access and Defaults";case "setting:android:network":return "Connect To";case "setting:android:print":return "Printers and Faxes";case "setting:page:accounts":return "User Accounts";case "xp:run":return "Run";case "xp:logoff":return "Log Off";case "xp:power":return "Turn Off";default:return fallback;}
    }
    private static void recent(DesktopContextMenu menu,Context c,DesktopStore store,String id,String name,ShortcutMenus.Host host){
        String uri=id.startsWith("recent-file:")?id.substring(12):"";
        menu.separator();menu.item("Remove from This List","empty",()->{JSONArray old=RecentFiles.list(store),kept=new JSONArray();for(int i=0;i<old.length();i++){JSONObject file=old.optJSONObject(i);if(file!=null&&!uri.equals(file.optString("uri")))kept.put(file);}store.prefs.edit().putString("recent_files",kept.toString()).apply();host.changed();});
        menu.item("Properties","empty",()->{JSONObject file=RecentFiles.get(store,uri);Aero.dialog(c).setTitle(name+" Properties").setMessage(file==null?"This recent item is no longer available.":"Name: "+file.optString("name")+"\nType: "+file.optString("mime",file.optString("kind","Document"))+"\nLocation: "+uri).setPositiveButton("OK",null).show();});
    }
    private static void properties(Context c,DesktopStore store,String id,String name){
        String pkg=AppUninstaller.packageName(id);String detail="Type: "+(store.isFolder(id)?"App folder":"Shortcut")+"\nName: "+name;
        XpPrograms.Program program=XpPrograms.get(id);if(program!=null)detail+="\n\n"+program.description;
        if(store.isFolder(id))detail+="\nContains: "+store.folderItems(id).size()+" shortcuts";
        if(pkg!=null){detail+="\nPackage: "+pkg;try{PackageInfo info=c.getPackageManager().getPackageInfo(pkg,0);detail+="\nVersion: "+info.versionName;}catch(android.content.pm.PackageManager.NameNotFoundException ignored){detail+="\nThe installed app is unavailable.";}}
        else if(id.startsWith("setting:"))detail+="\nOpens the corresponding Android or Nth setting.";
        else detail+="\nTarget: "+title(id,program==null?id:program.title);
        android.app.AlertDialog.Builder builder=Aero.dialog(c).setTitle(name+" Properties").setMessage(detail).setPositiveButton("OK",null);
        if(pkg!=null)builder.setNeutralButton("App info",(d,w)->{try{c.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",pkg,null)));}catch(ActivityNotFoundException|SecurityException error){Toast.makeText(c,"Android app information is unavailable.",Toast.LENGTH_SHORT).show();}});
        builder.show();
    }
    private XpStartMenus(){}
}
