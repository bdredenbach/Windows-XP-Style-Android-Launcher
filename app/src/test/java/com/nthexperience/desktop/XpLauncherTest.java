package com.nthexperience.desktop;

import android.content.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="mdpi")
public class XpLauncherTest {
    Context context;DesktopStore store;
    @Before public void setup(){context=RuntimeEnvironment.getApplication();store=new DesktopStore(context);store.prefs.edit().clear().commit();}
    @Test public void firstXpLaunchKeepsRestoredChoices(){store.prefs.edit().putInt(XpTheme.KEY,4).putBoolean("taskbar_show_labels",false).putString("gadgets","[\"clock\"]").commit();XpTheme.defaults(store);assertEquals(4,XpTheme.scheme(context));assertFalse(store.prefs.getBoolean("taskbar_show_labels",true));assertEquals(Collections.singletonList("clock"),store.gadgets());}
    @Test public void themeBackupRoundTripAndInvalidScheme()throws Exception{XpTheme.defaults(store);XpTheme.apply(store,2);DesktopBackup.Plan plan=DesktopBackup.parse(DesktopBackup.snapshot(store).toString());assertEquals(2,plan.values.get(XpTheme.KEY));JSONObject backup=DesktopBackup.snapshot(store);backup.getJSONObject("settings").getJSONObject(XpTheme.KEY).put("value",99);try{DesktopBackup.parse(backup.toString());fail("Invalid XP scheme must fail validation");}catch(JSONException expected){}}
    @Test public void savedThemesIncludeXpSchemeAndOlderThemesHaveBlueFallback()throws Exception{XpTheme.apply(store,4);JSONObject profile=ThemeProfiles.capture(store,"Zune test");XpTheme.apply(store,1);ThemeProfiles.apply(store,profile);assertEquals(4,XpTheme.scheme(context));profile.remove(XpTheme.KEY);ThemeProfiles.apply(store,profile);assertEquals(0,XpTheme.scheme(context));}
    @Test public void menuFitsNarrowPortraitAndKeepsTapAndHoldOptions(){
        XpTheme.defaults(store);final String[] opened={""},held={""};
        StartMenu menu=new StartMenu(context,new StartMenu.Actions(){public void open(String id){opened[0]=id;}public void options(View v,String id){held[0]=id;}public void home(){}public void settings(){}public boolean isHome(){return true;}},store,new AeroWallpaper(context,3,false));
        menu.entries(Collections.emptyList());menu.measure(View.MeasureSpec.makeMeasureSpec(320,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(540,View.MeasureSpec.EXACTLY));menu.layout(0,0,320,540);
        View row=find(menu,"Open Programs: Notepad");assertNotNull(row);row.performClick();assertEquals("@notes",opened[0]);row.performLongClick();assertEquals("@notes",held[0]);
        View run=find(menu,"Run…");assertNotNull(run);assertTrue(run.getMeasuredWidth()>100);assertTrue(run.getMeasuredHeight()>=40);
    }
    private View find(View v,String description){if(description.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=find(((ViewGroup)v).getChildAt(i),description);if(found!=null)return found;}return null;}
}
