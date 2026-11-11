package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.time.Duration;
import java.util.*;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w393dp-h800dp-mdpi",shadows=XpVisualTest.DeniedStorage.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
public class XpShellUpgradeTest {
    Context c;DesktopStore store;
    @Before public void before(){c=RuntimeEnvironment.getApplication();store=new DesktopStore(c);store.prefs.edit().clear().putBoolean("reduce_motion",true).commit();XpTheme.defaults(store);}
    @Test public void hdAssetsRetainAllIdsAndChoosePortraitsWithBoundedDecodes()throws Exception{
        for(int id=11;id<31;id++){
            Bitmap image=XpWallpapers.image(c,id,false,393,800),thumb=XpWallpapers.image(c,id,true,800,393);assertNotNull(image);assertNotNull(thumb);assertTrue(image.getAllocationByteCount()<=12*1024*1024);assertTrue(thumb.getWidth()<=224);assertTrue(thumb.getHeight()<=224);
            store.prefs.edit().putInt("wallpaper",id).commit();assertEquals(id,DesktopBackup.parse(DesktopBackup.snapshot(store).toString()).values.get("wallpaper"));
        }
        Bitmap portrait=XpWallpapers.image(c,11,false,393,800),wide=XpWallpapers.image(c,11,false,800,393);assertTrue(portrait.getHeight()>portrait.getWidth());assertTrue(wide.getWidth()>wide.getHeight());assertTrue(wide.getWidth()>=1920);assertNotSame(portrait,wide);
        assertEquals(XpWallpapers.asset(15,false),XpWallpapers.asset(15,true));
        assertEquals(2,XpWallpapers.sample(3840,2160,false));assertTrue(XpWallpapers.sample(3840,2160,true)>=16);
        for(String removed:new String[]{"wallpapers/aero-aurora.webp","wallpapers/aero-aurora-landscape.webp","fonts/selawk.ttf","fonts/selawksb.ttf"})try(InputStream ignored=c.getAssets().open(removed)){fail("Obsolete packaged asset: "+removed);}catch(IOException expected){}
    }
    @Test public void taskbarOptionsStayStagedAndRoundTripInBackups()throws Exception{
        String[] keys={XpTaskbarAutoHide.KEY,XpTaskbarAutoHide.LOCK,"tray_language","tray_removable","tray_balloons"};String[] labels={"Auto-hide the taskbar","Lock the taskbar","Show language indicator","Show removable storage when connected","Show low-battery balloons"};
        List<String> events=new ArrayList<>();VistaShell.Actions actions=new VistaShell.Actions(){public void open(String id){}public void action(String id){events.add(id);}};
        XpTaskbarProperties panel=new XpTaskbarProperties(c,store,actions,null,()->{});
        for(int i=0;i<keys.length;i++)((CheckBox)find(panel,labels[i])).setChecked(!store.prefs.getBoolean(keys[i],i>=3));
        find(panel,"Property tab: Start Menu").performClick();JSONObject saved=new JSONObject(panel.state().toString());find(panel,"Cancel").performClick();assertFalse(store.prefs.getBoolean(XpTaskbarAutoHide.KEY,false));assertTrue(events.isEmpty());
        panel=new XpTaskbarProperties(c,store,actions,saved,()->{});find(panel,"Apply").performClick();assertTrue(store.prefs.getBoolean(XpTaskbarAutoHide.KEY,false));assertTrue(store.prefs.getBoolean(XpTaskbarAutoHide.LOCK,false));assertTrue(store.prefs.getBoolean("tray_language",false));
        DesktopBackup.Plan backup=DesktopBackup.parse(DesktopBackup.snapshot(store).toString());for(String key:keys)assertEquals(store.prefs.getBoolean(key,false),backup.values.get(key));assertEquals(Collections.singletonList("taskbar-refresh"),events);
    }
    @Test public void autoHideOverlaysWithoutMovingWorkAreaOrChangingEitherRow()throws Exception{
        FrameLayout root=new FrameLayout(c);LinearLayout shell=Aero.column(c);root.addView(shell,new FrameLayout.LayoutParams(-1,-1));FrameLayout work=new FrameLayout(c);shell.addView(work,new LinearLayout.LayoutParams(-1,0,1));View window=new View(c);work.addView(window,new FrameLayout.LayoutParams(180,220));View slot=new View(c);shell.addView(slot,new LinearLayout.LayoutParams(-1,80));
        TaskbarItems rows=new TaskbarItems(c,true);rows.bottomBounds(90,320,393);Map<String,Integer> mapping=new HashMap<>();for(int i=0;i<12;i++){TextView item=Aero.text(c,"App "+i,12,Color.BLACK);item.setTag(R.id.taskbar_order_key,"app"+i);item.setLayoutParams(new LinearLayout.LayoutParams(110,36));TaskbarItems.slot(item,true);rows.addView(item);mapping.put("app"+i,i%2);}rows.assignRows(mapping);root.addView(rows,new FrameLayout.LayoutParams(-1,80,Gravity.BOTTOM));boolean[] busy={false};XpTaskbarAutoHide control=new XpTaskbarAutoHide(root,rows,slot,()->busy[0]);control.configure(true,80);layout(root,393,800);rows.scrollRowTo(0,100);rows.scrollRowTo(1,70);int[] positions=rows.scrollPositions();int height=work.getHeight(),top=window.getTop();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2));assertFalse(control.shown());assertEquals(View.INVISIBLE,rows.getVisibility());assertNotNull(find(root,"Reveal taskbar"));layout(root,393,800);assertEquals(height,work.getHeight());assertEquals(top,window.getTop());assertArrayEquals(positions,rows.scrollPositions());
        find(root,"Reveal taskbar").performClick();layout(root,393,800);assertTrue(control.shown());assertEquals(height,work.getHeight());assertArrayEquals(positions,rows.scrollPositions());busy[0]=true;Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3));assertTrue(control.shown());busy[0]=false;Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1));assertFalse(control.shown());control.configure(false,80);assertTrue(control.shown());assertEquals(80,slot.getLayoutParams().height);control.close();
    }
    @Test public void lockBlocksTaskAndRowReorderingWithoutDisablingLaunches(){
        TaskbarItems rows=new TaskbarItems(c,true);HorizontalScrollView scroll=new HorizontalScrollView(c);scroll.addView(rows);TextView first=Aero.button(c,"First",()->store.prefs.edit().putBoolean("launched",true).apply());first.setTag(R.id.taskbar_order_key,"first");first.setLayoutParams(new LinearLayout.LayoutParams(100,36));TaskbarItems.slot(first,true);rows.addView(first);TextView second=Aero.text(c,"Second",12,Color.BLACK);second.setTag(R.id.taskbar_order_key,"second");second.setLayoutParams(new LinearLayout.LayoutParams(100,36));TaskbarItems.slot(second,true);rows.addView(second);TaskbarReorder reorder=new TaskbarReorder(rows,scroll,order->{});
        store.prefs.edit().putBoolean(XpTaskbarAutoHide.LOCK,true).commit();assertFalse(reorder.begin(first));assertFalse(reorder.move("first",1));assertFalse(reorder.moveRow("first",1));first.performClick();assertTrue(store.prefs.getBoolean("launched",false));store.prefs.edit().putBoolean(XpTaskbarAutoHide.LOCK,false).commit();assertTrue(reorder.moveRow("first",1));reorder.close();
    }
    @Test public void captionStatesAndPropertyHelpRenderDifferently(){
        WindowButton button=new WindowButton(c,"×",()->{});int normal=pixel(button);button.active(false);int inactive=pixel(button);button.setEnabled(false);int disabled=pixel(button);assertNotEquals(normal,inactive);assertNotEquals(inactive,disabled);
        Activity activity=Robolectric.buildActivity(Activity.class).setup().get();FrameLayout host=new FrameLayout(activity);activity.setContentView(host);DesktopWindow window=new DesktopWindow(activity,host,"Display Properties",new View(activity),new AeroWallpaper(activity,11),()->{},store,"@appearance","portrait");host.addView(window);View help=find(window,"Help for properties");assertEquals(View.VISIBLE,help.getVisibility());help.performClick();assertNotNull(find(org.robolectric.shadows.ShadowDialog.getLatestDialog().getWindow().getDecorView(),"XP dialog caption"));org.robolectric.shadows.ShadowDialog.getLatestDialog().dismiss();activity.finish();
    }
    @Test public void sessionPanelsAndActualLauncherProduceVisualEvidence()throws Exception{
        c.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();store.prefs.edit().putInt("startup_mode",2).commit();c.getSharedPreferences(InitializationState.PREFS,0).edit().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();c.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        File output=new File("build/xp-upgrade-rendered");output.mkdirs();
        for(boolean landscape:new boolean[]{false,true}){
            RuntimeEnvironment.setQualifiers(landscape?"w800dp-h393dp-land-mdpi":"w393dp-h800dp-port-mdpi");int width=landscape?800:393,height=landscape?393:800;String suffix=landscape?"-land":"-port";
            try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).create().start().resume().visible()){
                MainActivity activity=controller.get();View root=activity.getWindow().getDecorView();layout(root,width,height);activity.open("@computer");layout(root,width,height);
                java.lang.reflect.Method show=MainActivity.class.getDeclaredMethod("showXpSession",String.class);show.setAccessible(true);
                for(String mode:new String[]{"power","logoff","welcome"}){show.invoke(activity,mode);Dialog dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();View content=dialog.getWindow().getDecorView();layout(content,Math.min(width-20,430),Math.min(height-48,330));assertNotNull(find(content,mode.equals("power")?"XP Turn Off panel":mode.equals("logoff")?"XP Log Off panel":"XP Welcome screen"));save(content,new File(output,mode+suffix+".png"));if(mode.equals("logoff")){find(content,"Log Off: Return to Welcome").performClick();Dialog welcome=org.robolectric.shadows.ShadowDialog.getLatestDialog();assertNotNull(find(welcome.getWindow().getDecorView(),"Return to desktop"));find(welcome.getWindow().getDecorView(),"Return to desktop").performClick();}else dialog.dismiss();}
                activity.open("setting:page:taskbar");layout(root,width,height);assertNotNull(find(root,"Auto-hide the taskbar"));save(root,new File(output,"properties"+suffix+".png"));
            }
        }
    }
    private int pixel(WindowButton v){v.layout(0,0,32,32);Bitmap b=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));int p=b.getPixel(16,5);b.recycle();return p;}
    static View find(View view,String name){if(name.equals(view.getContentDescription())||view instanceof TextView&&name.contentEquals(((TextView)view).getText()))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View match=find(((ViewGroup)view).getChildAt(i),name);if(match!=null)return match;}return null;}
    static void layout(View view,int width,int height){for(int i=0;i<4;i++){force(view);view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));view.layout(0,0,width,height);Shadows.shadowOf(Looper.getMainLooper()).idle();}}
    private static void force(View view){view.forceLayout();if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)force(((ViewGroup)view).getChildAt(i));}
    private static void save(View view,File file)throws Exception{Bitmap b=Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));try(FileOutputStream out=new FileOutputStream(file)){b.compress(Bitmap.CompressFormat.PNG,100,out);}b.recycle();}
}
