package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.AudioManager;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowAlertDialog;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w393dp-h800dp-mdpi",shadows=XpVisualTest.DeniedStorage.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
public class XpTidyTest {
    Context c;DesktopStore store;
    @Before public void setup(){c=RuntimeEnvironment.getApplication();store=new DesktopStore(c);store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("reduce_motion",true).commit();XpTheme.defaults(store);c.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();c.getSharedPreferences(InitializationState.PREFS,0).edit().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();c.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();}
    @Test public void verticalTrackbarKeepsTouchKeyboardAndAccessibilityValues(){
        XpWidgets.Vertical slider=new XpWidgets.Vertical(c);slider.setMax(20);slider.setProgress(10);slider.layout(0,0,48,160);int[] starts={0},ends={0},users={0};slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){starts[0]++;}public void onStopTrackingTouch(SeekBar b){ends[0]++;}public void onProgressChanged(SeekBar b,int n,boolean user){if(user)users[0]++;}});
        touch(slider,MotionEvent.ACTION_DOWN,12);touch(slider,MotionEvent.ACTION_UP,12);assertTrue(slider.getProgress()>=19);touch(slider,MotionEvent.ACTION_DOWN,148);touch(slider,MotionEvent.ACTION_UP,148);assertTrue(slider.getProgress()<=1);assertEquals(2,starts[0]);assertEquals(2,ends[0]);assertTrue(users[0]>=2);
        slider.onKeyDown(KeyEvent.KEYCODE_DPAD_UP,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP));assertTrue(slider.getProgress()>0);Bundle value=new Bundle();value.putFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE,12);assertTrue(slider.performAccessibilityAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId(),value));assertEquals(12,slider.getProgress());slider.setEnabled(false);touch(slider,MotionEvent.ACTION_DOWN,10);assertEquals(12,slider.getProgress());
    }
    @Test public void sharedRadioAndCheckControlsKeepSelectionSemantics(){RadioGroup group=new RadioGroup(c);RadioButton a=XpWidgets.radio(c),b=XpWidgets.radio(c);a.setId(101);b.setId(102);group.addView(a);group.addView(b);a.performClick();b.performClick();assertFalse(a.isChecked());assertTrue(b.isChecked());assertTrue(b.getButtonDrawable() instanceof AeroCheck);CheckBox box=XpWidgets.check(c);box.performClick();assertTrue(box.isChecked());box.performClick();assertFalse(box.isChecked());}
    @Test public void disabledMenusDoNotRunAndSelectedChecksHaveVisibleContrast()throws Exception{
        try(ActivityController<Activity> owner=Robolectric.buildActivity(Activity.class).setup()){
            FrameLayout root=new FrameLayout(owner.get());owner.get().setContentView(root);layout(root,393,800);int[] calls={0};DesktopContextMenu menu=new DesktopContextMenu(owner.get());menu.item("Unavailable","empty",false,()->calls[0]++);menu.checked("Selected",true,()->calls[0]++);menu.show(root,390,790,0,0,0,0);PopupWindow popup=Shadows.shadowOf(RuntimeEnvironment.getApplication()).getLatestPopupWindow();View disabled=find(popup.getContentView(),"Unavailable");disabled.performClick();assertEquals(0,calls[0]);assertTrue(menu.showing());View checked=find(popup.getContentView(),"Selected");checked.setPressed(true);layout(popup.getContentView(),238,68);Bitmap image=draw(popup.getContentView());int white=0;for(int y=36;y<60;y++)for(int x=4;x<29;x++)if((image.getPixel(x,y)&0xffffff)==0xffffff)white++;assertTrue("Selected menu check is white on blue",white>8);image.recycle();checked.performClick();assertEquals(1,calls[0]);assertFalse(menu.showing());
        }
    }
    @Test public void notificationChoicesAreStagedAndPortable()throws Exception{
        try(ActivityController<MainActivity> owner=home()){
            SystemTray tray=tray(owner.get());tray.customize();AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();((CheckBox)find(dialog.getWindow().getDecorView(),"Notification icon: Volume")).setChecked(false);dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();Shadows.shadowOf(Looper.getMainLooper()).idle();assertTrue(store.prefs.getBoolean("tray_volume",true));
            tray.customize();dialog=ShadowAlertDialog.getLatestAlertDialog();((CheckBox)find(dialog.getWindow().getDecorView(),"Notification icon: Volume")).setChecked(false);dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();Shadows.shadowOf(Looper.getMainLooper()).idle();assertFalse(store.prefs.getBoolean("tray_volume",true));assertEquals(Boolean.FALSE,DesktopBackup.parse(DesktopBackup.snapshot(store).toString()).values.get("tray_volume"));tray.customize();dialog=ShadowAlertDialog.getLatestAlertDialog();assertFalse(((CheckBox)find(dialog.getWindow().getDecorView(),"Notification icon: Volume")).isChecked());dialog.dismiss();
        }
    }
    @Test public void volumeMuteRestoreAndBackUseTheRealAudioState()throws Exception{
        try(ActivityController<MainActivity> owner=home()){
            MainActivity a=owner.get();View root=a.getWindow().getDecorView();SystemTray tray=tray(a);AudioManager audio=c.getSystemService(AudioManager.class);audio.setStreamVolume(AudioManager.STREAM_MUSIC,5,0);tray.show("volume");layout(root,393,800);assertTrue(find(root,"Media volume") instanceof XpWidgets.Vertical);find(root,"Mute media").performClick();assertEquals(0,audio.getStreamVolume(AudioManager.STREAM_MUSIC));tray.dismiss();tray.show("volume");layout(root,393,800);find(root,"Mute media").performClick();assertEquals(5,audio.getStreamVolume(AudioManager.STREAM_MUSIC));a.onBackPressed();assertFalse(tray.showing());
        }
    }
    @Test public void trayArtworkChangesForRealStatusVariants(){Set<Integer> hashes=new HashSet<>();for(int state=0;state<4;state++){Bitmap image=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.scale(2,2);XpTrayArtwork.draw(canvas,state<2?1:2,50,false,state==0,state==3);int[] pixels=new int[1024];image.getPixels(pixels,0,32,0,0,32,32);hashes.add(Arrays.hashCode(pixels));image.recycle();}assertEquals(4,hashes.size());}
    @Test public void trayPagesAndMenusRenderAllThemesInsideBothWorkAreas()throws Exception{
        File output=new File("build/xp-tidy-rendered");output.mkdirs();
        for(int scheme=0;scheme<6;scheme++)for(boolean landscape:new boolean[]{false,true}){
            RuntimeEnvironment.setQualifiers(landscape?"w800dp-h393dp-land-mdpi":"w393dp-h800dp-port-mdpi");int w=landscape?800:393,h=landscape?393:800;XpTheme.apply(store,scheme);
            try(ActivityController<MainActivity> owner=home()){
                MainActivity a=owner.get();View root=a.getWindow().getDecorView();SystemTray tray=tray(a);layout(root,w,h);
                for(String page:new String[]{"network","volume","battery","brightness","controls"}){tray.show(page);layout(root,w,h);View frame=(View)((ViewGroup)find(root,"System tray flyout")).getChildAt(0);assertTrue(page,frame.getWidth()<=w);assertTrue(page,frame.getBottom()<=frame.getRootView().getHeight());assertTrue(page,frame.getTop()>=0);save(draw(root),new File(output,page+"-"+scheme+(landscape?"-land":"")+".png"));tray.dismiss();}
                View speaker=find(root,"Volume control");speaker.performLongClick();PopupWindow popup=Shadows.shadowOf(RuntimeEnvironment.getApplication()).getLatestPopupWindow();assertNotNull(find(popup.getContentView(),"Open Volume Control"));layout(popup.getContentView(),popup.getWidth(),popup.getHeight());save(draw(popup.getContentView()),new File(output,"volume-menu-"+scheme+(landscape?"-land":"")+".png"));popup.dismiss();
                tray.customize();AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();layout(dialog.getWindow().getDecorView(),Math.min(w,360),Math.min(h,400));save(draw(dialog.getWindow().getDecorView()),new File(output,"notifications-"+scheme+(landscape?"-land":"")+".png"));dialog.dismiss();
            }
        }
    }
    static void touch(View view,int action,float y){MotionEvent event=MotionEvent.obtain(0,10,action,24,y,0);view.onTouchEvent(event);event.recycle();}
    ActivityController<MainActivity> home(){ActivityController<MainActivity> owner=Robolectric.buildActivity(MainActivity.class).create().start().resume().visible();layout(owner.get().getWindow().getDecorView(),c.getResources().getConfiguration().screenWidthDp,c.getResources().getConfiguration().screenHeightDp);return owner;}
    static SystemTray tray(MainActivity a)throws Exception{java.lang.reflect.Field field=MainActivity.class.getDeclaredField("tray");field.setAccessible(true);return (SystemTray)field.get(a);}
    static View find(View root,String name){return XpExperiencePolishTest.find(root,name);}static void layout(View root,int w,int h){XpShellUpgradeTest.layout(root,w,h);}static Bitmap draw(View root){return XpExperiencePolishTest.draw(root);}static void save(Bitmap b,File file)throws Exception{XpExperiencePolishTest.save(b,file);b.recycle();}
}
