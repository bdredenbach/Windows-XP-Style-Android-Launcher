package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import java.util.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35,qualifiers="mdpi")
public class XpStartContextTest {
    static final String APP="example.startapp/.Main";
    ActivityController<Activity> controller;Activity activity;DesktopStore store;FrameLayout root;StartMenu start;AeroWallpaper wallpaper;
    String opened,renamed,selectedSource;
    ShortcutMenus.Host host=new ShortcutMenus.Host(){public void open(String id){opened=id;}public void rename(String id){renamed=id;}public void changed(){start.refresh();}};
    @Before public void setup(){
        controller=Robolectric.buildActivity(Activity.class).setup().visible();activity=controller.get();store=new DesktopStore(activity);store.prefs.edit().clear().commit();ExplorerClipboard.clear();
        ApplicationInfo app=new ApplicationInfo();app.packageName="example.startapp";app.nonLocalizedLabel="Test app";PackageInfo info=new PackageInfo();info.packageName=app.packageName;info.applicationInfo=app;info.versionName="1.2";shadowOf(activity.getPackageManager()).installPackage(info);
        root=new FrameLayout(activity);activity.setContentView(root);wallpaper=new AeroWallpaper(activity,0,false);
        start=new StartMenu(activity,new StartMenu.Actions(){public void open(String id){opened=id;}public void options(View v,String id){options(v,id,XpStartMenus.HOME);}public void options(View v,String id,String source){selectedSource=source;ContextMenus.centered(v,(x,y)->XpStartMenus.show(activity,start,x,y,store,id,source,id,host));}public void home(){}public void settings(){}public boolean isHome(){return true;}},store,wallpaper);
        root.addView(start,new FrameLayout.LayoutParams(390,620));start.entries(Collections.singletonList(new AppCatalog.Entry(APP,"Test app",new ColorDrawable())));layout();start.show(false);
    }
    void layout(){shadowOf(android.os.Looper.getMainLooper()).idle();root.measure(View.MeasureSpec.makeMeasureSpec(420,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(740,View.MeasureSpec.EXACTLY));root.layout(0,0,420,740);}
    @After public void close(){PopupWindow p=shadowOf(RuntimeEnvironment.getApplication()).getLatestPopupWindow();if(p!=null)p.dismiss();start.hide();wallpaper.close();ExplorerClipboard.clear();controller.pause().stop().destroy();}
    View find(View v,String text){if(text.contentEquals(v.getContentDescription()==null?"":v.getContentDescription())||v instanceof TextView&&text.contentEquals(((TextView)v).getText()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View child=find(((ViewGroup)v).getChildAt(i),text);if(child!=null)return child;}return null;}
    PopupWindow popup(){PopupWindow p=shadowOf(RuntimeEnvironment.getApplication()).getLatestPopupWindow();assertNotNull(p);assertTrue(p.isShowing());return p;}
    View command(String text){View row=find(popup().getContentView(),text);assertNotNull("Missing command "+text,row);return row;}
    void click(String text){assertTrue(command(text).performClick());layout();}
    void menu(String id,String source){XpStartMenus.show(activity,start,30,120,store,id,source,id,host);}
    @Test public void sendToDesktopIsIdempotentAndNeverRemovesAnExistingShortcut(){
        menu("@notes",XpStartMenus.HOME);click("Send To");click("Desktop (create shortcut)");assertTrue(store.pinned("@notes"));
        int size=store.desktop().size();menu("@notes",XpStartMenus.HOME);click("Send To");click("Desktop (create shortcut)");assertTrue(store.pinned("@notes"));assertEquals(size,store.desktop().size());
    }
    @Test public void homeRemovalKeepsDesktopAndInstalledAppAndDoesNotImmediatelyRefillTheRow(){
        store.desktop(Arrays.asList(APP,"@notes"));store.startPins(Collections.singletonList(APP));start.show(false);menu(APP,XpStartMenus.HOME);click("Remove from This List");
        assertFalse(store.startPins().contains(APP));assertTrue(store.pinned(APP));assertTrue(store.startExcluded().contains(APP));assertNull(find(start,"Open Programs: Test app"));assertTrue(AppUninstaller.available(activity,APP));
        menu(APP,XpStartMenus.PROGRAMS);click("Pin to Start menu");assertTrue(store.startPins().contains(APP));assertFalse(store.startExcluded().contains(APP));layout();assertNotNull(find(start,"Open Programs: Test app"));
    }
    @Test public void unpinAndRemoveHaveDifferentSemantics(){
        store.startPins(Collections.singletonList(APP));store.launched(APP);menu(APP,XpStartMenus.HOME);click("Unpin from Start menu");assertFalse(store.startPins().contains(APP));assertFalse(store.startExcluded().contains(APP));assertTrue(store.recents().contains(APP));
        menu("@browser",XpStartMenus.HOME);assertNull(find(popup().getContentView(),"Cut"));click("Remove from This List");assertNull(find(start,"Open Programs: "+BrowserPanel.NAME));
    }
    @Test public void allProgramsAndNestedAccessoriesHaveRealHoldHandlersWithoutLaunching(){
        find(start,"All Programs").performClick();assertTrue(command("Test app").performContextClick());assertEquals(XpStartMenus.PROGRAMS,selectedSource);assertNull(opened);assertNotNull(command("Send To"));popup().dismiss();
        find(start,"All Programs").performClick();click("Accessories");assertTrue(command("Notepad").performLongClick());assertEquals(XpStartMenus.PROGRAMS,selectedSource);assertNull(opened);click("Rename…");assertEquals("@notes",renamed);
    }
    @Test public void programDeleteRequiresConfirmationAndRestoreKeepsOriginalDesktopFolder(){
        String folder=store.createFolder("My apps",Collections.singletonList(APP));menu(folder,XpStartMenus.PROGRAMS);click("Delete");AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();assertNotNull(dialog);dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();layout();assertTrue(store.startDeleted().isEmpty());
        menu(folder,XpStartMenus.PROGRAMS);click("Delete");ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick();layout();assertTrue(store.isFolder(folder));assertTrue(store.pinned(folder));assertTrue(store.startDeleted().contains(folder));
        assertTrue(find(start,"All Programs").performLongClick());click("Restore deleted shortcuts…");ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick();layout();assertFalse(store.startDeleted().contains(folder));assertNotNull(command("My apps"));assertEquals(Collections.singletonList(APP),store.folderItems(folder));
    }
    @Test public void copyAndPinnedCutUseShortcutClipboardAndOnlyMoveTheSelectedStartPin(){
        store.startPins(Arrays.asList(APP,"@notes"));menu(APP,XpStartMenus.HOME);click("Copy");ExplorerClipboard.paste(store,"desktop");assertTrue(store.pinned(APP));assertTrue(store.startPins().contains(APP));
        menu(APP,XpStartMenus.HOME);click("Cut");String folder=store.createFolder("Tools",Collections.emptyList());ExplorerClipboard.paste(store,"contents:"+folder);assertFalse(store.startPins().contains(APP));assertTrue(store.startPins().contains("@notes"));assertTrue(store.pinned(APP));assertEquals(Collections.singletonList(APP),store.folderItems(folder));
    }
    @Test public void allProgramsCutMovesOnlyTheStartShortcutAfterSuccessfulPaste(){
        menu(APP,XpStartMenus.PROGRAMS);click("Cut");assertFalse(store.startDeleted().contains(APP));ExplorerClipboard.paste(store,"desktop");assertTrue(store.pinned(APP));assertTrue(store.startDeleted().contains(APP));assertTrue(AppUninstaller.available(activity,APP));
    }
    @Test public void desktopPinRestoresAProgramPreviouslyRemovedFromStart(){
        XpStartMenus.remove(store,APP,true);ShortcutMenus.show(activity,start,30,120,store,APP,"desktop","Test app",host);click("Pin to Start Menu");assertTrue(store.startPins().contains(APP));assertFalse(store.startExcluded().contains(APP));assertFalse(store.startDeleted().contains(APP));assertNotNull(find(start,"Open Programs: Test app"));
    }
    @Test public void myComputerHasSystemCommandsAndSettingsLinksHaveNoShortcutDeletion(){
        assertTrue(find(start,"My Computer").performLongClick());assertEquals(XpStartMenus.PLACE,selectedSource);assertNotNull(command("Explore"));assertNotNull(command("Manage"));assertNull(find(popup().getContentView(),"Delete"));click("Properties");assertEquals("@system",opened);
        assertTrue(find(start,"Connect To").performContextClick());click("Open");assertEquals("setting:android:network",opened);
    }
    @Test public void propertiesShowsRealPackageAndAppInfoAndBuiltinsCannotUninstall(){
        menu(APP,XpStartMenus.PROGRAMS);click("Properties");AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();assertTrue(shadowOf(dialog).getMessage().toString().contains("example.startapp"));assertTrue(shadowOf(dialog).getMessage().toString().contains("1.2"));dialog.getButton(AlertDialog.BUTTON_NEUTRAL).performClick();layout();assertEquals("package:example.startapp",shadowOf(activity).getNextStartedActivity().getDataString());
        menu("@paint",XpStartMenus.PROGRAMS);assertNull(find(popup().getContentView(),"Uninstall"));
    }
    @Test public void uninstallUsesAndroidConfirmationWithoutDeletingTheStartOrDesktopShortcut(){
        store.startPins(Collections.singletonList(APP));store.desktop(Collections.singletonList(APP));menu(APP,XpStartMenus.HOME);click("Uninstall");org.robolectric.shadows.ShadowActivity.IntentForResult intent=shadowOf(activity).getNextStartedActivityForResult();assertEquals(Intent.ACTION_DELETE,intent.intent.getAction());assertEquals("package:example.startapp",intent.intent.getDataString());assertTrue(store.startPins().contains(APP));assertTrue(store.pinned(APP));
    }
    @Test public void recentDocumentRemovalOnlyChangesHistory(){
        RecentFiles.opened(store,Uri.parse("content://example/book"),"text/plain","Book.txt");find(start,"My Recent Documents").performClick();assertTrue(command("Book.txt").performLongClick());assertEquals(XpStartMenus.DOCUMENT,selectedSource);assertNull(find(popup().getContentView(),"Uninstall"));click("Remove from This List");assertEquals(0,RecentFiles.list(store).length());assertTrue(store.startDeleted().isEmpty());
    }
    @Test public void startStateAndNewShortcutTargetsRoundTripInDesktopBackup()throws Exception{
        XpStartMenus.remove(store,APP,true);XpStartMenus.pin(store,"xp:mail",true);store.desktop(Arrays.asList("xp:mail","setting:android:network","xp:run"));store.prefs.edit().putBoolean("xp_start_sort",true).commit();DesktopBackup.Plan plan=DesktopBackup.parse(DesktopBackup.snapshot(store).toString());assertEquals(true,plan.values.get("xp_start_sort"));assertTrue(DesktopStore.array(new org.json.JSONArray(plan.string("xp_start_deleted","[]"))).contains(APP));assertTrue(plan.string("shortcuts","").contains("xp:mail"));
    }
}
