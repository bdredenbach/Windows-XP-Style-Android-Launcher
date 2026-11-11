package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.*;
import java.time.Duration;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w393dp-h800dp-mdpi",shadows=XpVisualTest.DeniedStorage.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
public class XpExperiencePolishTest {
    Context c;DesktopStore store;
    @Before public void prepare(){c=RuntimeEnvironment.getApplication();store=new DesktopStore(c);store.prefs.edit().clear().putBoolean("reduce_motion",true).commit();XpTheme.defaults(store);}
    @Test public void notificationWellReachesEveryEdgeWithoutAnInsetLedge(){
        XpTaskbar bar=new XpTaskbar(c);bar.setBackground(XpTheme.chrome(c,AeroGlass.TASKBAR));View tasks=new View(c),tray=new View(c),clock=new View(c);bar.addView(tasks,new LinearLayout.LayoutParams(180,40));bar.addView(tray,new LinearLayout.LayoutParams(70,40));bar.addView(clock,new LinearLayout.LayoutParams(70,40));bar.notificationStartsAt(tray);
        for(int height:new int[]{44,84})for(int scheme=0;scheme<6;scheme++){
            XpTheme.apply(store,scheme);layout(bar,320,height);Bitmap b=draw(bar);
            int top=height-44;assertEquals(b.getPixel(260,top+1),b.getPixel(319,top+1));assertEquals(b.getPixel(260,height-1),b.getPixel(319,height-1));
            assertEquals(b.getPixel(200,top+1),b.getPixel(260,top+1));b.recycle();
        }
    }
    @Test public void captionMotionCanBeInterruptedByRestoreAndCloseWithoutScalingTheWindow(){
        Activity a=Robolectric.buildActivity(Activity.class).setup().get();FrameLayout host=new FrameLayout(a);a.setContentView(host);layout(host,800,600);
        DesktopWindow w=new DesktopWindow(a,host,"Notepad",new View(a),new AeroWallpaper(a,11),()->{},store,"@notes","landscape");w.open();layout(host,800,600);idle(300);store.prefs.edit().putBoolean("reduce_motion",false).commit();w.minimize();assertTrue(w.minimizing());assertTrue(w.motionRunning());assertEquals(1,w.getScaleX(),0);w.restore();idle(400);assertEquals(View.VISIBLE,w.getVisibility());assertFalse(w.minimizing());assertFalse(w.motionRunning());assertEquals(1,w.getScaleY(),0);
        w.maximize();assertTrue(w.maximized());layout(host,800,600);w.restoreDown();layout(host,800,600);idle(400);assertFalse(w.maximized());assertTrue(w.getWidth()<800);w.minimize();w.closeWindow();idle(400);assertNull(w.getParent());assertFalse(w.motionRunning());a.finish();
    }
    @Test public void reducedMotionCompletesMinimizeAndRestoreSynchronously(){
        Activity a=Robolectric.buildActivity(Activity.class).setup().get();FrameLayout host=new FrameLayout(a);a.setContentView(host);layout(host,500,600);DesktopWindow w=new DesktopWindow(a,host,"Test",new View(a),new AeroWallpaper(a,11),()->{},store,"@notes","portrait");w.open();layout(host,500,600);w.minimize();assertEquals(View.GONE,w.getVisibility());assertFalse(w.motionRunning());w.restore();assertEquals(View.VISIBLE,w.getVisibility());assertFalse(w.motionRunning());w.closeWindow();a.finish();
    }
    @Test public void altTabCyclesBothWaysAndCommitsOnlyWhenAltIsReleased(){
        Activity a=Robolectric.buildActivity(Activity.class).setup().get();FrameLayout host=new FrameLayout(a);a.setContentView(host);layout(host,393,800);List<DesktopWindow> windows=new ArrayList<>();
        for(String title:new String[]{"First","Second","Third"}){DesktopWindow w=new DesktopWindow(a,host,title,new View(a),new AeroWallpaper(a,11),()->{},store,"@"+title,"portrait");w.open();windows.add(w);}layout(host,393,800);
        WindowPreviews switcher=new WindowPreviews(a,new WindowPreviews.Host(){public List<DesktopWindow> windows(){return windows;}public List<String> external(){return Collections.emptyList();}public String label(String id){return id;}public void open(String id){}public View wallpaper(){return host;}public void showDesktop(){}public void preparePreview(){}});
        assertTrue(switcher.key(tab(false)));Dialog d=org.robolectric.shadows.ShadowDialog.getLatestDialog();assertEquals("Second",((TextView)find(d.getWindow().getDecorView(),"Selected program")).getText().toString());assertEquals(windows.get(2),host.getChildAt(host.getChildCount()-1));
        assertTrue(switcher.key(tab(false)));assertEquals("First",((TextView)find(d.getWindow().getDecorView(),"Selected program")).getText().toString());switcher.key(tab(true));assertEquals("Second",((TextView)find(d.getWindow().getDecorView(),"Selected program")).getText().toString());
        assertTrue(switcher.key(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ALT_LEFT)));assertFalse(d.isShowing());assertEquals(windows.get(1),host.getChildAt(host.getChildCount()-1));
        switcher.show();d=org.robolectric.shadows.ShadowDialog.getLatestDialog();switcher.key(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ESCAPE));assertFalse(d.isShowing());a.finish();
    }
    @Test public void pcmFeedsDistinctVisualizationsAndFlushRemovesOldAudio()throws Exception{
        MediaPlayback.Signal signal=new MediaPlayback.Signal();signal.flush(48000,2,androidx.media3.common.C.ENCODING_PCM_16BIT);ByteBuffer pcm=ByteBuffer.allocate(2048*4).order(ByteOrder.LITTLE_ENDIAN);for(int i=0;i<2048;i++){short value=(short)(Math.sin(i*2*Math.PI*12/256)*15000);pcm.putShort(value);pcm.putShort(value);}pcm.flip();signal.handleBuffer(pcm);assertEquals(256,signal.samples().length);assertEquals(0,pcm.position());float peak=0;for(float value:signal.spectrum())peak=Math.max(peak,value);assertTrue(peak>.1f);
        File output=new File("build/xp-polish-rendered");output.mkdirs();Set<Integer> hashes=new HashSet<>();XpMusicVisualizer visual=new XpMusicVisualizer(c,signal,()->false);for(int mode=0;mode<4;mode++){visual.mode(mode);layout(visual,480,260);Bitmap b=draw(visual);int[] pixels=new int[480*260];b.getPixels(pixels,0,480,0,0,480,260);hashes.add(Arrays.hashCode(pixels));save(b,new File(output,"visualizer-"+mode+".png"));b.recycle();}assertEquals(4,hashes.size());signal.flush(44100,1,androidx.media3.common.C.ENCODING_PCM_16BIT);for(float value:signal.samples())assertEquals(0,value,0);for(float value:signal.spectrum())assertEquals(0,value,0);
    }
    @Test public void playerAndTallerStartRenderInBothOrientations()throws Exception{
        c.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();store.prefs.edit().putInt("startup_mode",2).commit();c.getSharedPreferences(InitializationState.PREFS,0).edit().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();c.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        File output=new File("build/xp-polish-rendered");output.mkdirs();
        for(boolean land:new boolean[]{false,true}){
            RuntimeEnvironment.setQualifiers(land?"w800dp-h393dp-land-mdpi":"w393dp-h800dp-port-mdpi");int width=land?800:393,height=land?393:800;
            try(org.robolectric.android.controller.ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).create().start().resume().visible()){
                MainActivity a=controller.get();View root=a.getWindow().getDecorView();layout(root,width,height);findType(root,XpStartButton.class).performClick();layout(root,width,height);StartMenu menu=(StartMenu)findType(root,StartMenu.class);if(!land)assertTrue(menu.getHeight()>=590);save(draw(root),new File(output,"start-"+land+".png"));menu.hide();
                a.open(MediaLibrary.PLAYER);layout(root,width,height);MediaPanel panel=(MediaPanel)findType(root,MediaPanel.class);assertNotNull(find(panel,"Visualization: "+XpMusicVisualizer.NAMES[0]));assertNotNull(find(panel,"Stop"));save(draw(root),new File(output,"player-"+land+".png"));panel.visualization(3);assertNotNull(find(panel,"Visualization: Particle Rings"));panel.visualization(0);
            }
        }
    }
    private KeyEvent tab(boolean back){return new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_TAB,0,KeyEvent.META_ALT_ON|(back?KeyEvent.META_SHIFT_ON:0));}
    static void idle(long ms){Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ms));}
    static void layout(View view,int w,int h){XpShellUpgradeTest.layout(view,w,h);}
    static Bitmap draw(View v){Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));return b;}
    static void save(Bitmap b,File file)throws Exception{try(FileOutputStream out=new FileOutputStream(file)){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
    static View find(View view,String name){return XpShellUpgradeTest.find(view,name);}
    static View findType(View view,Class<?> type){if(type.isInstance(view))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View match=findType(((ViewGroup)view).getChildAt(i),type);if(match!=null)return match;}return null;}
}
