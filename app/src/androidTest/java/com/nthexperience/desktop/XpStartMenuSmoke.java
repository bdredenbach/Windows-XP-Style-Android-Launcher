package com.nthexperience.desktop;
import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Real holds in both the Start Activity and separate All Programs PopupWindows. */
public final class XpStartMenuSmoke extends Instrumentation {
    MainActivity home;View root;DesktopStore store;int checks;
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){Bundle out=new Bundle();try{
        Context c=getTargetContext();store=new DesktopStore(c);store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("reduce_motion",true).commit();XpTheme.defaults(store);
        c.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();c.getSharedPreferences(InitializationState.PREFS,0).edit().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();c.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(c,MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();await(home::hasWindowFocus,"Desktop gains focus");
        for(boolean landscape:new boolean[]{false,true}){
            ui(()->home.setRequestedOrientation(landscape?ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));await(()->landscape?root.getWidth()>root.getHeight():root.getHeight()>root.getWidth(),"Orientation settles");
            startMenu();View notes=find(root,"Open Programs: Notepad");check(notes!=null,"Pinned Notepad visible");hold(notes);check(node("Send To")!=null,"Real hold opens XP shortcut menu");check(node("Remove from This List")!=null,"Start removal is separate from desktop removal");check(node("Uninstall")==null,"Built-in shortcut has no Android uninstall");shot("start-context"+(landscape?"-land":"-port"));
            clickNode("Send To");check(node("Desktop (create shortcut)")!=null,"Send To opens its submenu");clickNode("Desktop (create shortcut)");check(store.pinned("@notes"),"Send To creates the real desktop shortcut");int count=store.desktop().size();
            hold(find(root,"Open Programs: Notepad"));clickNode("Send To");clickNode("Desktop (create shortcut)");check(store.pinned("@notes")&&store.desktop().size()==count,"Repeated Send To keeps existing desktop shortcut");
            hold(find(root,"Open Programs: Notepad"));clickNode("Remove from This List");check(!store.startPins().contains("@notes")&&store.pinned("@notes"),"Start removal keeps desktop shortcut");
            tap(find(root,"All Programs"));clickNode("Accessories");holdNode("Paint");check(node("Send To")!=null,"Nested All Programs row supports native hold");check(node("Delete")!=null,"Program shortcut has XP Delete");clickNode("Pin to Start menu");check(store.startPins().contains("@paint"),"All Programs pin saves to Start");
            hold(find(root,"Open Programs: Paint"));clickNode("Properties");check(node("Paint Properties")!=null,"Properties opens an XP dialog");shot("start-properties"+(landscape?"-land":"-port"));clickNode("OK");
            hold(find(root,"My Computer"));check(node("Explore")!=null&&node("Manage")!=null,"My Computer has its own XP commands");clickNode("Properties");settle();check(find(root,"Property tab: General")!=null,"My Computer Properties routes to real System page");
            startMenu();hold(find(root,"All Programs"));check(node("Sort by Name")!=null,"All Programs folder supports hold");clickNode("Sort by Name");holdNode("Games");check(node("Properties")!=null,"Program group folder has a context menu");back();back();
            ui(()->{XpStartMenus.pin(store,"@notes",true);XpStartMenus.pin(store,"@paint",false);});
        }
        String fixture="com.nthexperience.desktop.fixture/com.nthexperience.desktop.fixture.TaskActivity";ui(()->{store.startPins(Collections.singletonList(fixture));});startMenu();View app=find(root,"Open Programs: Task switch probe");check(app!=null,"Installed test app is pinned");hold(app);check(node("Uninstall")!=null,"Removable Android app exposes uninstall");check(node("Properties")!=null,"Installed app exposes properties");back();check(store.startPins().contains(fixture),"Dismissing uninstall menu preserves the shortcut");
        check(DesktopBackup.parse(DesktopBackup.snapshot(store).toString()).values.containsKey("xp_start_excluded"),"Backup validates Start customization");
        out.putString("result","PASS: "+checks+" XP Start checks; real home/group/program holds, idempotent desktop shortcut, Start-only removal, pinning, properties routing and app uninstall availability in portrait/landscape.");finish(Activity.RESULT_OK,out);
    }catch(Throwable error){android.util.Log.e("XpSmoke","XP Start check failed",error);try{shot("start-failure");}catch(Exception ignored){}out.putString("result","FAIL: "+error);finish(Activity.RESULT_CANCELED,out);}}
    void startMenu()throws Exception{ui(()->{StartMenu start=(StartMenu)findType(root,StartMenu.class);start.show(false);});settle();}
    View findType(View v,Class<?> type){if(type.isInstance(v))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=findType(((ViewGroup)v).getChildAt(i),type);if(found!=null)return found;}return null;}
    void back()throws Exception{getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);settle();}
    void holdNode(String label)throws Exception{AccessibilityNodeInfo item=node(label);if(item==null)throw new AssertionError("Missing row "+label);Rect bounds=new Rect();item.getBoundsInScreen(bounds);check(!bounds.isEmpty(),"Popup row has visible bounds: "+label);long start=SystemClock.uptimeMillis();input(MotionEvent.ACTION_DOWN,bounds.centerX(),bounds.centerY(),start);Thread.sleep(750);input(MotionEvent.ACTION_UP,bounds.centerX(),bounds.centerY(),start);settle();}
    void ui(Runnable action){runOnMainSync(action);}void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;if(checks%8==0){Bundle progress=new Bundle();progress.putString("progress",label);sendStatus(0,progress);}}void settle()throws Exception{waitForIdleSync();Thread.sleep(180);}void await(BooleanSupplier condition,String label)throws Exception{for(int i=0;i<100;i++){boolean[] ok={false};ui(()->ok[0]=condition.getAsBoolean());if(ok[0]){check(true,label);return;}Thread.sleep(100);}throw new AssertionError(label);}
    View find(View v,String description){if(description.equals(v.getContentDescription()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=find(((ViewGroup)v).getChildAt(i),description);if(found!=null)return found;}return null;}
    View findText(View v,String text){if(v instanceof TextView&&text.contentEquals(((TextView)v).getText()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=findText(((ViewGroup)v).getChildAt(i),text);if(found!=null)return found;}return null;}
    DesktopWindow window(View v,String id){if(v instanceof DesktopWindow&&id.equals(((DesktopWindow)v).toolId))return (DesktopWindow)v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){DesktopWindow found=window(((ViewGroup)v).getChildAt(i),id);if(found!=null)return found;}return null;}
    AccessibilityNodeInfo node(String label){return node(getUiAutomation().getRootInActiveWindow(),label);}AccessibilityNodeInfo node(AccessibilityNodeInfo parent,String label){if(parent==null)return null;if(label.contentEquals(parent.getText()==null?"":parent.getText())||label.contentEquals(parent.getContentDescription()==null?"":parent.getContentDescription()))return parent;for(int i=0;i<parent.getChildCount();i++){AccessibilityNodeInfo found=node(parent.getChild(i),label);if(found!=null)return found;}return null;}
    void clickNode(String label)throws Exception{AccessibilityNodeInfo item=node(label);if(item==null||!item.performAction(AccessibilityNodeInfo.ACTION_CLICK))throw new AssertionError("Cannot click "+label);settle();}
    void input(int action,float x,float y,long start){MotionEvent event=MotionEvent.obtain(start,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);getUiAutomation().injectInputEvent(event,true);event.recycle();}
    void tap(View v)throws Exception{int[] p=new int[2];ui(()->v.getLocationOnScreen(p));long start=SystemClock.uptimeMillis();input(MotionEvent.ACTION_DOWN,p[0]+v.getWidth()/2f,p[1]+v.getHeight()/2f,start);input(MotionEvent.ACTION_UP,p[0]+v.getWidth()/2f,p[1]+v.getHeight()/2f,start);settle();}
    void hold(View v)throws Exception{int[] p=new int[2];ui(()->v.getLocationOnScreen(p));long start=SystemClock.uptimeMillis();input(MotionEvent.ACTION_DOWN,p[0]+v.getWidth()/2f,p[1]+v.getHeight()/2f,start);Thread.sleep(750);input(MotionEvent.ACTION_UP,p[0]+v.getWidth()/2f,p[1]+v.getHeight()/2f,start);settle();}
    void drag(View v,float from,float to)throws Exception{int[] p=new int[2];ui(()->v.getLocationOnScreen(p));float x=p[0]+v.getWidth()/2f;long start=SystemClock.uptimeMillis();input(MotionEvent.ACTION_DOWN,x,p[1]+v.getHeight()*from,start);for(int i=1;i<=8;i++){input(MotionEvent.ACTION_MOVE,x,p[1]+v.getHeight()*(from+(to-from)*i/8),start);Thread.sleep(30);}input(MotionEvent.ACTION_UP,x,p[1]+v.getHeight()*to,start);}
    void shot(String name)throws Exception{Bitmap image=getUiAutomation().takeScreenshot();File dir=new File(getTargetContext().getFilesDir(),"xp-verification");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,out);}image.recycle();}
}
