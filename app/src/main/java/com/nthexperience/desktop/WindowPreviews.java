package com.nthexperience.desktop;

import android.app.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;

/** XP uses icon switching and task group lists, rather than DWM thumbnails. */
final class WindowPreviews {
    interface Host {List<DesktopWindow> windows();List<String> external();String label(String id);void open(String id);View wallpaper();void showDesktop();void preparePreview();}
    static final class Card {final String title,detail;final View source;final Runnable open,close;Card(String title,String detail,View source,Runnable open,Runnable close){this.title=title;this.detail=detail;this.source=source;this.open=open;this.close=close;}}
    private final Activity activity;private final Host host;private Dialog dialog;private DesktopContextMenu groupMenu;
    WindowPreviews(Activity activity,Host host){this.activity=activity;this.host=host;}
    static boolean allowed(DesktopWindow window){return !(window.body instanceof BrowserPanel)||!((BrowserPanel)window.body).hasPrivateTabs();}
    void bindDesktopPeek(View button){button.setTooltipText("Show desktop");button.setOnHoverListener(null);}
    void bindTask(View button,List<DesktopWindow> windows,boolean mouse,Runnable commands){
        button.setOnHoverListener(null);button.setOnLongClickListener(v->{commands.run();return true;});
        if(windows.size()>1)button.setOnClickListener(v->{dismiss();groupMenu=new DesktopContextMenu(activity,"Grouped tasks");for(DesktopWindow window:windows)if(window.getParent()!=null)groupMenu.item(window.caption(),"files",window::restore);ContextMenus.centered(button,(x,y)->ContextMenus.show(groupMenu,button,x,y));});
    }
    void bindApp(View button,String title,boolean running,boolean mouse,Runnable open,Runnable commands){button.setOnHoverListener(null);button.setOnLongClickListener(v->{commands.run();return true;});}
    void show(){
        dismiss();host.preparePreview();((InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(),0);
        LinearLayout shell=Aero.column(activity);shell.setPadding(dp(4),dp(4),dp(4),dp(4));shell.setBackground(XpTheme.chrome(activity,AeroGlass.WINDOW));TextView caption=Aero.text(activity,"Switch Programs",12,XpTheme.titleInk(activity));Aero.bold(caption);caption.setPadding(dp(7),0,0,0);caption.setGravity(Gravity.CENTER_VERTICAL);shell.addView(caption,new LinearLayout.LayoutParams(-1,dp(27)));
        LinearLayout body=Aero.column(activity);body.setPadding(dp(12),dp(12),dp(12),dp(9));body.setBackgroundColor(XpTheme.surface(activity));shell.addView(body);GridLayout icons=new GridLayout(activity);icons.setColumnCount(4);icons.setContentDescription("XP program switcher");body.addView(icons);
        for(DesktopWindow window:host.windows())if(window.getParent()!=null)add(icons,window.caption(),window.toolId,()->{dismiss();window.restore();});for(String id:host.external())add(icons,host.label(id),id,()->{dismiss();host.open(id);});
        if(icons.getChildCount()==0)body.addView(Aero.text(activity,"No open programs",12,Aero.INK));body.addView(Aero.button(activity,"Cancel",this::dismiss),new LinearLayout.LayoutParams(-1,dp(32)));
        dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);dialog.setContentView(shell);dialog.show();if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().setLayout(Math.min(dp(354),activity.getResources().getDisplayMetrics().widthPixels-dp(24)),-2);}
    }
    private void add(GridLayout icons,String title,String id,Runnable click){LinearLayout item=Aero.column(activity);item.setGravity(Gravity.CENTER);item.setPadding(dp(4),dp(7),dp(4),dp(7));item.setFocusable(true);item.setBackground(XpControls.menuSelection(activity));item.setContentDescription("Switch to "+title);item.addView(new AeroIcon(activity,id.equals("@taskmanager")?"taskmanager":id.equals("@browser")?"browser":id.equals("@notes")?"notes":"files"),new LinearLayout.LayoutParams(dp(32),dp(32)));TextView label=Aero.text(activity,title,10,Aero.INK);label.setTextColor(XpControls.menuInk(Aero.INK));label.setDuplicateParentStateEnabled(true);label.setGravity(Gravity.CENTER);label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);item.addView(label,new LinearLayout.LayoutParams(-1,dp(30)));item.setOnClickListener(v->click.run());GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=dp(81);lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);icons.addView(item,lp);}
    void flip(){show();}
    boolean dismiss(){boolean shown=dialog!=null&&dialog.isShowing()||groupMenu!=null&&groupMenu.showing();if(dialog!=null){dialog.dismiss();dialog=null;}if(groupMenu!=null){groupMenu.dismiss();groupMenu=null;}return shown;}
    private int dp(int n){return Aero.dp(activity,n);}
}
