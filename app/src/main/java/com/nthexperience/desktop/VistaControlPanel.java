package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

final class VistaControlPanel extends VistaShell {
    private static final String[][] CATEGORIES=ControlPanelCatalog.CATEGORIES;
    static void searchItems(String query,List<StartSearch.Result> out){for(ControlPanelCatalog.Hit hit:ControlPanelCatalog.search(query))out.add(new StartSearch.Result("setting:"+hit.destination,hit.title,"Control Panel",hit.owner.icon,null,false));}
    private final AeroWallpaper wallpaper;
    private final LinearLayout displayHost;private XpDisplayProperties display;private String displayPage="",appletPage="";private XpTaskbarProperties taskbarProperties;private JSONObject taskbarDraft;private LinearLayout paneGroup;
    private JSONObject backgroundDraft=new JSONObject();private boolean classic,smallIcons;private final LinearLayout crumbs;private int layoutSignature=-1;
    private EditText nameInput;private String nameDraft;
    VistaControlPanel(Context c,DesktopStore store,AeroWallpaper wallpaper,Actions actions,String initial){
        super(c,store,actions,"Search Control Panel");this.wallpaper=wallpaper;classic=store.prefs.getBoolean("control_panel_classic",false);smallIcons=store.prefs.getBoolean("control_panel_small_icons",false);
        displayHost=Aero.column(c);displayHost.setVisibility(GONE);addView(displayHost,new LayoutParams(-1,0,1));
        folders.setText("Folders");folders.setContentDescription("Control Panel navigation");commands.removeAllViews();removeView(status);
        ViewGroup pane=(ViewGroup)side.getParent();pane.setBackgroundColor(0xff7ba2e7);pane.getLayoutParams().width=dp(144);side.setBackground(Aero.panel(c,0xff7ba2e7,0xff6375d6,0,0));side.setPadding(dp(12),dp(16),dp(12),dp(12));
        LinearLayout location=(LinearLayout)address.getParent();int index=location.indexOfChild(address);location.removeView(address);crumbs=new LinearLayout(c);crumbs.setGravity(Gravity.CENTER_VERTICAL);crumbs.setBackground(Aero.panel(c,0xffffffff,0xffffffff,0,0xff7f9db9));crumbs.setContentDescription("Address bar");location.addView(crumbs,index,new LayoutParams(0,dp(28),1));
        LinearLayout toolbar=(LinearLayout)search.getParent();toolbar.removeView(search);search.setVisibility(GONE);
        View classicBar=explorerChrome(name->{});removeView(classicBar);removeView(toolbar);removeView(location);addView(classicBar,0);addView(toolbar,1);addView(location,2);
        classicBar.setBackgroundColor(XpTheme.surface(c));for(int i=0;i<((ViewGroup)classicBar).getChildCount();i++){View item=((ViewGroup)classicBar).getChildAt(i);if(item instanceof TextView)item.setOnClickListener(v->classicMenu(v,((TextView)v).getText().toString()));}
        for(TextView button:new TextView[]{back,forward,up}){((ViewGroup)button.getParent()).removeView(button);String kind=button==back?"back":button==forward?"forward":"up";button.setText(button==back?"Back":"");button.setTextSize(11);button.setSingleLine(true);XpNavigation icon=new XpNavigation(c,kind);icon.setBounds(0,0,dp(24),dp(24));button.setCompoundDrawables(icon,null,null,null);button.setCompoundDrawablePadding(dp(4));button.setBackground(Aero.selection(c));commands.addView(button,new LayoutParams(dp(button==back?68:36),dp(36)));}
        toolbar.removeView(folders);TextView find=command("Search",()->{search.setVisibility(search.getVisibility()==VISIBLE?GONE:VISIBLE);if(search.getVisibility()==VISIBLE)search.requestFocus();else search.setText("");});XpNavigation searchIcon=new XpNavigation(c,"search");searchIcon.setBounds(0,0,dp(22),dp(22));find.setCompoundDrawables(searchIcon,null,null,null);commands.addView(find,new LayoutParams(dp(78),dp(36)));commands.addView(folders,new LayoutParams(dp(60),dp(36)));
        HorizontalScrollView tools=new HorizontalScrollView(c);tools.setHorizontalScrollBarEnabled(false);toolbar.removeView(commands);tools.addView(commands);toolbar.addView(tools,new LayoutParams(-1,dp(38)));toolbar.setBackgroundColor(XpTheme.surface(c));location.setBackgroundColor(XpTheme.surface(c));
        addView(search,3,new LayoutParams(-1,dp(34)));TextView go=command("Go",()->navigate("home"));location.addView(go,new LayoutParams(dp(35),dp(28)));revealNavigationPane();
        addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(isPropertyPage())return;int signature=columns(classic?(smallIcons?128:108):210,classic?6:2);if(signature!=layoutSignature){layoutSignature=signature;render();}});start(initial);
    }
    private void classicMenu(View anchor,String name){DesktopContextMenu menu=new DesktopContextMenu(getContext(),"Control Panel "+name+" menu");
        if(name.equals("View")){menu.checked("Category View",!classic,()->selectView(false));menu.checked("Classic View",classic,()->selectView(true));menu.item("Refresh","refresh",this::render);}
        else if(name.equals("File")){menu.item("Close","empty",()->{View v=this;while(v.getParent() instanceof View){v=(View)v.getParent();if(v instanceof DesktopWindow){((DesktopWindow)v).closeWindow();return;}}});}
        else if(name.equals("Edit")){menu.item("Find a setting…","search",()->{search.setVisibility(VISIBLE);search.requestFocus();});}
        else if(name.equals("Favorites")){menu.item("My Computer","computer",()->actions.open("@computer"));menu.item("Display Properties","appearance",()->navigate("personalization"));}
        else if(name.equals("Tools")){menu.item("Android settings…","control",()->actions.action("android:settings"));}
        else menu.item("Help and Support","help",()->actions.open("@welcome"));ContextMenus.centered(anchor,(x,y)->ContextMenus.show(menu,anchor,x,y));
    }
    private int columns(int minimum,int maximum){int width=content.getWidth();if(width<=0)width=getWidth()>0?getWidth():dp(360);return Math.max(1,Math.min(maximum,(width-dp(32))/dp(minimum)));}
    private void selectView(boolean value){boolean same=page().equals("home")&&query().isEmpty();classic=value;store.prefs.edit().putBoolean("control_panel_classic",value).apply();navigate("home");if(same)render();}
    private void selectCategory(String key){classic=false;store.prefs.edit().putBoolean("control_panel_classic",false).apply();navigate("category:"+key);}
    private CheckBox usageCheckbox;
    private View usageHelp;

    boolean isDisplayPage(){return page().equals("personalization")||page().equals("colors")||page().equals("background");}
    private boolean isPropertyPage(){return isDisplayPage()||page().equals("system")||page().equals("taskbar");}
    private void updateDisplayCaption(){View current=this;while(current.getParent() instanceof View){current=(View)current.getParent();if(current instanceof DesktopWindow){((DesktopWindow)current).caption(isDisplayPage()?"Display Properties":page().equals("system")?"System Properties":page().equals("taskbar")?"Taskbar and Start Menu Properties":"Control Panel");break;}}}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();updateDisplayCaption();}
    private void renderDisplay(){
        updateDisplayCaption();
        if(display!=null)display.dispose();
        if(!displayPage.equals(page())){GadgetLayout.put(backgroundDraft,"xpTab",page().equals("colors")?"Appearance":page().equals("background")?"Desktop":backgroundDraft.optString("xpTab","Themes"));displayPage=page();}
        for(int i=0;i<getChildCount();i++)getChildAt(i).setVisibility(getChildAt(i)==displayHost?VISIBLE:GONE);
        displayHost.removeAllViews();display=new XpDisplayProperties(getContext(),store,backgroundDraft,"Themes",actions,()->{View v=this;while(v.getParent() instanceof View){v=(View)v.getParent();if(v instanceof DesktopWindow){((DesktopWindow)v).closeWindow();return;}}});displayHost.addView(display,new LayoutParams(-1,-1));
    }
    private void closeProperty(){View v=this;while(v.getParent() instanceof View){v=(View)v.getParent();if(v instanceof DesktopWindow){((DesktopWindow)v).closeWindow();return;}}}
    private void renderProperty(){
        updateDisplayCaption();if(appletPage.equals(page())&&displayHost.getChildCount()>0)return;
        if(taskbarProperties!=null){taskbarDraft=taskbarProperties.state();taskbarProperties=null;}
        if(display!=null){display.dispose();display=null;}for(int i=0;i<getChildCount();i++)getChildAt(i).setVisibility(getChildAt(i)==displayHost?VISIBLE:GONE);displayHost.removeAllViews();appletPage=page();
        if(page().equals("taskbar")){taskbarProperties=new XpTaskbarProperties(getContext(),store,actions,taskbarDraft,this::closeProperty);displayHost.addView(taskbarProperties,new LayoutParams(-1,-1));}
        else displayHost.addView(XpSystemProperties.create(getContext(),store,actions,this::closeProperty),new LayoutParams(-1,-1));
    }
    @Override void render(){
        if(isDisplayPage()){appletPage="";renderDisplay();return;}
        if(isPropertyPage()){renderProperty();return;}
        if(page().equals("sidebar")){replacePage("icons");return;}
        updateDisplayCaption();if(taskbarProperties!=null){taskbarDraft=taskbarProperties.state();taskbarProperties=null;}appletPage="";
        if(display!=null){display.dispose();display=null;}
        for(int i=0;i<getChildCount();i++)getChildAt(i).setVisibility(getChildAt(i)==displayHost?GONE:VISIBLE);search.setVisibility(query().isEmpty()?GONE:VISIBLE);displayHost.removeAllViews();
        usageCheckbox=null;usageHelp=null;
        if(nameInput!=null)nameDraft=nameInput.getText().toString();nameInput=null;renderPane();LinearLayout column=column();String page=page();updateAddress();status.setText("Nth Desktop  •  "+title(page));
        if(!query().isEmpty())searchResults(column);
        else if(page.equals("home")){if(classic){viewPicker(column);classicView(column);}else categoryHome(column);}
        else if(page.startsWith("category:")){panelHeading(column,title(page));for(ControlPanelCatalog.Panel panel:ControlPanelCatalog.category(page.substring(9)))applet(column,panel);}
        else switch(page){
            case "personalization":personalization(column);break;
            case "background":backgrounds(column);break;
            case "colors":colors(column);break;
            case "grouping":heading(column,"Group similar taskbar buttons","Each folder window keeps its own navigation and view state.");for(String mode:new String[]{"Never","When full","Always"}){RadioButton option=XpWidgets.radio(getContext());option.setText(mode);option.setChecked(store.prefs.getString("taskbar_grouping","Always").equals(mode));option.setContentDescription("Taskbar grouping: "+mode);option.setOnClickListener(v->{store.prefs.edit().putString("taskbar_grouping",mode).apply();actions.action("taskbar-refresh");render();});column.addView(option);}break;
            case "sounds":heading(column,"Desktop sound effects","Choose the cues you want to hear.");DesktopSounds.settings(getContext(),column);break;
            case "startup":startup(column);break;
            case "taskbar-options":heading(column,"Advanced taskbar settings","Android integration and Nth launcher options.");check(column,"Show Android status bar","show_status_bar",false,()->actions.action("chrome"));taskbarUsage(column);taskRow(column,"Quick Launch","Choose and order taskbar shortcuts","programs","open:@quicklaunch");taskRow(column,"Choose default home app","Use Nth Desktop as your home","control","home");break;
            case "sidebar":heading(column,"Gadget Settings","Arrange gadgets anywhere on your desktop.");check(column,"Keep gadgets above windows","sidebar_always_top",false,()->actions.action("sidebar-apply"));taskRow(column,"Show desktop gadgets","Restore the gadgets to their saved positions","gadgets","sidebar-show");taskRow(column,"Hide desktop gadgets","Hide your gadgets and keep their settings","close","sidebar-hide");taskRow(column,"Add gadgets…","Open Gadget Gallery","gadgets","open:@gadgets");break;
            case "icons":heading(column,"Desktop Settings","Choose your icon layout, grid and desktop shortcuts.");column.addView(new DesktopSettings(getContext(),store,()->actions.action("desktop")),new LayoutParams(-1,-2));section(column,"Built-in desktop icons");String[][] icons={{"@recycle","Recycle Bin"},{"@computer","My Computer"},{"@files","Files"},{"@notes","Notepad"},{"@photos","Picture and Fax Viewer"},{"@appearance","Display Properties"},{"@control","Control Panel"},{"@filemanagers","File Managers"}};for(String[] icon:icons){CheckBox box=checkbox(icon[1],store.pinned(icon[0]));box.setOnCheckedChangeListener((v,value)->{if(value!=store.pinned(icon[0]))store.toggle(icon[0]);actions.action("desktop");});column.addView(box);}link(column,"Manage all desktop shortcuts",()->actions.action("shortcuts"));link(column,"Restore original desktop icons",()->actions.action("reset-icons"));break;
            case "accounts":accounts(column);break;
            case "system":system(column);break;
            case "clock":heading(column,"Clock appearance","Your device's local time and time zone");GadgetView preview=new GadgetView(getContext(),"clock",store,null);column.addView(preview,new LayoutParams(dp(132),dp(128)));LinearLayout faces=new LinearLayout(getContext());for(int i=0;i<2;i++){final int style=i;TextView button=Aero.button(getContext(),i==0?"Classic ivory":"Black glass",()->{store.prefs.edit().putInt("clock_style",style).apply();actions.action("gadgets");render();});faces.addView(button,new LayoutParams(0,dp(44),1));}column.addView(faces);check(column,"Show second hand","clock_seconds",true,()->actions.action("gadgets"));break;
            default:heading(column,"Control Panel","Choose Control Panel Home to view your settings.");break;
        }
        ((View)commands.getParent()).setVisibility(VISIBLE);if(page.equals("home")&&!classic)content.setBackgroundColor(0xff6375d6);else content.setBackgroundColor(0xffffffff);body(scrolling(column));
    }
    private void viewPicker(LinearLayout column){
        LinearLayout header=Aero.column(getContext());
        panelHeading(header,classic?"Control Panel":"Pick a category");
        TextView selector=Aero.button(getContext(),classic?"Switch to Category View":"Switch to Classic View",()->selectView(!classic));
        LayoutParams bounds=new LayoutParams(-2,dp(38));bounds.gravity=Gravity.END;bounds.bottomMargin=dp(9);
        header.addView(selector,bounds);column.addView(header);
    }
    private void selectIconSize(boolean small){
        smallIcons=small;store.prefs.edit().putBoolean("control_panel_small_icons",small).apply();selectView(true);
    }
    private void personalization(LinearLayout column){
        panelHeading(column,"Display Properties");
        section(column,"Windows and buttons");
        column.addView(new XpThemes(getContext(),store,columns(145,3),()->actions.action("xp-theme")));
        section(column,"My Themes");
        taskLink(column,"Save current theme…","theme-save");
        taskLink(column,"Import an Nth theme…","theme-import");
        JSONArray themes=ThemeProfiles.saved(store);
        for(int i=0;i<themes.length();i++){
            JSONObject theme=themes.optJSONObject(i);
            if(theme!=null)taskLink(column,theme.optString("name","Saved theme"),"theme-preview:"+i);
        }
        section(column,"Desktop appearance");
        for(ControlPanelCatalog.Task task:ControlPanelCatalog.panel("personalization").tasks){
            ControlPanelCatalog.Panel owner=ControlPanelCatalog.owner(task.destination);
            taskRow(column,task.title,task.description,iconFor(task.destination,owner.icon),task.destination);
        }
        taskRow(column,"Sounds","Choose optional desktop sound effects","sound","page:sounds");
    }
    private void panelHeading(LinearLayout parent,String title){TextView text=Aero.text(getContext(),title,17,0xff003399);text.setPadding(0,dp(5),0,dp(18));parent.addView(text);}
    private String iconFor(String destination,String fallback){switch(destination){case "page:colors":return "color";case "page:background":return "pictures";case "page:startup":return "sound";default:return fallback;}}
    private void destination(String value){if(value.startsWith("page:"))navigate(value.substring(5));else if(value.startsWith("open:"))actions.open(value.substring(5));else actions.action(value);}
    private TextView taskLink(LinearLayout parent,String title,String destination){TextView link=Aero.text(getContext(),title,12,0xff0066cc);link.setMinHeight(dp(32));link.setPadding(0,dp(4),dp(4),dp(4));link.setBackground(Aero.selection(getContext()));link.setFocusable(true);link.setContentDescription(title);link.setTag("control-destination:"+destination);link.setOnClickListener(v->destination(destination));parent.addView(link,new LayoutParams(-1,-2));return link;}
    private void taskRow(LinearLayout parent,String title,String description,String icon,String destination){task(parent,icon,title,description,()->destination(destination));View row=parent.getChildAt(parent.getChildCount()-1);row.setTag("control-destination:"+destination);}
    private void categoryHome(LinearLayout parent){
        parent.setBackground(Aero.panel(getContext(),0xff667bd5,0xff6375d6,0,0));TextView heading=Aero.text(getContext(),"Pick a category",22,0xffd6def5);Aero.bold(heading);heading.setPadding(dp(4),dp(5),0,dp(20));parent.addView(heading);
        GridLayout grid=new GridLayout(getContext());grid.setContentDescription("Control Panel categories");grid.setColumnCount(columns(210,2));parent.addView(grid,new LayoutParams(-1,-2));
        for(String[] category:CATEGORIES){LinearLayout cell=new LinearLayout(getContext());cell.setGravity(Gravity.CENTER_VERTICAL);cell.setPadding(0,dp(7),dp(8),dp(12));cell.setBackground(Aero.selection(getContext()));cell.setContentDescription(category[1]);cell.setFocusable(true);cell.setOnClickListener(v->selectCategory(category[0]));cell.addView(new AeroIcon(getContext(),category[2]),new LayoutParams(dp(42),dp(42)));TextView title=Aero.text(getContext(),category[1],12,0xffffffff);Aero.bold(title);title.setPadding(dp(10),0,0,0);cell.addView(title,new LayoutParams(0,dp(52),1));gridCell(grid,cell);}
    }
    private List<ControlPanelCatalog.Task> homeTasks(String key){String[][] choices;switch(key){
        case "system":choices=new String[][]{{"View device information","page:system"},{"Back up your desktop","open:@backup"},{"Review security settings","android:security"}};break;
        case "network":choices=new String[][]{{"View network connections","android:network"},{"Connect to Wi-Fi","android:wifi"}};break;
        case "hardware":choices=new String[][]{{"Adjust system sound","android:sound"},{"Change display settings","android:display"}};break;
        case "programs":choices=new String[][]{{"Manage installed apps","android:apps"},{"Choose default programs","android:defaults"}};break;
        case "accounts":choices=new String[][]{{"Change your desktop name","page:accounts"},{"Manage device accounts","android:accounts"}};break;
        case "appearance":choices=new String[][]{{"Change the theme","page:personalization"},{"Change desktop background","page:background"}};break;
        case "clock":choices=new String[][]{{"Set the date and time","android:date"},{"Change your language","android:language"}};break;
        default:choices=new String[][]{{"Open accessibility settings","android:accessibility"}};break;
        }List<ControlPanelCatalog.Task> result=new ArrayList<>();for(String[] task:choices)result.add(new ControlPanelCatalog.Task(task[0],"",task[1]));return result;}
    private void applet(LinearLayout parent,ControlPanelCatalog.Panel panel){LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.TOP);row.setPadding(0,dp(7),0,dp(14));row.addView(new AeroIcon(getContext(),panel.icon),new LayoutParams(dp(32),dp(32)));LinearLayout text=Aero.column(getContext());text.setPadding(dp(12),0,0,0);TextView title=Aero.text(getContext(),panel.title,15,0xff003399);title.setMinHeight(dp(32));title.setContentDescription(panel.title);title.setTag("control-destination:"+panel.destination);title.setBackground(Aero.selection(getContext()));title.setFocusable(true);title.setOnClickListener(v->destination(panel.destination));text.addView(title,new LayoutParams(-1,-2));TextView description=Aero.text(getContext(),panel.description,11,0xff56646e);description.setPadding(0,0,0,dp(4));text.addView(description);for(ControlPanelCatalog.Task task:panel.tasks)taskLink(text,task.title,task.destination);row.addView(text,new LayoutParams(0,-2,1));parent.addView(row,new LayoutParams(-1,-2));}
    private void classicView(LinearLayout parent){GridLayout grid=new GridLayout(getContext());grid.setContentDescription("Classic View applets");grid.setColumnCount(columns(smallIcons?128:108,6));parent.addView(grid,new LayoutParams(-1,-2));for(ControlPanelCatalog.Panel panel:ControlPanelCatalog.classic()){LinearLayout tile=Aero.column(getContext());tile.setGravity(smallIcons?Gravity.CENTER_VERTICAL:Gravity.TOP|Gravity.CENTER_HORIZONTAL);tile.setOrientation(smallIcons?HORIZONTAL:VERTICAL);tile.setPadding(dp(6),dp(10),dp(6),dp(8));int iconSize=smallIcons?22:40;tile.addView(new AeroIcon(getContext(),panel.icon),new LayoutParams(dp(iconSize),dp(iconSize)));TextView label=Aero.text(getContext(),panel.title,11,Aero.INK);label.setGravity(smallIcons?Gravity.CENTER_VERTICAL:Gravity.TOP|Gravity.CENTER_HORIZONTAL);label.setPadding(smallIcons?dp(5):0,smallIcons?0:dp(7),0,0);tile.addView(label,smallIcons?new LayoutParams(0,dp(35),1):new LayoutParams(-1,dp(54)));tile.setContentDescription(panel.title);tile.setTag("control-destination:"+panel.destination);tile.setBackground(Aero.selection(getContext()));tile.setFocusable(true);tile.setOnClickListener(v->destination(panel.destination));gridCell(grid,tile);}}
    private void gridCell(GridLayout grid,View cell){GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);lp.width=0;lp.setMargins(0,0,dp(7),dp(5));grid.addView(cell,lp);}
    private void searchResults(LinearLayout parent){
        panelHeading(parent,"Search results");List<ControlPanelCatalog.Hit> hits=ControlPanelCatalog.search(query());
        for(ControlPanelCatalog.Panel panel:ControlPanelCatalog.PANELS){List<ControlPanelCatalog.Hit> group=new ArrayList<>();ControlPanelCatalog.Hit primary=null;for(ControlPanelCatalog.Hit hit:hits)if(hit.owner==panel){group.add(hit);if(hit.destination.equals(panel.destination))primary=hit;}if(group.isEmpty())continue;
            LinearLayout header=new LinearLayout(getContext());header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(0,dp(12),0,dp(3));header.addView(new AeroIcon(getContext(),panel.icon),new LayoutParams(dp(26),dp(26)));String label=primary==null?panel.title:primary.title;TextView title=Aero.text(getContext(),label,15,0xff003399);title.setPadding(dp(10),0,0,0);header.addView(title,new LayoutParams(0,-2,1));if(primary!=null){header.setContentDescription(label);header.setTag("control-destination:"+primary.destination);header.setBackground(Aero.selection(getContext()));header.setFocusable(true);header.setMinimumHeight(dp(38));header.setOnClickListener(v->destination(panel.destination));}parent.addView(header);
            for(ControlPanelCatalog.Hit hit:group)if(hit!=primary){TextView link=taskLink(parent,hit.title,hit.destination);link.setPadding(dp(36),dp(4),0,dp(4));}
        }if(hits.isEmpty())heading(parent,"No matching settings","Try wallpaper, opacity, wireless or desktop grid.");
    }
    private String[][] related(){switch(page()){case "home":return new String[0][];case "personalization":return new String[][]{{"Desktop Settings","page:icons"},{"Taskbar and Start Menu","page:taskbar"},{"Display Settings","android:display"}};case "icons":return new String[][]{{"Display Properties","page:personalization"},{"Taskbar and Start Menu","page:taskbar"}};case "taskbar":return new String[][]{{"Display Properties","page:personalization"},{"Desktop Settings","page:icons"}};case "sidebar":return new String[][]{{"Display Properties","page:personalization"},{"Desktop Settings","page:icons"}};case "system":return new String[][]{{"Android device information","android:device"},{"Task Manager","open:@taskmanager"},{"Backup and Restore","open:@backup"}};case "clock":return new String[][]{{"Date and Time","android:date"}};default:return new String[0][];}}
    private void relatedFooter(LinearLayout parent){String[][] links=related();if(links.length==0)return;TextView title=Aero.text(getContext(),"See also",12,0xff596c79);title.setPadding(0,dp(20),0,dp(5));parent.addView(title);for(String[] link:links)taskLink(parent,link[0],link[1]);}
    private void paneHeading(String value){paneGroup=XpTaskPane.add(side,value,value.equals("Control Panel")).items;}
    private void paneLink(String title,boolean selected,Runnable action){LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(2),dp(4),dp(2),dp(4));row.setMinimumHeight(dp(33));row.setBackground(XpControls.menuSelection(getContext()));row.setFocusable(true);row.setContentDescription(title);row.addView(new AeroIcon(getContext(),title.contains("Help")?"help":"control"),new LayoutParams(dp(17),dp(17)));TextView link=Aero.text(getContext(),title,11,0xff214477);link.setTextColor(XpControls.menuInk(0xff214477));link.setDuplicateParentStateEnabled(true);link.setPadding(dp(5),0,0,0);if(selected)Aero.bold(link);row.addView(link,new LayoutParams(0,-2,1));row.setOnClickListener(v->action.run());paneGroup.addView(row);}
    private void renderPane(){side.removeAllViews();paneHeading("Control Panel");if(!page().equals("home"))paneLink("Control Panel Home",false,()->navigate("home"));paneLink(classic?"Switch to Category View":"Switch to Classic View",false,()->selectView(!classic));paneHeading("See also");paneLink("Help and Support",false,()->actions.open("@welcome"));paneLink("Other Control Panel Options",false,()->actions.action("android:settings"));for(String[] item:related())paneLink(item[0],false,()->destination(item[1]));}
    private String title(String page){if(page.startsWith("category:"))for(String[] category:CATEGORIES)if(category[0].equals(page.substring(9)))return category[1];ControlPanelCatalog.Panel owner=ControlPanelCatalog.owner("page:"+page);if(owner!=null){if(owner.destination.equals("page:"+page))return owner.title;for(ControlPanelCatalog.Task task:owner.tasks)if(task.destination.equals("page:"+page))return task.title;}return "Control Panel";}
    void wallpaperChosen(List<String> photos){
        BackgroundPicker.imported(backgroundDraft,photos);
        if(isDisplayPage()){GadgetLayout.put(backgroundDraft,"xpTab","Desktop");render();}else if(page().equals("background"))render();else navigate("background");
    }
    private void backgrounds(LinearLayout column){
        column.addView(new BackgroundPicker(getContext(),store,backgroundDraft,()->actions.action("wallpaper-browse"),
                ()->actions.action("wallpaper-folder"),()->{actions.action("wallpaper");render();}),new LayoutParams(-1,-2));
    }

    private void colors(LinearLayout column){
        heading(column,"Appearance","Choose a color scheme for windows, the Start menu and taskbar.");
        column.addView(new XpThemes(getContext(),store,columns(145,3),()->actions.action("xp-theme")));
        link(column,"Desktop background",()->navigate("background"));
        link(column,"Icon size and spacing",()->navigate("icons"));
    }
    private void percentSlider(LinearLayout column,String title,String key,int fallback){
        TextView label=Aero.text(getContext(),title+": "+store.prefs.getInt(key,fallback)+"%",13,Aero.INK);column.addView(label,new LayoutParams(-1,dp(34)));
        SeekBar slider=XpWidgets.seek(getContext());slider.setMax(100);slider.setProgress(store.prefs.getInt(key,fallback));slider.setContentDescription(title);slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int value,boolean user){if(!user)return;store.prefs.edit().putInt(key,value).apply();label.setText(title+": "+value+"%");actions.action("glass");}});column.addView(slider,new LayoutParams(-1,dp(44)));
    }
    private void startup(LinearLayout column){
        heading(column,"Startup and Sounds","A short XP-style loading bar and a welcome screen. Ordinary Home presses return straight to your desktop.");RadioGroup modes=new RadioGroup(getContext());String[] names={"First launch only","Once per phone reboot","Off"};for(int i=0;i<names.length;i++){RadioButton mode=XpWidgets.radio(getContext());mode.setId(500+i);mode.setText(names[i]);mode.setContentDescription(names[i]);mode.setTypeface(Aero.face(getContext()));mode.setTextSize(13);mode.setTextColor(Aero.INK);mode.setButtonDrawable(new AeroCheck(getContext(),true));mode.setMinHeight(dp(44));modes.addView(mode,new RadioGroup.LayoutParams(-1,dp(44)));}modes.check(500+store.prefs.getInt("startup_mode",0));modes.setOnCheckedChangeListener((group,id)->store.prefs.edit().putInt("startup_mode",id-500).apply());column.addView(modes);
        check(column,"Play the original Nth startup chime","startup_sound",false,()->{});TextView sound=Aero.text(getContext(),"Sound follows your phone’s sound mode and stays quiet while music is playing.",12,0xff586976);sound.setPadding(0,dp(4),0,dp(10));column.addView(sound);check(column,"Reduce motion","reduce_motion",false,()->{});TextView reduced=Aero.text(getContext(),"Also follows Android’s animation setting. Reduced motion uses a brief, silent still welcome.",12,0xff586976);reduced.setPadding(0,dp(4),0,dp(12));column.addView(reduced);TextView preview=Aero.button(getContext(),"Preview startup",()->actions.action("startup-preview"));preview.setContentDescription("Preview startup");column.addView(preview,new LayoutParams(-1,dp(44)));link(column,"Desktop sound effects",()->navigate("sounds"));link(column,"Open Welcome Center",()->actions.open("@welcome"));
    }
    private void selectWallpaper(int value){store.prefs.edit().putInt("wallpaper",value).apply();wallpaper.theme(value);actions.action("wallpaper");render();}
    private void taskbarUsage(LinearLayout column){
        usageCheckbox=checkbox("Include apps opened outside Nth Desktop",false);
        usageCheckbox.setOnClickListener(view->{
            CheckBox box=(CheckBox)view;
            boolean enable=box.isChecked();
            if(enable&&!ExternalTasks.permitted(getContext()))box.setChecked(false);
            actions.action(enable?"taskbar-usage-enable":"taskbar-usage-disable");
        });
        column.addView(usageCheckbox);
        column.addView(Aero.text(getContext(),"Optional Usage Access adds taskbar buttons for apps you open elsewhere.",12,0xff586976));

        LinearLayout help=Aero.column(getContext());
        help.setPadding(0,dp(5),0,0);
        help.addView(Aero.text(getContext(),"Turn on Android’s Usage Access switch for "
                +getContext().getString(R.string.app_name)+", then press Back.",12,0xff586976));
        if(Build.VERSION.SDK_INT>=33){
            TextView blocked=Aero.text(getContext(),"If Android says access was denied, open App info. "
                    +"Tap ⋮, choose Allow restricted settings, then press Back to continue.",12,0xff586976);
            blocked.setPadding(0,dp(5),0,0);
            help.addView(blocked);
            link(help,"Open App info",()->actions.action("taskbar-usage-app-info"))
                    .setContentDescription("Open App info for Usage Access");
        }
        column.addView(help);
        usageHelp=help;
        TextView privacy=Aero.text(getContext(),"Nth Desktop XP checks recent app usage when you return to the desktop. "
                +"Taskbar buttons stay in memory for the current session. This feature does not save usage history or run a background service.",12,0xff586976);
        privacy.setPadding(0,dp(5),0,dp(8));
        column.addView(privacy);
        refreshUsageAccess(ExternalTasks.permitted(getContext()));
    }

    void refreshUsageAccess(boolean permitted){
        if(usageCheckbox==null)return;
        usageCheckbox.setChecked(ExternalTasks.includesExternalApps(store,permitted));
        usageHelp.setVisibility(permitted?GONE:VISIBLE);
    }

    private CheckBox checkbox(String title,boolean checked){return Aero.check(getContext(),title,checked);}
    private void check(LinearLayout column,String title,String key,boolean fallback,Runnable changed){CheckBox box=checkbox(title,store.prefs.getBoolean(key,fallback));box.setOnCheckedChangeListener((v,value)->{store.prefs.edit().putBoolean(key,value).apply();changed.run();});column.addView(box);}
    private void accounts(LinearLayout column){heading(column,"Change your desktop name","This name appears at the top of your Start menu.");nameInput=new EditText(getContext());nameInput.setSingleLine(true);nameInput.setTypeface(Aero.face(getContext()));nameInput.setTextSize(15);nameInput.setContentDescription("Desktop name");nameInput.setText(nameDraft==null?store.prefs.getString("desktop_name",getContext().getString(R.string.app_name)):nameDraft);column.addView(nameInput,new LayoutParams(-1,dp(46)));column.addView(Aero.button(getContext(),"Save name",()->{String value=nameInput.getText().toString().trim();if(value.isEmpty()){nameInput.setError("Enter a name");return;}value=value.substring(0,Math.min(40,value.length()));store.prefs.edit().putString("desktop_name",value).apply();nameDraft=value;status.setText("Desktop name saved");Toast.makeText(getContext(),"Desktop name saved",Toast.LENGTH_SHORT).show();actions.action("profile");}));section(column,"Your picture");column.addView(ProfilePortrait.view(getContext(),store),new LayoutParams(dp(74),dp(74)));link(column,"Choose a profile picture…",()->{nameDraft=nameInput.getText().toString();actions.action("profile-pick");}).setContentDescription("Choose a profile picture…");link(column,"Use the Nth picture",()->{store.prefs.edit().remove("profile_portrait").apply();actions.action("profile");render();});link(column,"Manage device accounts in Android",()->actions.action("android:accounts"));}
    private void system(LinearLayout column){
        heading(column,"View basic information about your computer","");section(column,getContext().getString(R.string.app_name)+" edition");
        LinearLayout edition=new LinearLayout(getContext());edition.setGravity(Gravity.CENTER_VERTICAL);LinearLayout identity=Aero.column(getContext());
        String appName=getContext().getString(R.string.app_name);
        TextView brand=Aero.text(getContext(),appName,24,0xff1e4e76);identity.addView(brand);
        TextView version=Aero.text(getContext(),"Version "+AppInfo.version(getContext())+"  •  Build "+AppInfo.build(getContext()),12,0xff60717d);version.setContentDescription("Installed Nth Desktop version");version.setPadding(0,dp(9),0,0);identity.addView(version);edition.addView(identity,new LayoutParams(0,-2,1));edition.addView(new AeroIcon(getContext(),"orb"),new LayoutParams(dp(85),dp(85)));edition.setPadding(dp(4),dp(8),dp(4),dp(12));column.addView(edition);
        property(column,"Android version",Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")");section(column,"System");property(column,"Manufacturer",Build.MANUFACTURER);property(column,"Model",Build.MODEL);
        String processor=Build.VERSION.SDK_INT>=31&&!Build.SOC_MODEL.equals(Build.UNKNOWN)?Build.SOC_MODEL:Build.HARDWARE;property(column,"Processor",processor+"  •  "+Runtime.getRuntime().availableProcessors()+" logical processors");
        android.app.ActivityManager manager=(android.app.ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);if(manager!=null){android.app.ActivityManager.MemoryInfo memory=new android.app.ActivityManager.MemoryInfo();manager.getMemoryInfo(memory);property(column,"Installed memory (RAM)",android.text.format.Formatter.formatFileSize(getContext(),memory.totalMem));}
        property(column,"System type",(Build.SUPPORTED_64_BIT_ABIS.length>0?"64-bit":"32-bit")+" Android"+(Build.SUPPORTED_ABIS.length>0?" ("+Build.SUPPORTED_ABIS[0]+")":""));
        section(column,"Device and storage");
        property(column,"Android build",Build.DISPLAY);
        try{StatFs disk=new StatFs(Environment.getDataDirectory().getPath());property(column,"Internal storage",android.text.format.Formatter.formatFileSize(getContext(),disk.getAvailableBytes())+" free of "+android.text.format.Formatter.formatFileSize(getContext(),disk.getTotalBytes()));}catch(RuntimeException ignored){}
        android.util.DisplayMetrics display=getResources().getDisplayMetrics();property(column,"Display",display.widthPixels+" × "+display.heightPixels+" pixels");section(column,"Computer name and device settings");property(column,"Computer name",store.prefs.getString("desktop_name",getContext().getString(R.string.app_name)));link(column,"Change desktop name",()->navigate("accounts"));link(column,"Android device information",()->actions.action("android:device"));
        section(column,"Default home app");boolean home=getContext() instanceof MainActivity&&((MainActivity)getContext()).isHome();TextView state=Aero.text(getContext(),home?"Nth Desktop is your default home app.":"Choose Nth Desktop as your default home app.",13,0xff334c5d);state.setContentDescription("Desktop home status");state.setPadding(dp(4),dp(10),0,dp(5));column.addView(state);link(column,"Default home app",()->actions.action("home"));String about="About "+appName;link(column,about,()->Aero.dialog(getContext()).setTitle(about).setMessage(appName+" "+AppInfo.version(getContext())+"\nNth Experience\n\nOriginal artwork and native desktop code. Selawik font by Microsoft, used under the SIL Open Font License.").setPositiveButton("OK",null).show());
    }
    private void section(LinearLayout column,String title){TextView text=Aero.text(getContext(),title,14,0xff003399);text.setPadding(0,dp(17),0,dp(7));column.addView(text);View line=new View(getContext());line.setBackgroundColor(0xffd7e2ea);column.addView(line,new LayoutParams(-1,dp(1)));}
    private void property(LinearLayout column,String name,String value){LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.TOP);row.setPadding(dp(4),dp(7),dp(2),dp(5));TextView label=Aero.text(getContext(),name+":",12,0xff5c7180),text=Aero.text(getContext(),value,12,0xff263e4e);text.setTag("system:"+name);label.setPadding(0,0,dp(9),0);row.addView(label,new LayoutParams(0,-2,.43f));row.addView(text,new LayoutParams(0,-2,.57f));column.addView(row,new LayoutParams(-1,-2));}
    @Override void showFolders(){DesktopContextMenu menu=new DesktopContextMenu(getContext(),"Control Panel navigation");menu.checked("Control Panel Home",page().equals("home")&&!classic,()->selectView(false));menu.checked("Classic View",page().equals("home")&&classic,()->selectView(true));menu.separator();for(String[] category:CATEGORIES)menu.item(category[1],category[2],()->selectCategory(category[0]));ContextMenus.centered(folders,(x,y)->ContextMenus.show(menu,folders,x,y));}
    private String parentPage(String value){return ControlPanelCatalog.parent(value);}
    private void updateAddress(){
        crumbs.removeAllViews();TextView label=Aero.text(getContext(),"Address",11,Aero.INK);label.setPadding(dp(5),0,dp(6),0);crumbs.addView(label,new LayoutParams(-2,-1));String path=page().equals("home")?"Control Panel":"Control Panel\\"+title(page());TextView value=command(path,()->navigate("home"));value.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);value.setSingleLine(true);value.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);value.setContentDescription("Control Panel address");crumbs.addView(value,new LayoutParams(0,-1,1));
    }
    @Override boolean canGoUp(){return parentPage(page())!=null;}
    @Override void goUp(){String parent=parentPage(page());if(parent!=null)navigate(parent);}
    @Override void close(){if(display!=null)display.dispose();}
    @Override JSONObject extraState(){JSONObject result=new JSONObject();GadgetLayout.put(result,"backgroundDraft",backgroundDraft);GadgetLayout.put(result,"classic",classic);if(taskbarProperties!=null)GadgetLayout.put(result,"taskbarDraft",taskbarProperties.state());if(nameInput!=null)GadgetLayout.put(result,"nameDraft",nameInput.getText().toString());return result;}
    @Override void restoreExtra(JSONObject value){if(value!=null){JSONObject saved=value.optJSONObject("backgroundDraft");if(saved!=null)backgroundDraft=saved;classic=value.optBoolean("classic");taskbarDraft=value.optJSONObject("taskbarDraft");nameDraft=value.optString("nameDraft",null);}}
}
