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
    private final List<Choice> choices=new ArrayList<>();
    private final List<View> cells=new ArrayList<>();
    private TextView selectedTitle;
    private int selected;
    private boolean keyboard;
    private static final class Choice {
        final String title,id;final Runnable activate;
        Choice(String title,String id,Runnable activate){this.title=title;this.id=id;this.activate=activate;}
    }
    void show(){begin(false,false);}
    boolean key(KeyEvent event){
        int key=event.getKeyCode();boolean down=event.getAction()==KeyEvent.ACTION_DOWN;
        if(down&&event.isAltPressed()&&key==KeyEvent.KEYCODE_TAB){
            if(dialog==null||!dialog.isShowing())begin(true,event.isShiftPressed());
            else cycle(event.isShiftPressed()?-1:1);
            return true;
        }
        if(dialog==null||!dialog.isShowing())return false;
        if(!down&&(key==KeyEvent.KEYCODE_ALT_LEFT||key==KeyEvent.KEYCODE_ALT_RIGHT)&&keyboard){accept();return true;}
        if(down&&key==KeyEvent.KEYCODE_ESCAPE){dismiss();return true;}
        if(down&&key==KeyEvent.KEYCODE_ENTER){accept();return true;}
        if(down&&(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT)){cycle(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);return true;}
        return false;
    }
    private void begin(boolean usingKeyboard,boolean backwards){
        dismiss();keyboard=usingKeyboard;host.preparePreview();((InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(),0);
        List<DesktopWindow> windows=new ArrayList<>(host.windows());Collections.reverse(windows);
        for(DesktopWindow window:windows)if(window.getParent()!=null)choices.add(new Choice(window.caption(),window.toolId,()->{if(window.getParent()!=null)window.restore();}));
        for(String id:host.external())choices.add(new Choice(host.label(id),id,()->host.open(id)));
        int width=Math.min(dp(380),activity.getResources().getDisplayMetrics().widthPixels-dp(24));
        int columns=Math.max(1,Math.min(7,(width-dp(24))/dp(48)));
        LinearLayout shell=Aero.column(activity);shell.setPadding(dp(8),dp(12),dp(8),dp(8));shell.setBackground(Aero.panel(activity,XpTheme.surface(activity),XpTheme.surface(activity),0,XpTheme.palette(activity)[1]));
        GridLayout icons=new GridLayout(activity);icons.setColumnCount(columns);icons.setContentDescription("XP program switcher");
        for(int i=0;i<choices.size();i++){
            int index=i;Choice choice=choices.get(i);FrameLayout cell=new FrameLayout(activity);cell.setPadding(dp(6),dp(6),dp(6),dp(6));cell.setFocusable(true);cell.setContentDescription("Switch to "+choice.title);cell.setTooltipText(choice.title);
            String icon=choice.id.equals("@taskmanager")?"taskmanager":choice.id.equals("@browser")?"browser":choice.id.equals("@notes")?"notes":choice.id.equals(MediaLibrary.PLAYER)?"mediaplayer":choice.id.equals("@control")?"control":"files";
            cell.addView(new AeroIcon(activity,icon),new FrameLayout.LayoutParams(dp(32),dp(32),Gravity.CENTER));
            cell.setOnClickListener(v->{select(index);accept();});cell.setOnFocusChangeListener((v,focused)->{if(focused)select(index);});
            GridLayout.LayoutParams size=new GridLayout.LayoutParams();size.width=dp(48);size.height=dp(48);icons.addView(cell,size);cells.add(cell);
        }
        ScrollView scroll=new ScrollView(activity);scroll.addView(icons);shell.addView(scroll,new LinearLayout.LayoutParams(-1,Math.min(dp(48)*Math.max(1,(choices.size()+columns-1)/columns),Math.max(dp(48),activity.getResources().getDisplayMetrics().heightPixels-dp(160)))));
        selectedTitle=Aero.text(activity,"No open programs",12,Aero.INK);selectedTitle.setContentDescription("Selected program");selectedTitle.setGravity(Gravity.CENTER);selectedTitle.setSingleLine();selectedTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);selectedTitle.setBackground(Aero.panel(activity,XpTheme.surface(activity),XpTheme.surface(activity),0,0xff9c9b97));LinearLayout.LayoutParams label=new LinearLayout.LayoutParams(-1,dp(30));label.topMargin=dp(8);shell.addView(selectedTitle,label);
        if(!usingKeyboard)shell.addView(Aero.button(activity,"Cancel",this::dismiss),new LinearLayout.LayoutParams(-1,dp(32)));
        dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);dialog.setContentView(shell);dialog.setOnKeyListener((d,k,e)->key(e));dialog.show();
        if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);dialog.getWindow().setWindowAnimations(0);dialog.getWindow().setLayout(width,-2);}
        select(usingKeyboard&&choices.size()>1?(backwards?choices.size()-1:1):0);
    }
    private void cycle(int delta){if(!choices.isEmpty())select(Math.floorMod(selected+delta,choices.size()));}
    private void select(int index){
        if(choices.isEmpty())return;selected=Math.floorMod(index,choices.size());
        for(int i=0;i<cells.size();i++){View cell=cells.get(i);cell.setSelected(i==selected);android.graphics.drawable.GradientDrawable outline=new android.graphics.drawable.GradientDrawable();outline.setColor(XpTheme.surface(activity));if(i==selected)outline.setStroke(dp(2),0xff316ac5);cell.setBackground(outline);}
        selectedTitle.setText(choices.get(selected).title);View cell=cells.get(selected);cell.requestRectangleOnScreen(new android.graphics.Rect(0,0,cell.getWidth(),cell.getHeight()),true);
    }
    private void accept(){if(choices.isEmpty()){dismiss();return;}Runnable action=choices.get(selected).activate;dismiss();action.run();}
    void flip(){show();}
    boolean dismiss(){boolean shown=dialog!=null&&dialog.isShowing()||groupMenu!=null&&groupMenu.showing();if(dialog!=null){dialog.dismiss();dialog=null;}choices.clear();cells.clear();selectedTitle=null;keyboard=false;if(groupMenu!=null){groupMenu.dismiss();groupMenu=null;}return shown;}
    private int dp(int n){return Aero.dp(activity,n);}
}
