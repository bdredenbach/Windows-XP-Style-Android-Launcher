package com.nthexperience.desktop;

import android.content.*;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w393dp-h800dp-mdpi")
@LooperMode(LooperMode.Mode.PAUSED)
public class XpFidelityTest {
    Context context;DesktopStore store;
    @Before public void before(){context=RuntimeEnvironment.getApplication();store=new DesktopStore(context);store.prefs.edit().clear().commit();}
    @Test public void wallpapersDecodeAndSurviveBackupWithoutRenumberingLegacyChoices()throws Exception{
        XpTheme.defaults(store);assertEquals(XpWallpapers.FIRST,store.prefs.getInt("wallpaper",-1));assertEquals("Nth Hills",DesktopBackgrounds.NAMES[3]);
        for(int id=XpWallpapers.FIRST;id<DesktopBackgrounds.NAMES.length;id++){
            Bitmap full=XpWallpapers.image(context,id,false),thumb=XpWallpapers.image(context,id,true);assertNotNull(DesktopBackgrounds.NAMES[id],full);assertNotNull(thumb);assertTrue(full.getWidth()>=800);assertTrue(thumb.getWidth()<=235);
            store.prefs.edit().putInt("wallpaper",id).commit();assertEquals(id,DesktopBackup.parse(DesktopBackup.snapshot(store).toString()).values.get("wallpaper"));
        }
        store.prefs.edit().clear().putInt("wallpaper",3).commit();XpTheme.defaults(store);assertEquals(3,store.prefs.getInt("wallpaper",-1));
    }
    @Test public void themeDraftSurvivesTabsButCancelNeverCommits(){
        XpTheme.defaults(store);JSONObject draft=new JSONObject();boolean[] closed={false};List<String> actions=new ArrayList<>();
        XpDisplayProperties panel=panel(draft,actions,closed);
        ((Spinner)find(panel,"XP theme")).setSelection(2);idle();assertEquals(0,XpTheme.scheme(context));
        find(panel,"Display tab: Appearance").performClick();assertEquals(2,((Spinner)find(panel,"XP color scheme")).getSelectedItemPosition());
        find(panel,"Cancel").performClick();assertTrue(closed[0]);assertEquals(0,XpTheme.scheme(context));assertEquals(0,draft.length());assertTrue(actions.isEmpty());
    }
    @Test public void wallpaperApplyAndRecreatedDraftUseSameSelection()throws Exception{
        XpTheme.defaults(store);JSONObject draft=new JSONObject();List<String> actions=new ArrayList<>();boolean[] closed={false};XpDisplayProperties panel=panel(draft,actions,closed);
        find(panel,"Display tab: Desktop").performClick();ListView list=(ListView)find(panel,"Desktop background list");list.performItemClick(null,3,list.getAdapter().getItemId(3));
        assertEquals(XpWallpapers.FIRST,store.prefs.getInt("wallpaper",-1));panel.dispose();
        panel=panel(new JSONObject(draft.toString()),actions,closed);assertEquals("Desktop",panel.selectedTab());find(panel,"Apply").performClick();
        assertEquals(XpWallpapers.FIRST+3,store.prefs.getInt("wallpaper",-1));assertEquals(Collections.singletonList("wallpaper"),actions);assertFalse(closed[0]);
    }
    @Test public void systemOnlyDreamBindingAndMatchingPreviewAreAvailable()throws Exception{
        ServiceInfo info=context.getPackageManager().getServiceInfo(new ComponentName(context,XpDreamService.class),0);
        assertTrue(info.exported);assertEquals("android.permission.BIND_DREAM_SERVICE",info.permission);
        XpDisplayProperties panel=panel(new JSONObject(),new ArrayList<>(),new boolean[1]);find(panel,"Display tab: Screen Saver").performClick();assertNotNull(find(panel,"Nth Desktop XP screen saver preview"));
    }
    private XpDisplayProperties panel(JSONObject draft,List<String> log,boolean[] closed){return new XpDisplayProperties(context,store,draft,"Themes",new VistaShell.Actions(){public void open(String id){log.add(id);}public void action(String id){log.add(id);}},()->closed[0]=true);}
    private void idle(){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();}
    private View find(View view,String description){if(description.equals(view.getContentDescription()))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View found=find(((ViewGroup)view).getChildAt(i),description);if(found!=null)return found;}return null;}
}
