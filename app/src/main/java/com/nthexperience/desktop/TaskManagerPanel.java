package com.nthexperience.desktop;

import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.*;
import android.net.TrafficStats;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;

/** Windows XP classic monitor. Animations interpolate measured samples only. */
final class TaskManagerPanel extends LinearLayout {
    static final class Task {final String id,title,status;final boolean external;Task(String id,String title,String status,boolean external){this.id=id;this.title=title;this.status=status;this.external=external;}}
    interface Host {List<Task> tasks();View icon(String id);void switchTo(String id);void end(String id);void appInfo(String id);void newTask();void close();default void restart(){newTask();}}
    private final Host host;private final LinearLayout content,tabs;private final TextView status,selection,switchTo,end;private LinearLayout actionRow;private TextView statusCpu,statusMemory;private Meter cpuMeter,memoryMeter;private int processCount;
    private List<Task> tasks=new ArrayList<>();private String selected="",tab="Applications",sort="Task";private boolean active=true,paused,descending;
    private int interval=2000,samples;private long lastWall,lastCpu,lastRx=-1,lastTx=-1;private float cpu=Float.NaN,memory,heap,rx=Float.NaN,tx=Float.NaN;
    private ActivityManager.MemoryInfo memoryInfo=new ActivityManager.MemoryInfo();private DesktopContextMenu menu;
    private final float[][] history=new float[5][60];private final List<HistoryGraph> graphs=new ArrayList<>();
    private TextView cpuText,memoryText,heapText,detail,rxText,txText;
    private final Runnable tick=new Runnable(){public void run(){if(!sampling())return;sample();if(tab.equals("Applications"))refreshTasks();else if(tab.equals("Processes"))renderProcesses();postDelayed(this,interval);}};
    TaskManagerPanel(Context c,Host host){
        super(c);this.host=host;setOrientation(VERTICAL);setBackgroundColor(XpTheme.surface(c));setContentDescription("Task Manager content");for(float[] series:history)Arrays.fill(series,Float.NaN);
        LinearLayout bar=new LinearLayout(c);bar.setPadding(dp(3),0,0,0);
        for(String label:new String[]{"File","Options","View","Help"}){TextView link=text(label,11);link.setGravity(Gravity.CENTER);link.setFocusable(true);link.setBackground(XpControls.menuSelection(c));link.setTextColor(XpControls.menuInk(Aero.INK));link.setContentDescription("Task Manager "+label+" menu");link.setOnClickListener(v->menu(link,label));bar.addView(link,new LayoutParams(-2,dp(26)));}addView(bar);
        HorizontalScrollView tabScroll=new HorizontalScrollView(c);tabScroll.setHorizontalScrollBarEnabled(false);tabs=new LinearLayout(c);tabs.setPadding(dp(5),dp(4),dp(5),0);tabScroll.addView(tabs);addView(tabScroll,new LayoutParams(-1,dp(35)));
        content=Aero.column(c);content.setPadding(dp(8),dp(9),dp(8),dp(7));content.setBackground(Aero.panel(c,XpTheme.surface(c),XpTheme.surface(c),0,0xff919b9c));LayoutParams inner=new LayoutParams(-1,0,1);inner.setMargins(dp(5),0,dp(5),0);addView(content,inner);
        selection=text("Select a task",10);selection.setMaxLines(2);addView(selection,new LayoutParams(-1,dp(30)));
        actionRow=new LinearLayout(c);actionRow.setGravity(Gravity.RIGHT);actionRow.setPadding(dp(5),dp(3),dp(5),dp(6));TextView newTask=Aero.button(c,"New Task…",host::newTask);switchTo=Aero.button(c,"Switch To",this::switchSelected);end=Aero.button(c,"End Task",this::endSelected);
        for(TextView button:new TextView[]{end,switchTo,newTask}){button.setTextSize(11);button.setMinWidth(0);LayoutParams size=new LayoutParams(0,dp(32),1);size.setMargins(dp(3),0,0,0);actionRow.addView(button,size);}addView(actionRow);
        LinearLayout statusBar=new LinearLayout(c);status=text("",10);statusCpu=text("",10);statusMemory=text("",10);for(TextView field:new TextView[]{status,statusCpu,statusMemory}){field.setSingleLine();field.setEllipsize(android.text.TextUtils.TruncateAt.END);field.setBackground(Aero.panel(c,XpTheme.surface(c),XpTheme.surface(c),0,0xffa0a0a0));statusBar.addView(field,new LayoutParams(0,dp(24),1));}addView(statusBar);render();
    }
    private void menu(View anchor,String label){dismissMenu();menu=new DesktopContextMenu(getContext(),"Task Manager "+label+" menu");
        if(label.equals("File")){menu.item("New Task…","programs",host::newTask);menu.item("Restart Nth Desktop","refresh",host::restart);menu.separator();menu.item("Exit Task Manager","close",host::close);}
        else if(label.equals("View")){menu.item("Refresh Now","refresh",this::refresh);for(int speed:new int[]{1000,2000,5000})menu.checked(speed==1000?"Update speed: High":speed==2000?"Update speed: Normal":"Update speed: Low",interval==speed&&!paused,()->{interval=speed;paused=false;sync();});menu.checked("Pause updates",paused,()->{paused=!paused;sync();});}
        else if(label.equals("Options")){menu.checked("Animated graphs",getContext().getSharedPreferences("desktop",0).getBoolean("taskmanager_animate",true),()->{android.content.SharedPreferences p=getContext().getSharedPreferences("desktop",0);p.edit().putBoolean("taskmanager_animate",!p.getBoolean("taskmanager_animate",true)).apply();stopAnimations();});}
        else menu.item("About this Task Manager","taskmanager",()->Aero.dialog(getContext()).setTitle("Nth Task Manager").setMessage("Applications manages real Nth windows. Processes lists only processes owned by Nth. Performance shows Nth CPU, device RAM and Nth Java heap. Networking measures device-wide interface counters; it does not inspect connections or browsing content. Unsupported counters are marked unavailable. Animation interpolates real samples, follows reduced motion, and stops when hidden or in the background.").setPositiveButton("OK",null).show());
        ContextMenus.centered(anchor,(x,y)->ContextMenus.show(menu,anchor,x,y));
    }
    private void render(){stopAnimations();graphs.clear();cpuText=memoryText=heapText=detail=rxText=txText=null;tabs.removeAllViews();for(String name:new String[]{"Applications","Processes","Performance","Networking","Users"}){TextView button=text(name,11);button.setGravity(Gravity.CENTER);button.setFocusable(true);button.setBackground(XpControls.tab(getContext()));button.setSelected(name.equals(tab));button.setContentDescription("Task Manager "+name);button.setOnClickListener(v->{tab=name;render();});tabs.addView(button,new LayoutParams(dp(name.equals("Applications")?83:name.equals("Performance")?80:name.equals("Processes")?71:name.equals("Networking")?74:45),dp(31)));}
        content.removeAllViews();if(tab.equals("Applications"))renderTasks();else if(tab.equals("Processes"))renderProcesses();else if(tab.equals("Users"))renderSystemTab();else renderGraphs();updateSelection();updateLabels();
    }
    private LinearLayout table(String[] columns,float[] widths){
        LinearLayout panel=Aero.column(getContext());panel.setBackground(Aero.panel(getContext(),0xffffffff,0xffffffff,0,0xff7f9db9));panel.setPadding(dp(1),dp(1),dp(1),dp(1));
        LinearLayout header=new LinearLayout(getContext());for(int i=0;i<columns.length;i++){TextView cell=text(columns[i],11);cell.setBackground(Aero.panel(getContext(),0xfffaf9f4,0xffe9e7df,0,0xffc7c5b2));cell.setSingleLine();cell.setEllipsize(android.text.TextUtils.TruncateAt.END);header.addView(cell,new LayoutParams(0,dp(25),widths[i]));}panel.addView(header);return panel;
    }
    private void renderTasks(){
        tasks=new ArrayList<>(host.tasks());if(tasks.stream().noneMatch(t->t.id.equals(selected)))selected="";
        tasks.sort((a,b)->{int compare=(sort.equals("Status")?a.status:a.title).compareToIgnoreCase(sort.equals("Status")?b.status:b.title);return descending?-compare:compare;});
        LinearLayout panel=table(new String[]{"Task","Status"},new float[]{.7f,.3f});panel.setContentDescription("XP applications list");content.addView(panel,new LayoutParams(-1,0,1));
        LinearLayout header=(LinearLayout)panel.getChildAt(0);for(int i=0;i<2;i++){String name=i==0?"Task":"Status";TextView cell=(TextView)header.getChildAt(i);cell.setText(name+(sort.equals(name)?descending?" ▾":" ▴":""));cell.setContentDescription("Sort tasks by "+name);cell.setOnClickListener(v->{descending=sort.equals(name)&&!descending;sort=name;content.removeAllViews();renderTasks();updateSelection();});}
        ScrollView scroll=new ScrollView(getContext());LinearLayout rows=Aero.column(getContext());scroll.addView(rows);panel.addView(scroll,new LayoutParams(-1,0,1));
        for(Task task:tasks){
            LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(3),0,0,0);row.setFocusable(true);row.setContentDescription("Task: "+task.title);row.setBackground(XpControls.menuSelection(getContext()));row.setSelected(task.id.equals(selected));
            row.addView(host.icon(task.id),new LayoutParams(dp(16),dp(16)));TextView title=text(task.title,11);title.setTextColor(XpControls.menuInk(Aero.INK));title.setDuplicateParentStateEnabled(true);title.setSingleLine();title.setEllipsize(android.text.TextUtils.TruncateAt.END);row.addView(title,new LayoutParams(0,-1,.7f));
            TextView state=text(task.status,10);state.setTextColor(XpControls.menuInk(Aero.INK));state.setDuplicateParentStateEnabled(true);state.setMaxLines(2);row.addView(state,new LayoutParams(0,-1,.3f));
            row.setOnClickListener(v->{selected=task.id;render();});ContextMenus.bind(row,(x,y)->{selected=task.id;updateSelection();dismissMenu();menu=new DesktopContextMenu(getContext(),"Task commands");menu.item("Switch To","programs",this::switchSelected);menu.item(task.external?"App Info…":"End Task",task.external?"control":"close",this::endSelected);ContextMenus.show(menu,row,x,y);});rows.addView(row,new LayoutParams(-1,dp(30)));
        }
        if(tasks.isEmpty())rows.addView(text("No other Nth windows are open.",11),new LayoutParams(-1,dp(42)));
    }
    private void renderProcesses(){
        if(!tab.equals("Processes"))return;content.removeAllViews();LinearLayout panel=table(new String[]{"Image Name","PID","Mem Usage"},new float[]{.5f,.18f,.32f});panel.setContentDescription("XP processes list");content.addView(panel,new LayoutParams(-1,-1));ScrollView scroll=new ScrollView(getContext());LinearLayout rows=Aero.column(getContext());scroll.addView(rows);panel.addView(scroll,new LayoutParams(-1,0,1));
        ActivityManager manager=getContext().getSystemService(ActivityManager.class);List<ActivityManager.RunningAppProcessInfo> running=manager==null?null:manager.getRunningAppProcesses();processCount=0;
        if(running!=null)for(ActivityManager.RunningAppProcessInfo process:running){if(process.uid!=android.os.Process.myUid())continue;processCount++;long bytes=-1;try{Debug.MemoryInfo[] values=manager.getProcessMemoryInfo(new int[]{process.pid});if(values.length>0)bytes=values[0].getTotalPss()*1024L;}catch(RuntimeException ignored){}
            LinearLayout row=new LinearLayout(getContext());String[] values={process.processName,Integer.toString(process.pid),bytes>=0?mb(bytes):"Unavailable"};float[] weights={.5f,.18f,.32f};for(int i=0;i<values.length;i++){TextView value=text(values[i],10);value.setSingleLine();value.setEllipsize(android.text.TextUtils.TruncateAt.END);row.addView(value,new LayoutParams(0,dp(30),weights[i]));}rows.addView(row);
        }
        if(processCount==0)rows.addView(text("Process details are unavailable on this device.",11));updateSelection();updateLabels();
    }
    private void renderSystemTab(){
        LinearLayout panel=table(new String[]{"User","ID","Status"},new float[]{.55f,.15f,.3f});panel.setContentDescription("XP users list");content.addView(panel,new LayoutParams(-1,-1));LinearLayout row=new LinearLayout(getContext());String[] values={getContext().getSharedPreferences("desktop",0).getString("desktop_name","Nth Experience"),Integer.toString(android.os.Process.myUid()/100000),"Active"};float[] weights={.55f,.15f,.3f};for(int i=0;i<values.length;i++)row.addView(text(values[i],11),new LayoutParams(0,dp(32),weights[i]));panel.addView(row);
    }
    private LinearLayout group(LinearLayout parent,String label){
        LinearLayout wrapper=Aero.column(getContext());wrapper.setPadding(dp(7),0,dp(7),dp(7));wrapper.setBackground(Aero.panel(getContext(),XpTheme.surface(getContext()),XpTheme.surface(getContext()),0,0xffaca899));TextView title=text(label,11);title.setPadding(0,0,0,dp(3));wrapper.addView(title,new LayoutParams(-1,dp(23)));LayoutParams lp=new LayoutParams(-1,-2);lp.bottomMargin=dp(8);parent.addView(wrapper,lp);return wrapper;
    }
    private void renderGraphs(){
        ScrollView scroll=new ScrollView(getContext());LinearLayout body=Aero.column(getContext());scroll.addView(body);content.addView(scroll,new LayoutParams(-1,-1));
        if(tab.equals("Performance")){
            LinearLayout first=new LinearLayout(getContext());body.addView(first);LinearLayout meterColumn=Aero.column(getContext());first.addView(meterColumn,new LayoutParams(dp(78),-2));cpuText=text("CPU Usage",10);meterColumn.addView(cpuText,new LayoutParams(-1,dp(26)));cpuMeter=new Meter(getContext());cpuMeter.setContentDescription("Nth CPU usage meter");meterColumn.addView(cpuMeter,new LayoutParams(-1,dp(96)));
            LinearLayout graphColumn=Aero.column(getContext());LayoutParams gp=new LayoutParams(0,-2,1);gp.leftMargin=dp(7);first.addView(graphColumn,gp);graph(graphColumn,"CPU Usage History (Nth)",0,"Nth Desktop CPU history",100);
            LinearLayout second=new LinearLayout(getContext());body.addView(second);LinearLayout ramColumn=Aero.column(getContext());second.addView(ramColumn,new LayoutParams(dp(78),-2));memoryText=text("RAM Usage",10);ramColumn.addView(memoryText,new LayoutParams(-1,dp(26)));memoryMeter=new Meter(getContext());memoryMeter.setContentDescription("Physical memory usage meter");ramColumn.addView(memoryMeter,new LayoutParams(-1,dp(96)));LinearLayout memoryColumn=Aero.column(getContext());LayoutParams mp=new LayoutParams(0,-2,1);mp.leftMargin=dp(7);second.addView(memoryColumn,mp);graph(memoryColumn,"Physical Memory Usage History",1,"Device physical memory history",100);
            LinearLayout stats=group(body,"Physical Memory and Nth Process");detail=text("",11);detail.setTypeface(Typeface.MONOSPACE);stats.addView(detail);heapText=text("",10);stats.addView(heapText);
        }else{
            rxText=graph(body,"Received",3,"Device received traffic history",0);txText=graph(body,"Sent",4,"Device sent traffic history",0);LinearLayout note=group(body,"Adapter information");detail=text("Device-wide traffic. Android does not expose a separate per-adapter list to this launcher.",11);note.addView(detail);
        }if(samples==0)sample();
    }
    private TextView graph(LinearLayout body,String label,int series,String description,float maximum){LinearLayout box=group(body,label);HistoryGraph graph=new HistoryGraph(getContext(),history[series],maximum);graph.setContentDescription(description);graphs.add(graph);box.addView(graph,new LayoutParams(-1,dp(90)));TextView value=text("",10);if(tab.equals("Networking"))box.addView(value,new LayoutParams(-1,dp(23)));return value;}
    private TextView text(String value,int size){TextView t=Aero.text(getContext(),value,size,Aero.INK);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(6),dp(3),dp(4),dp(3));return t;}
    private Task selected(){for(Task task:tasks)if(task.id.equals(selected))return task;return null;}
    private void updateSelection(){
        Task task=selected();boolean apps=tab.equals("Applications"),enabled=apps&&task!=null;switchTo.setEnabled(enabled);end.setEnabled(enabled||tab.equals("Processes"));end.setText(tab.equals("Processes")?"Restart Desktop":task!=null&&task.external?"App Info…":"End Task");actionRow.setVisibility(apps||tab.equals("Processes")?VISIBLE:GONE);switchTo.setVisibility(apps?VISIBLE:GONE);actionRow.getChildAt(2).setVisibility(apps?VISIBLE:GONE);
        selection.setText(tab.equals("Performance")?"CPU: Nth process, one core. Memory: whole device.":tab.equals("Networking")?"Device-wide counters; no connection inspection.":tab.equals("Processes")?"Only processes owned by Nth are visible to Android.":tab.equals("Users")?"Current Android user; other users are private.":task!=null&&task.external?"Recently opened Android app. App Info manages it.":task==null?"Select a task.":task.title);
    }
    private void switchSelected(){Task task=selected();if(task!=null&&tab.equals("Applications"))host.switchTo(task.id);}
    private void endSelected(){if(tab.equals("Processes")){host.restart();return;}Task task=selected();if(task==null)return;if(task.external)host.appInfo(task.id);else host.end(task.id);refresh();}
    void tasksChanged(){if(tab.equals("Applications"))refreshTasks();}
    private void refreshTasks(){List<Task> next=host.tasks();boolean same=next.size()==tasks.size();if(same)for(Task a:next){Task b=tasks.stream().filter(t->t.id.equals(a.id)).findFirst().orElse(null);if(b==null||!a.title.equals(b.title)||!a.status.equals(b.status)){same=false;break;}}if(!same){content.removeAllViews();renderTasks();updateSelection();}}
    void refresh(){if(tab.equals("Applications"))refreshTasks();else{sample();if(tab.equals("Processes"))renderProcesses();}}
    private void sample(){long now=SystemClock.elapsedRealtime(),elapsed=lastWall==0?0:now-lastWall,currentCpu=android.os.Process.getElapsedCpuTime(),currentRx=TrafficStats.getTotalRxBytes(),currentTx=TrafficStats.getTotalTxBytes();cpu=elapsed>0?Math.max(0,100f*(currentCpu-lastCpu)/elapsed):Float.NaN;rx=elapsed>0&&lastRx>=0&&currentRx>=lastRx?(currentRx-lastRx)*1000f/elapsed:Float.NaN;tx=elapsed>0&&lastTx>=0&&currentTx>=lastTx?(currentTx-lastTx)*1000f/elapsed:Float.NaN;lastWall=now;lastCpu=currentCpu;lastRx=currentRx;lastTx=currentTx;
        ActivityManager manager=getContext().getSystemService(ActivityManager.class);if(manager!=null)manager.getMemoryInfo(memoryInfo);memory=memoryInfo.totalMem>0?100f*(memoryInfo.totalMem-memoryInfo.availMem)/memoryInfo.totalMem:Float.NaN;Runtime runtime=Runtime.getRuntime();heap=100f*(runtime.totalMemory()-runtime.freeMemory())/runtime.maxMemory();float[] values={Float.isNaN(cpu)?cpu:Math.min(100,cpu),memory,heap,rx,tx};for(int i=0;i<history.length;i++){System.arraycopy(history[i],1,history[i],0,59);history[i][59]=values[i];}samples++;updateLabels();for(HistoryGraph graph:graphs)graph.changed(sampling()&&!paused&&Aero.motion(getContext())&&getContext().getSharedPreferences("desktop",0).getBoolean("taskmanager_animate",true));
    }
    private void updateLabels(){
        if(cpuText!=null){cpuMeter.value(cpu);memoryMeter.value(memory);Runtime runtime=Runtime.getRuntime();detail.setText("Total RAM     "+mb(memoryInfo.totalMem)+"\nAvailable     "+mb(memoryInfo.availMem)+"\nNth threads   "+Thread.getAllStackTraces().size()+"\nUptime        "+SystemClock.elapsedRealtime()/3600000+"h "+SystemClock.elapsedRealtime()/60000%60+"m");heapText.setText("Nth Java heap: "+mb(runtime.totalMemory()-runtime.freeMemory())+" / "+mb(runtime.maxMemory()));}
        if(rxText!=null){rxText.setText("Received: "+rate(rx));txText.setText("Sent: "+rate(tx));}
        if(status!=null){status.setText((paused?"Paused • ":"")+"Nth tasks: "+host.tasks().size());statusCpu.setText("Nth CPU: "+percent(cpu));statusMemory.setText("RAM: "+percent(memory));}
    }
    private String percent(float n){return Float.isNaN(n)?"sampling…":String.format(Locale.getDefault(),"%.1f%%",n);}
    private String rate(float value){return Float.isNaN(value)?"unavailable / awaiting interval":mb((long)value)+"/s";}
    private String mb(long bytes){return android.text.format.Formatter.formatShortFileSize(getContext(),Math.max(0,bytes));}
    private boolean sampling(){return active&&!paused&&isAttachedToWindow()&&isShown()&&getWindowVisibility()==VISIBLE;}
    private void sync(){removeCallbacks(tick);lastWall=0;lastCpu=0;lastRx=lastTx=-1;if(!sampling())stopAnimations();else post(tick);updateLabels();}
    private void stopAnimations(){for(HistoryGraph graph:graphs)graph.stop();}
    void active(boolean value){active=value;if(!value)dismissMenu();sync();}
    int sampleCount(){return samples;}int animatedGraphCount(){int n=0;for(HistoryGraph graph:graphs)if(graph.animating())n++;return n;}
    JSONObject state(){JSONObject value=new JSONObject();GadgetLayout.put(value,"tab",tab);GadgetLayout.put(value,"selected",selected);GadgetLayout.put(value,"paused",paused);GadgetLayout.put(value,"interval",interval);GadgetLayout.put(value,"sort",sort);GadgetLayout.put(value,"descending",descending);return value;}
    void restore(JSONObject value){if(value==null)return;String name=value.optString("tab");tab=Arrays.asList("Applications","Processes","Performance","Networking","Users").contains(name)?name:"Applications";selected=value.optString("selected");paused=value.optBoolean("paused");interval=value.optInt("interval",2000);if(interval!=1000&&interval!=2000&&interval!=5000)interval=2000;sort=value.optString("sort").equals("Status")?"Status":"Task";descending=value.optBoolean("descending");render();sync();}
    void close(){active=false;removeCallbacks(tick);stopAnimations();dismissMenu();}
    private void dismissMenu(){if(menu!=null){menu.dismiss();menu=null;}}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();sync();}
    @Override protected void onVisibilityChanged(View v,int visibility){super.onVisibilityChanged(v,visibility);if(content!=null)sync();}
    @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(content!=null)sync();}
    @Override protected void onDetachedFromWindow(){removeCallbacks(tick);stopAnimations();dismissMenu();super.onDetachedFromWindow();}
    private int dp(int value){return Aero.dp(getContext(),value);}
    private static final class Meter extends View {
        private float value=Float.NaN;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        Meter(Context c){super(c);}void value(float n){value=n;invalidate();}
        @Override protected void onDraw(Canvas canvas){canvas.drawColor(Color.BLACK);float d=getResources().getDisplayMetrics().density;int bars=18;float h=(getHeight()-22*d)/bars,w=(getWidth()-14*d)/2;for(int n=0;n<bars;n++){paint.setColor(!Float.isNaN(value)&&n*100f/bars<value?0xff00ff00:0xff003800);float y=getHeight()-21*d-(n+1)*h;for(int col=0;col<2;col++)canvas.drawRect(4*d+col*(w+4*d),y,4*d+col*(w+4*d)+w,y+Math.max(d,h-2*d),paint);}paint.setColor(0xff00ff00);paint.setTextSize(11*d);paint.setTypeface(Typeface.MONOSPACE);paint.setTextAlign(Paint.Align.CENTER);canvas.drawText(Float.isNaN(value)?"…":Math.round(value)+"%",getWidth()/2f,getHeight()-4*d,paint);}
    }
    private static final class HistoryGraph extends View {
        private final float[] values;private float[] before;private float fraction=1,maximum;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private ValueAnimator animator;
        HistoryGraph(Context context,float[] values,float maximum){super(context);this.values=values;this.maximum=maximum;before=values.clone();}
        void changed(boolean animate){stop();if(animate){fraction=0;animator=ValueAnimator.ofFloat(0,1);animator.setDuration(500);animator.addUpdateListener(a->{fraction=(float)a.getAnimatedValue();invalidate();});animator.start();}else{fraction=1;before=values.clone();invalidate();}}
        boolean animating(){return animator!=null&&animator.isRunning();}
        void stop(){if(animator!=null){animator.cancel();animator=null;}fraction=1;}
        @Override protected void onDraw(Canvas canvas){canvas.drawColor(0xff000000);paint.setColor(0xff004000);paint.setStrokeWidth(1);for(int x=0;x<getWidth();x+=Math.max(1,getWidth()/12))canvas.drawLine(x,0,x,getHeight(),paint);for(int y=0;y<getHeight();y+=Math.max(1,getHeight()/5))canvas.drawLine(0,y,getWidth(),y,paint);float ceiling=maximum;if(ceiling<=0){ceiling=1024;for(float n:values)if(Float.isFinite(n))ceiling=Math.max(ceiling,n*1.15f);}paint.setColor(0xff57e86c);paint.setStrokeWidth(Aero.dp(getContext(),1));for(int i=1;i<60;i++){float a=value(i-1),b=value(i);if(Float.isFinite(a)&&Float.isFinite(b))canvas.drawLine((i-1)*getWidth()/59f,getHeight()*(1-a/ceiling),i*getWidth()/59f,getHeight()*(1-b/ceiling),paint);}if(fraction>=1)before=values.clone();}
        private float value(int i){return Float.isFinite(before[i])&&Float.isFinite(values[i])?before[i]*(1-fraction)+values[i]*fraction:values[i];}
    }
}
