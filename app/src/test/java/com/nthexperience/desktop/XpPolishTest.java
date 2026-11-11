package com.nthexperience.desktop;

import android.content.Context;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w393dp-h800dp-mdpi")
@LooperMode(LooperMode.Mode.PAUSED)
public class XpPolishTest {
    Context c;DesktopStore store;
    @Before public void prepare(){c=RuntimeEnvironment.getApplication();store=new DesktopStore(c);store.prefs.edit().clear().commit();XpTheme.defaults(store);}
    @Test public void explorerTaskGroupsHaveGapsAndCanCollapse(){
        LinearLayout parent=Aero.column(c);XpTaskPane first=XpTaskPane.add(parent,"File and Folder Tasks",false),second=XpTaskPane.add(parent,"Other Places",false);
        assertTrue(((LinearLayout.LayoutParams)first.getLayoutParams()).bottomMargin>=Aero.dp(c,14));assertTrue(second.items.getPaddingTop()>0);
        find(first,"Collapse task group: File and Folder Tasks").performClick();assertEquals(View.GONE,first.items.getVisibility());find(first,"Expand task group: File and Folder Tasks").performClick();assertEquals(View.VISIBLE,first.items.getVisibility());
    }
    @Test public void taskbarDraftCancelsAndAppliesAcrossTabs()throws Exception{
        boolean[] closed={false};List<String> log=new ArrayList<>();VistaShell.Actions actions=actions(log);
        XpTaskbarProperties panel=new XpTaskbarProperties(c,store,actions,null,()->closed[0]=true);
        ((CheckBox)find(panel,"Expand taskbar to two rows")).setChecked(true);assertFalse(store.prefs.getBoolean("taskbar_expanded",false));
        find(panel,"Property tab: Start Menu").performClick();JSONObject pending=new JSONObject(panel.state().toString());panel=new XpTaskbarProperties(c,store,actions,pending,()->closed[0]=true);
        find(panel,"Property tab: Taskbar").performClick();assertTrue(((CheckBox)find(panel,"Expand taskbar to two rows")).isChecked());find(panel,"Cancel").performClick();assertTrue(closed[0]);assertFalse(store.prefs.getBoolean("taskbar_expanded",false));assertTrue(log.isEmpty());
        panel=new XpTaskbarProperties(c,store,actions,null,()->{});((CheckBox)find(panel,"Expand taskbar to two rows")).setChecked(true);find(panel,"Apply").performClick();assertTrue(store.prefs.getBoolean("taskbar_expanded",false));assertEquals(Collections.singletonList("taskbar-refresh"),log);
    }
    @Test public void taskManagerListsRealTasksAndKeepsPerformanceSeparate(){
        List<String> events=new ArrayList<>();TaskManagerPanel panel=new TaskManagerPanel(c,new TaskManagerPanel.Host(){
            public List<TaskManagerPanel.Task> tasks(){return Arrays.asList(new TaskManagerPanel.Task("notes","Notepad","Running",false),new TaskManagerPanel.Task("external","Music","Recently opened",true));}
            public View icon(String id){return new AeroIcon(c,"notes");}public void switchTo(String id){events.add("switch:"+id);}public void end(String id){events.add("end:"+id);}public void appInfo(String id){events.add("info:"+id);}public void newTask(){}public void close(){}
        });
        assertNotNull(find(panel,"XP applications list"));find(panel,"Task: Notepad").performClick();find(panel,"End Task").performClick();assertEquals(Collections.singletonList("end:notes"),events);
        find(panel,"Task: Music").performClick();find(panel,"App Info…").performClick();assertEquals("info:external",events.get(1));
        find(panel,"Task Manager Performance").performClick();assertNotNull(find(panel,"Nth CPU usage meter"));assertNotNull(find(panel,"Nth Desktop CPU history"));assertNotNull(find(panel,"Physical memory usage meter"));assertNull(find(panel,"Control Panel categories"));
        find(panel,"Task Manager Processes").performClick();assertNotNull(find(panel,"XP processes list"));find(panel,"Task Manager Users").performClick();assertNotNull(find(panel,"XP users list"));panel.close();
    }
    @Test public void catalogsAndSearchExposeXpNames(){
        assertEquals(10,ControlPanelCatalog.CATEGORIES.length);
        for(ControlPanelCatalog.Panel panel:ControlPanelCatalog.PANELS)for(String later:new String[]{"Action Center","Network and Sharing Center","Programs and Features","Default Programs","Gadget Settings"})assertNotEquals(later,panel.title);
        for(String query:new String[]{"flip","gadget","peek","aero"})for(StartSearch.Result r:StartSearch.find(store,Collections.emptyList(),query))assertFalse(r.id.equals("@flip3d")||r.id.equals("@gadgets"));
    }
    @Test public void systemAndClockUsePropertyTabs(){View system=XpSystemProperties.create(c,store,actions(new ArrayList<>()),()->{});for(String name:XpSystemProperties.TABS){assertNotNull(find(system,"Property tab: "+name));find(system,"Property tab: "+name).performClick();}View clock=XpDateTimeProperties.create(c,actions(new ArrayList<>()),()->{});assertNotNull(find(clock,"Analog system clock"));assertNotNull(find(clock,"Property tab: Time Zone"));}
    private VistaShell.Actions actions(List<String> out){return new VistaShell.Actions(){public void open(String id){out.add(id);}public void action(String id){out.add(id);}};}
    private View find(View view,String description){if(description.equals(view.getContentDescription())||view instanceof TextView&&description.contentEquals(((TextView)view).getText()))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View found=find(((ViewGroup)view).getChildAt(i),description);if(found!=null)return found;}return null;}
}
