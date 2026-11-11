package com.nthexperience.desktop;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import java.io.*;
import static org.junit.Assert.*;

/** Renders the real launcher Activity with Android native graphics for visual review. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w393dp-h800dp-mdpi",shadows=XpVisualTest.DeniedStorage.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
public class XpVisualTest {
    @Implements(android.os.Environment.class)
    public static class DeniedStorage extends org.robolectric.shadows.ShadowEnvironment {@Implementation(minSdk=30) protected static boolean isExternalStorageManager(){return false;}}
    @Test public void actualXpShellRendersAllSchemes()throws Exception{
        Context context=RuntimeEnvironment.getApplication();DesktopStore store=new DesktopStore(context);
        store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("reduce_motion",true).commit();
        context.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();
        context.getSharedPreferences(InitializationState.PREFS,0).edit().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();
        context.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        File output=new File("build/xp-rendered");output.mkdirs();
        for(int scheme=0;scheme<XpTheme.NAMES.length;scheme++)for(boolean landscape:new boolean[]{false,true}){
            RuntimeEnvironment.setQualifiers(landscape?"w800dp-h393dp-land-mdpi":"w393dp-h800dp-port-mdpi");String suffix=landscape?"-land":"";
            XpTheme.apply(store,scheme);
            try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).create().start().resume().visible()){
                MainActivity activity=controller.get();View root=activity.getWindow().getDecorView();layout(root);
                View button=find(root,XpStartButton.class);assertNotNull(button);button.performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();layout(root);
                StartMenu menu=(StartMenu)find(root,StartMenu.class);assertEquals(View.VISIBLE,menu.getVisibility());assertTrue(menu.getWidth()<=Math.min(420,root.getWidth()));assertTrue(menu.getHeight()>300);
                save(root,new File(output,"start-"+scheme+suffix+".png"));
                menu.hide();activity.open("@computer");layout(root);VistaExplorer explorer=(VistaExplorer)find(root,VistaExplorer.class);assertNotNull(explorer);assertTrue(explorer.content.getChildAt(0).getHeight()>0);save(root,new File(output,"explorer-"+scheme+suffix+".png"));
                activity.open("setting:page:personalization");layout(root);save(root,new File(output,"display-"+scheme+suffix+".png"));
                View desktop=findDescription(root,"Display tab: Desktop");assertNotNull(desktop);desktop.performClick();layout(root);save(root,new File(output,"desktop-"+scheme+suffix+".png"));
                activity.open("@notes");layout(root);assertNotNull(find(root,XpNotepad.class));save(root,new File(output,"notepad-"+scheme+suffix+".png"));
                activity.open("@taskmanager");layout(root);save(root,new File(output,"taskmanager-applications-"+scheme+suffix+".png"));findDescription(root,"Task Manager Performance").performClick();layout(root);save(root,new File(output,"taskmanager-performance-"+scheme+suffix+".png"));
                activity.open("@control");layout(root);((VistaControlPanel)find(root,VistaControlPanel.class)).navigate("home");layout(root);assertTrue(((VistaControlPanel)find(root,VistaControlPanel.class)).back.isShown());assertTrue(((VistaControlPanel)find(root,VistaControlPanel.class)).back.getWidth()>0);save(root,new File(output,"control-panel-"+scheme+suffix+".png"));
                activity.open("@system");layout(root);save(root,new File(output,"system-"+scheme+suffix+".png"));
                activity.open("setting:page:taskbar");layout(root);save(root,new File(output,"taskbar-properties-"+scheme+suffix+".png"));
                activity.open("@recycle");layout(root);save(root,new File(output,"recycle-"+scheme+suffix+".png"));
                activity.open("@organize");layout(root);save(root,new File(output,"app-folders-"+scheme+suffix+".png"));
                activity.open("@clock");layout(root);save(root,new File(output,"date-time-"+scheme+suffix+".png"));
            }
        }
    }
    void layout(View root){int width=root.getResources().getConfiguration().screenWidthDp,height=root.getResources().getConfiguration().screenHeightDp;for(int pass=0;pass<4;pass++){force(root);root.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));root.layout(0,0,width,height);org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();}}
    void force(View v){v.forceLayout();if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)force(((ViewGroup)v).getChildAt(i));}
    View findDescription(View view,String description){if(description.equals(view.getContentDescription()))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View found=findDescription(((ViewGroup)view).getChildAt(i),description);if(found!=null)return found;}return null;}
    View find(View view,Class<?> type){if(type.isInstance(view))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View found=find(((ViewGroup)view).getChildAt(i),type);if(found!=null)return found;}return null;}
    void save(View view,File file)throws Exception{Bitmap image=Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));try(FileOutputStream out=new FileOutputStream(file)){image.compress(Bitmap.CompressFormat.PNG,100,out);}image.recycle();}
}
