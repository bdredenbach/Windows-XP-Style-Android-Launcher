package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Color;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;

/** XP profile header, two panes and All Programs flyouts. */
final class StartMenu extends LinearLayout {
    interface Actions{void open(String id);void options(View anchor,String id);default void options(View anchor,String id,String source){options(anchor,id);}void home();void settings();boolean isHome();}
    private final Actions actions;private final DesktopStore store;
    private final List<AppCatalog.Entry> all=new ArrayList<>();
    private final List<StartSearch.Result> shown=new ArrayList<>();
    private final Set<String> pins=new HashSet<>();
    private final BaseAdapter adapter=new ProgramsAdapter();
    private final ListView list;private final LinearLayout profile,searchBox;
    private final EditText search;private final TextView name,programs;
    private DesktopContextMenu flyout;
    StartMenu(Context c,Actions actions,DesktopStore store,AeroWallpaper wallpaper){
        super(c);this.actions=actions;this.store=store;setOrientation(VERTICAL);setElevation(Aero.dp(c,8));
        setPadding(Aero.dp(c,2),Aero.dp(c,2),Aero.dp(c,2),Aero.dp(c,2));setBackground(XpTheme.chrome(c,AeroGlass.START));
        int[] palette=XpTheme.palette(c);
        profile=new LinearLayout(c);profile.setGravity(Gravity.CENTER_VERTICAL);profile.setPadding(Aero.dp(c,10),Aero.dp(c,5),Aero.dp(c,8),Aero.dp(c,5));
        profile.addView(avatar(),new LayoutParams(Aero.dp(c,42),Aero.dp(c,32)));
        name=Aero.text(c,"",17,XpTheme.titleInk(c));Aero.bold(name);name.setSingleLine();name.setEllipsize(TextUtils.TruncateAt.END);name.setPadding(Aero.dp(c,10),0,0,0);name.setOnClickListener(v->actions.open("setting:page:accounts"));ContextMenus.bind(name,(x,y)->actions.options(name,"setting:page:accounts",XpStartMenus.PLACE));profile.addView(name,new LayoutParams(0,-1,1));addView(profile,new LayoutParams(-1,Aero.dp(c,58)));
        View line=new View(c);line.setBackgroundColor(0xffffbc55);addView(line,new LayoutParams(-1,Aero.dp(c,1)));
        LinearLayout panes=new LinearLayout(c);panes.setBaselineAligned(false);addView(panes,new LayoutParams(-1,0,1));
        LinearLayout left=Aero.column(c);left.setBackgroundColor(Color.WHITE);panes.addView(left,new LayoutParams(0,-1,.52f));View seam=new View(c);seam.setBackgroundColor(0xff88acd6);panes.addView(seam,new LayoutParams(Aero.dp(c,1),-1));
        FrameLayout content=new FrameLayout(c);list=new ListView(c);list.setDividerHeight(0);list.setSelector(Aero.selection(c));list.setAdapter(adapter);list.setContentDescription("Installed programs. Hold an app for Start menu options.");
        list.setOnItemClickListener((p,v,position,id)->actions.open(shown.get(position).id));
        list.setOnItemLongClickListener((p,v,position,id)->{options(v,shown.get(position));return true;});content.addView(list,new FrameLayout.LayoutParams(-1,-1));
        TextView empty=Aero.text(c,"No matching programs",12,Aero.INK);empty.setGravity(Gravity.CENTER);content.addView(empty,new FrameLayout.LayoutParams(-1,-1));list.setEmptyView(empty);left.addView(content,new LayoutParams(-1,0,1));
        searchBox=Aero.column(c);searchBox.setPadding(Aero.dp(c,5),Aero.dp(c,4),Aero.dp(c,5),Aero.dp(c,4));searchBox.setBackgroundColor(palette[3]);
        search=new EditText(c);search.setSingleLine();search.setTextSize(13);search.setTextColor(Aero.INK);search.setHintTextColor(0xff5d6470);search.setHint("Search programs");search.setContentDescription("Search apps, settings, notes and linked files");search.setTypeface(Aero.face(c));search.setBackground(Aero.panel(c,Color.WHITE,Color.WHITE,0,0xff7f9db9));search.setPadding(Aero.dp(c,6),0,Aero.dp(c,6),0);searchBox.addView(search,new LayoutParams(-1,Aero.dp(c,38)));searchBox.setVisibility(GONE);left.addView(searchBox,new LayoutParams(-1,-2));
        programs=Aero.text(c,"All Programs  ►",13,Aero.INK);Aero.bold(programs);programs.setGravity(Gravity.CENTER);programs.setBackground(XpControls.menuSelection(c));programs.setTextColor(XpControls.menuInk(Aero.INK));programs.setFocusable(true);programs.setContentDescription("All Programs");programs.setOnClickListener(v->programMenu(programs,""));ContextMenus.bind(programs,(x,y)->groupOptions(programs,""));left.addView(programs,new LayoutParams(-1,Aero.dp(c,38)));
        LinearLayout right=Aero.column(c);right.setPadding(Aero.dp(c,5),Aero.dp(c,5),Aero.dp(c,4),Aero.dp(c,5));right.setBackgroundColor(palette[4]);
        String[][] places={{"My Documents","@documents","documents"},{"My Recent Documents","recent","clock"},{"My Pictures","@pictures","pictures"},{"My Music","@music","music"},{"My Computer","@computer","computer"},{"Control Panel","@control","control"},{"Set Program Access and Defaults","setting:android:defaults","programs"},{"Connect To","setting:android:network","network"},{"Printers and Faxes","setting:android:print","printer"},{"Help and Support","@welcome","help"},{"Search","search","search"},{"Run…","xp:run","programs"}};
        for(int i=0;i<places.length;i++){
            String[] item=places[i];if(i==5||i==9){View divider=new View(c);divider.setBackgroundColor(0xffa8bfdc);LayoutParams rule=new LayoutParams(-1,Aero.dp(c,1));rule.setMargins(Aero.dp(c,5),Aero.dp(c,3),Aero.dp(c,5),Aero.dp(c,3));right.addView(divider,rule);}
            LinearLayout row=new LinearLayout(c);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(Aero.dp(c,3),0,Aero.dp(c,2),0);row.setBackground(XpControls.menuSelection(c));row.setFocusable(true);row.setContentDescription(item[0]);
            row.addView(new AeroIcon(c,item[2]),new LayoutParams(Aero.dp(c,21),Aero.dp(c,21)));
            TextView text=Aero.text(c,item[0],11,0xff12336b);text.setTextColor(XpControls.menuInk(0xff12336b));text.setDuplicateParentStateEnabled(true);text.setMaxLines(2);text.setEllipsize(TextUtils.TruncateAt.END);text.setPadding(Aero.dp(c,5),0,0,0);if(i<5)Aero.bold(text);row.addView(text,new LayoutParams(0,-1,1));
            row.setOnClickListener(v->{if(item[1].equals("recent"))recent(row);else if(item[1].equals("search"))actions.open("@search");else actions.open(item[1]);});ContextMenus.bind(row,(x,y)->actions.options(row,item[1].equals("recent")?"@recent":item[1].equals("search")?"@search":item[1],XpStartMenus.PLACE));right.addView(row,new LayoutParams(-1,Aero.dp(c,item[0].equals("Set Program Access and Defaults")?44:item[1].equals("xp:run")?40:36)));
        }
        ScrollView scroll=new ScrollView(c);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);scroll.addView(right,new ScrollView.LayoutParams(-1,-2));panes.addView(scroll,new LayoutParams(0,-1,.48f));
        LinearLayout footer=new LinearLayout(c);footer.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);footer.setPadding(Aero.dp(c,5),0,Aero.dp(c,5),0);
        for(String[] item:new String[][]{{"Log Off","xp:logoff","Log Off"},{"Turn Off","xp:power","Turn Off"}}){TextView button=Aero.text(c,item[0],12,XpTheme.titleInk(c));button.setCompoundDrawablePadding(Aero.dp(c,6));XpNavigation icon=new XpNavigation(c,item[1].equals("xp:logoff")?"logoff":"power");icon.setBounds(0,0,Aero.dp(c,26),Aero.dp(c,26));button.setCompoundDrawables(icon,null,null,null);button.setGravity(Gravity.CENTER);button.setFocusable(true);button.setContentDescription(item[2]);button.setBackground(Aero.selection(c));button.setOnClickListener(v->actions.open(item[1]));ContextMenus.bind(button,(x,y)->commandOptions(button,item[0],()->actions.open(item[1])));footer.addView(button,new LayoutParams(0,Aero.dp(c,40),1));}
        addView(footer,new LayoutParams(-1,Aero.dp(c,44)));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int n,int f){}public void onTextChanged(CharSequence s,int a,int b,int n){filter();}public void afterTextChanged(Editable e){}});refreshName();filter();
    }
    void refresh(){dismissFlyout();refreshName();filter();}
    private void dismissFlyout(){if(flyout!=null){flyout.dismiss();flyout=null;}}
    private void options(View anchor,StartSearch.Result item){actions.options(anchor,item.id,search.getText().length()==0?XpStartMenus.HOME:item.shortcut?XpStartMenus.PROGRAMS:item.id.startsWith("recent-file:")?XpStartMenus.DOCUMENT:XpStartMenus.PLACE);}
    private View avatar(){View v=ProfilePortrait.view(getContext(),store);v.setContentDescription("Change profile picture");v.setFocusable(true);v.setOnClickListener(w->actions.open("setting:page:accounts"));ContextMenus.bind(v,(x,y)->actions.options(v,"setting:page:accounts",XpStartMenus.PLACE));return v;}
    void entries(List<AppCatalog.Entry> entries){all.clear();all.addAll(entries);filter();}
    void refreshName(){name.setText(store.prefs.getString("desktop_name",getContext().getString(R.string.app_name)));profile.removeViewAt(0);profile.addView(avatar(),0,new LayoutParams(Aero.dp(getContext(),42),Aero.dp(getContext(),42)));}
    void refreshHome(){}
    void show(boolean keyboard){animate().cancel();refreshName();setVisibility(VISIBLE);bringToFront();searchBox.setVisibility(GONE);search.setText("");filter();if(keyboard)showSearch();else{search.clearFocus();requestFocus();}if(Aero.motion(getContext())){setAlpha(0);animate().alpha(1).setDuration(140).start();}else setAlpha(1);}
    private void showSearch(){searchBox.setVisibility(VISIBLE);search.requestFocus();search.post(()->((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(search,InputMethodManager.SHOW_IMPLICIT));}
    void hide(){dismissFlyout();animate().cancel();((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(),0);search.clearFocus();setVisibility(GONE);}
    JSONObject state(){JSONObject value=new JSONObject();GadgetLayout.put(value,"visible",getVisibility()==VISIBLE);GadgetLayout.put(value,"query",search.getText().toString());GadgetLayout.put(value,"search",searchBox.getVisibility()==VISIBLE);GadgetLayout.put(value,"position",list.getFirstVisiblePosition());return value;}
    void restore(JSONObject state){if(state==null||!state.optBoolean("visible"))return;show(false);searchBox.setVisibility(state.optBoolean("search")?VISIBLE:GONE);search.setText(state.optString("query"));list.setSelection(state.optInt("position"));}
    private StartSearch.Result builtin(String id,String label,String icon){return new StartSearch.Result(id,store.name(id,label),"Programs",icon,null,true);}
    private StartSearch.Result entry(String id){XpPrograms.Program program=XpPrograms.get(id);if(program!=null)return builtin(id,program.title,program.icon);for(AppCatalog.Entry app:all)if(app.id.equals(id))return new StartSearch.Result(id,store.name(id,app.label),"Programs","programs",app.icon,true);if(FolderLinks.id(id)){JSONObject link=FolderLinks.get(store,id);if(link!=null)return builtin(id,link.optString("title","Folder"),"files");}switch(id){case "xp:mail":return builtin(id,"E-mail","mail");case "@browser":return builtin(id,BrowserPanel.NAME,"browser");case "@files":return builtin(id,"Nth Explorer","files");case "@notes":return builtin(id,"Notepad","notes");case "@photos":return builtin(id,"My Pictures","pictures");case MediaLibrary.PLAYER:return builtin(id,MediaLibrary.PLAYER_TITLE,"mediaplayer");case MediaLibrary.CENTER:return builtin(id,MediaLibrary.CENTER_TITLE,"mediacenter");default:if(store.isFolder(id))return builtin(id,store.name(id,"Folder"),"folderapps");String title=XpStartMenus.title(id,"");if(!title.isEmpty())return builtin(id,title,"programs");if(Arrays.asList("@taskmanager","@backup","@files","@gadget:calculator","@gadget:puzzle","@gadget:dice").contains(id))return builtin(id,id.equals("@taskmanager")?"Task Manager":id.equals("@backup")?"Backup and Restore":id.equals("@files")?"Nth Explorer":GadgetCatalog.title(id.substring(8)),"programs");return null;}}
    private void filter(){
        shown.clear();pins.clear();String query=search.getText().toString().trim();
        if(!query.isEmpty())shown.addAll(StartSearch.find(store,all,query));
        else{
            Set<String> seen=new HashSet<>(),excluded=new HashSet<>(store.startExcluded()),deleted=new HashSet<>(store.startDeleted());
            List<String> fixed=new ArrayList<>(Arrays.asList("@browser","xp:mail"));fixed.addAll(store.startPins());
            for(String id:fixed){StartSearch.Result r=entry(id);if(r!=null&&!excluded.contains(id)&&!deleted.contains(id)&&seen.add(id)){shown.add(r);pins.add(id);}}
            for(String id:store.recents()){StartSearch.Result r=entry(id);if(r!=null&&!excluded.contains(id)&&!deleted.contains(id)&&seen.add(id)&&shown.size()<10)shown.add(r);}
            for(AppCatalog.Entry app:all)if(!excluded.contains(app.id)&&!deleted.contains(app.id)&&seen.add(app.id)&&shown.size()<10)shown.add(new StartSearch.Result(app.id,store.name(app.id,app.label),"Programs","programs",app.icon,true));
        }
        adapter.notifyDataSetChanged();
    }
    private void programItem(DesktopContextMenu menu,String title,String id,String icon){
        if(store.startDeleted().contains(id))return;
        menu.contextualItem(store.name(id,title),icon,()->actions.open(id),row->actions.options(row,id,XpStartMenus.PROGRAMS));
    }
    private void groupItem(DesktopContextMenu menu,String group){menu.contextualSubmenu(group,"folderapps",()->programMenu(programs,group),row->groupOptions(row,group));}
    private void groupOptions(View anchor,String group){
        // Use the attached Start surface, since a flyout row belongs to a separate PopupWindow.
        ContextMenus.centered(anchor,(x,y)->{
            DesktopContextMenu menu=new DesktopContextMenu(getContext(),"Start program folder context menu");
            menu.item("Open","empty",()->programMenu(programs,group));menu.item("Sort by Name","empty",()->{store.prefs.edit().putBoolean("xp_start_sort",true).apply();programMenu(programs,group);});
            if(!store.startDeleted().isEmpty())menu.item("Restore deleted shortcuts…","empty",()->Aero.dialog(getContext()).setTitle("Restore Start menu shortcuts").setMessage("Restore shortcuts removed from All Programs?").setPositiveButton("Restore",(d,w)->{List<String> excluded=store.startExcluded();excluded.removeAll(store.startDeleted());store.startExcluded(excluded);store.startDeleted(Collections.emptyList());refresh();programMenu(programs,group);}).setNegativeButton("Cancel",null).show());
            menu.separator();menu.item("Properties","empty",()->Aero.dialog(getContext()).setTitle((group.isEmpty()?"All Programs":group)+" Properties").setMessage("Program folder in the Nth Start menu. Hold a program for shortcut commands. Built-in program groups are managed by Nth Desktop.").setPositiveButton("OK",null).show());ContextMenus.show(menu,this,x,y);
        });
    }
    private void commandOptions(View anchor,String title,Runnable action){ContextMenus.centered(anchor,(x,y)->{DesktopContextMenu menu=new DesktopContextMenu(getContext(),"Start command context menu");menu.item(title,"empty",action);ContextMenus.show(menu,this,x,y);});}
    private void programMenu(View anchor,String group){
        dismissFlyout();flyout=new DesktopContextMenu(getContext(),group.isEmpty()?"All Programs":group,30,250);
        if(group.isEmpty()){
            groupItem(flyout,"Accessories");groupItem(flyout,"Games");
            programItem(flyout,MediaLibrary.PLAYER_TITLE,MediaLibrary.PLAYER,"mediaplayer");programItem(flyout,MediaLibrary.CENTER_TITLE,MediaLibrary.CENTER,"mediacenter");flyout.separator();
            List<String> folders=new ArrayList<>();store.folders().keys().forEachRemaining(folders::add);
            List<AppCatalog.Entry> apps=new ArrayList<>(all);
            if(store.prefs.getBoolean("xp_start_sort",false)){java.text.Collator collator=java.text.Collator.getInstance();folders.sort((a,b)->collator.compare(store.name(a,"Folder"),store.name(b,"Folder")));apps.sort((a,b)->collator.compare(store.name(a.id,a.label),store.name(b.id,b.label)));}
            for(String id:folders)programItem(flyout,store.name(id,"Folder"),id,"folderapps");
            for(AppCatalog.Entry app:apps)programItem(flyout,app.label,app.id,"programs");
        }else{
            if(group.equals("Accessories"))for(String child:new String[]{"Accessibility","Entertainment","System Tools"})groupItem(flyout,child);
            List<String[]> items=new ArrayList<>();for(XpPrograms.Program p:XpPrograms.ALL)if(p.group.equals(group))items.add(new String[]{p.title,p.id,p.icon});
            if(group.equals("Accessories"))Collections.addAll(items,new String[][]{{"Notepad","@notes","notes"},{"Calculator","@gadget:calculator","calculator"},{"Nth Explorer","@files","files"},{"My Pictures","@photos","pictures"},{"Task Manager","@taskmanager","taskmanager"},{"Backup and Restore","@backup","drive"}});
            if(group.equals("Games"))Collections.addAll(items,new String[][]{{"Tile Puzzle","@gadget:puzzle","puzzle"},{"Dice Roller","@gadget:dice","dice"}});
            if(store.prefs.getBoolean("xp_start_sort",false)){java.text.Collator collator=java.text.Collator.getInstance();items.sort((a,b)->collator.compare(store.name(a[1],a[0]),store.name(b[1],b[0])));}
            for(String[] item:items)programItem(flyout,item[0],item[1],item[2]);
            flyout.separator();flyout.item("Back to All Programs","files",()->programMenu(programs,""));
        }
        ContextMenus.centered(anchor,(x,y)->ContextMenus.show(flyout,this,x,y));
    }
    private void recent(View anchor){
        dismissFlyout();flyout=new DesktopContextMenu(getContext(),"My Recent Documents",30,250);org.json.JSONArray files=RecentFiles.list(store);
        for(int i=0;i<Math.min(12,files.length());i++){JSONObject file=files.optJSONObject(i);if(file!=null){String id="recent-file:"+file.optString("uri");flyout.contextualItem(file.optString("name"),"documentfile",()->actions.open(id),row->actions.options(row,id,XpStartMenus.DOCUMENT));}}
        if(files.length()==0)flyout.item("No recent documents","empty",false,()->{});flyout.separator();flyout.item("View all recent items","clock",()->actions.open("@recent"));ContextMenus.centered(anchor,(x,y)->ContextMenus.show(flyout,this,x,y));
    }
    private final class ProgramsAdapter extends BaseAdapter {
        public int getCount(){return shown.size();}public Object getItem(int p){return shown.get(p);}public long getItemId(int p){return p;}
        public View getView(int position,View recycled,ViewGroup parent){
            StartSearch.Result item=shown.get(position);LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(Aero.dp(getContext(),7),Aero.dp(getContext(),3),Aero.dp(getContext(),4),Aero.dp(getContext(),3));row.setBackground(XpControls.menuSelection(getContext()));
            View icon;if(item.icon!=null){ImageView image=new ImageView(getContext());image.setImageDrawable(item.icon);icon=image;}else icon=new AeroIcon(getContext(),item.kind);row.addView(icon,new LayoutParams(Aero.dp(getContext(),29),Aero.dp(getContext(),29)));
            LinearLayout words=Aero.column(getContext());words.setGravity(Gravity.CENTER_VERTICAL);words.setPadding(Aero.dp(getContext(),7),0,0,0);words.setDuplicateParentStateEnabled(true);
            boolean internet=item.id.equals("@browser"),email=item.id.equals("xp:mail");TextView title=Aero.text(getContext(),internet?"Internet":item.title,12,Aero.INK);title.setTextColor(XpControls.menuInk(Aero.INK));title.setDuplicateParentStateEnabled(true);title.setMaxLines(2);title.setEllipsize(TextUtils.TruncateAt.END);if(internet||email)Aero.bold(title);words.addView(title);
            if(internet||email){TextView sub=Aero.text(getContext(),internet?BrowserPanel.NAME:"Android mail app",10,0xff777777);sub.setSingleLine(true);sub.setEllipsize(TextUtils.TruncateAt.END);sub.setTextColor(XpControls.menuInk(0xff777777));sub.setDuplicateParentStateEnabled(true);words.addView(sub);}
            row.addView(words,new LayoutParams(0,Aero.dp(getContext(),internet||email?43:34),1));if(position>0&&!pins.contains(item.id)&&pins.contains(shown.get(position-1).id)){row.setPadding(row.getPaddingLeft(),Aero.dp(getContext(),7),row.getPaddingRight(),Aero.dp(getContext(),3));}
            row.setContentDescription("Open Programs: "+item.title);row.setOnClickListener(v->actions.open(item.id));ContextMenus.bind(row,(x,y)->options(row,item));row.setFocusable(false);return row;
        }
    }
}
