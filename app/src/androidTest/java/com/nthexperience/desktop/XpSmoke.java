package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;

/** Native XP identity, app actions, themes, rotation, row anchoring and backup checks. */
public final class XpSmoke extends Instrumentation {
    MainActivity home;DesktopStore store;View root;int checks;
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);start();}
    @Override public void onStart(){Bundle out=new Bundle();try{
        Context context=getTargetContext();store=new DesktopStore(context);store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("reduce_motion",true).commit();
        context.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();
        context.getSharedPreferences(InitializationState.PREFS,0).edit().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();
        context.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        launch();check(context.getPackageName().equals("com.nthexperience.desktop.xp"),"Unique XP install identity");
        check(context.getString(R.string.app_name).equals("Nth Desktop XP"),"XP display name");
        check(store.gadgets().isEmpty(),"Fresh desktop has no Seven gadgets");check(store.prefs.getBoolean("taskbar_show_labels",false),"XP labels default on");
        ui(()->find(root,v->v instanceof XpStartButton).performClick());settle();
        check(find(root,v->"My Computer".equals(v.getContentDescription()))!=null,"XP places visible");
        check(find(root,v->"All Programs".equals(v.getContentDescription()))!=null,"All Programs reachable");shot("01-start-blue");
        View app=find(root,v->"Open Programs: Notepad".equals(v.getContentDescription()));check(app!=null,"Pinned Notepad exists");tap(app);settle();
        check(window("@notes")!=null,"Real program row tap opens Notepad");
        ui(()->home.open("@computer"));settle();check(find(root,v->"Navigation: My Documents".equals(v.getContentDescription()))!=null,"Explorer XP tasks reachable");shot("02-explorer-blue");
        ui(()->home.open("setting:page:personalization"));settle();check(find(root,v->"Apply theme: Silver".equals(v.getContentDescription()))!=null,"Display Properties offers XP schemes");shot("03-display-properties");
        // A real hold invokes the options route retained from Seven's Start touch fix.
        ui(()->find(root,v->v instanceof XpStartButton).performClick());settle();app=find(root,v->"Open Programs: Notepad".equals(v.getContentDescription()));
        hold(app);settle();getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);settle();
        ui(()->{StartMenu menu=(StartMenu)find(root,v->v instanceof StartMenu);menu.hide();});
        for(int scheme=0;scheme<XpTheme.NAMES.length;scheme++){
            final int selected=scheme;ui(()->{home.finish();XpTheme.apply(store,selected);});settle();launch();
            ui(()->home.open("@notes"));settle();DesktopWindow notes=window("@notes");check(notes!=null,"Notepad opens under "+XpTheme.NAMES[scheme]);
            ui(()->find(root,v->v instanceof XpStartButton).performClick());settle();shot("theme-"+scheme+"-portrait");
            ui(()->home.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));Thread.sleep(1200);settle();root=home.getWindow().getDecorView();
            View start=find(root,v->v instanceof XpStartButton);check(start!=null&&start.getWidth()>0,"Start reachable in landscape");shot("theme-"+scheme+"-landscape");
            ui(()->home.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));Thread.sleep(1000);settle();
        }
        ui(()->{((StartMenu)find(root,v->v instanceof StartMenu)).hide();store.prefs.edit().putBoolean("taskbar_expanded",true).commit();invoke("applyTaskbarRows");invoke("renderTaskbar");});settle();
        ViewGroup bar=(ViewGroup)find(root,v->"Desktop taskbar".equals(v.getContentDescription()));View start=find(root,v->v instanceof XpStartButton);check(start.getBottom()>bar.getHeight()/2,"Expanded Start remains on bottom row");shot("04-expanded-taskbar");
        DesktopBackup.Plan backup=DesktopBackup.parse(DesktopBackup.snapshot(store).toString());check(((Integer)backup.values.get(XpTheme.KEY))==5,"Backup retains Classic scheme");
        out.putString("result","PASS: "+checks+" XP checks, six themes in portrait and landscape");finish(Activity.RESULT_OK,out);
    }catch(Throwable failure){out.putString("result","FAIL: "+failure);android.util.Log.e("XpSmoke","Native XP check failed",failure);finish(Activity.RESULT_CANCELED,out);}}
    void launch()throws Exception{home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();for(int i=0;i<100&&!home.hasWindowFocus();i++)Thread.sleep(100);settle();}
    void ui(Runnable action){runOnMainSync(action);}
    void settle()throws Exception{waitForIdleSync();Thread.sleep(350);}
    void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    View find(View view,Predicate<View> match){if(match.test(view))return view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){View found=find(group.getChildAt(i),match);if(found!=null)return found;}}return null;}
    DesktopWindow window(String id){return (DesktopWindow)find(root,v->v instanceof DesktopWindow&&((DesktopWindow)v).toolId.equals(id));}
    void tap(View view)throws Exception{check(view!=null,"Tap target exists");int[] at=new int[2];view.getLocationOnScreen(at);long now=SystemClock.uptimeMillis();for(int action:new int[]{MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP}){MotionEvent event=MotionEvent.obtain(now,SystemClock.uptimeMillis(),action,at[0]+view.getWidth()/2f,at[1]+view.getHeight()/2f,0);getUiAutomation().injectInputEvent(event,true);event.recycle();}}
    void hold(View view)throws Exception{check(view!=null,"Hold target exists");int[] at=new int[2];view.getLocationOnScreen(at);long now=SystemClock.uptimeMillis();MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,at[0]+view.getWidth()/2f,at[1]+view.getHeight()/2f,0);getUiAutomation().injectInputEvent(down,true);Thread.sleep(850);MotionEvent up=MotionEvent.obtain(now,SystemClock.uptimeMillis(),MotionEvent.ACTION_UP,down.getX(),down.getY(),0);getUiAutomation().injectInputEvent(up,true);down.recycle();up.recycle();}
    void invoke(String name){try{java.lang.reflect.Method method=MainActivity.class.getDeclaredMethod(name);method.setAccessible(true);method.invoke(home);}catch(Exception failure){throw new RuntimeException(failure);}}
    void shot(String name)throws Exception{Bitmap bitmap=getUiAutomation().takeScreenshot();check(bitmap!=null,"Screenshot captured: "+name);File dir=new File(getTargetContext().getExternalFilesDir(null),"xp-verification");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();}
}
