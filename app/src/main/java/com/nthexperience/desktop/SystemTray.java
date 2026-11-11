package com.nthexperience.desktop;

import android.content.*;
import android.graphics.*;
import android.media.AudioManager;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Foreground-only status subscriptions and native flyouts, within the desktop's safe bounds. */
final class SystemTray extends LinearLayout {
    interface Host {void action(String id);void open(String id);void opening();default void layout(){}}
    private final FrameLayout workspace;
    private final AeroWallpaper wallpaper;
    private final Host host;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ConnectivityManager connections;
    private final AudioManager audio;
    private final PowerManager power;
    private final ArrayList<TrayIcon> icons=new ArrayList<>();
    private FrameLayout cover;
    private LinearLayout frame;
    private TextView summary;
    private SeekBar volume;private CheckBox mute;private DesktopContextMenu commands;private boolean networkOnline,networkLimited,volumeMuted;
    private Intent battery;
    private String page="";
    private int leftInset,rightInset,restoreVolume,workspaceBudget,overlayInset;
    private TextView language;private View removable;private XpTrayBalloon balloon;private boolean lowBatteryShown;
    private final android.os.storage.StorageManager storage;
    private final View taskManager;
    private final TrayFlag actionCenter;private int alertCount=-1;
    private final SharedPreferences notices;
    private final SharedPreferences.OnSharedPreferenceChangeListener alertChanges=(saved,key)->{if(key==null||key.startsWith("tray_")||key.equals("files")||key.equals("backup")||key.equals("linked_folders"))refresh();};
    private final BrightnessControl brightness;private boolean expanded;private final android.content.SharedPreferences prefs;private TextView compactStatus;
    private boolean running,batteryRegistered,networkRegistered,storageRegistered,tracking;
    private final BroadcastReceiver batteryChanged=new BroadcastReceiver(){public void onReceive(Context c,Intent i){if(Intent.ACTION_BATTERY_CHANGED.equals(i.getAction()))battery=i;update();}};
    private final BroadcastReceiver storageChanged=new BroadcastReceiver(){public void onReceive(Context c,Intent i){refresh();}};
    private final ConnectivityManager.NetworkCallback networkChanged=new ConnectivityManager.NetworkCallback(){public void onAvailable(Network n){refresh();}public void onLost(Network n){refresh();}public void onCapabilitiesChanged(Network n,NetworkCapabilities caps){refresh();}};
    private final Runnable tick=new Runnable(){public void run(){if(!running||cover==null)return;update();main.postDelayed(this,1000);}};
    SystemTray(Context c,FrameLayout workspace,AeroWallpaper wallpaper,Host host){
        super(c);storage=c.getSystemService(android.os.storage.StorageManager.class);balloon=new XpTrayBalloon(workspace);notices=c.getSharedPreferences("seven_action_center",0);prefs=c.getSharedPreferences("desktop",0);expanded=prefs.getBoolean("tray_expanded",false);brightness=new BrightnessControl((android.app.Activity)c);this.workspace=workspace;this.wallpaper=wallpaper;this.host=host;connections=c.getSystemService(ConnectivityManager.class);audio=c.getSystemService(AudioManager.class);power=c.getSystemService(PowerManager.class);setGravity(Gravity.CENTER_VERTICAL);
        String[] names={"Battery status","Network status","Volume control","Brightness control"};String[] pages={"battery","network","volume","brightness"};
        for(int i=0;i<names.length;i++){TrayIcon icon=new TrayIcon(c,i);icon.setContentDescription(names[i]);icon.setFocusable(true);icon.setBackground(Aero.selection(c));String target=pages[i];icon.setOnClickListener(v->show(target));addView(icon,new LayoutParams(dp(28),dp(XpMetrics.ROW)));icons.add(icon);ContextMenus.bind(icon,(x,y)->menu(icon,target,x,y));}
        taskManager = new TrayIcon(c,5);
        taskManager.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        taskManager.setFocusable(true);
        taskManager.setContentDescription("Task Manager");
        taskManager.setTooltipText("Task Manager");
        taskManager.setBackground(Aero.selection(c));
        taskManager.setOnClickListener(v -> host.open("@taskmanager"));
        addView(taskManager, new LayoutParams(dp(28), dp(XpMetrics.ROW)));
        actionCenter=new TrayFlag(c);
        actionCenter.setFocusable(true);
        actionCenter.setContentDescription("Action Center");
        actionCenter.setTooltipText("Action Center");
        actionCenter.setBackground(Aero.selection(c));
        actionCenter.setOnClickListener(v->show("actioncenter"));
        addView(actionCenter,new LayoutParams(dp(28),dp(XpMetrics.ROW)));
        language=Aero.text(c,"",11,XpTheme.taskbarInk(c));language.setGravity(Gravity.CENTER);language.setContentDescription("Language indicator");language.setTooltipText("Choose Android input method");language.setFocusable(true);language.setBackground(Aero.selection(c));language.setOnClickListener(v->{android.view.inputmethod.InputMethodManager input=c.getSystemService(android.view.inputmethod.InputMethodManager.class);if(input!=null)input.showInputMethodPicker();});addView(language,new LayoutParams(dp(28),dp(XpMetrics.ROW)));
        removable=new TrayIcon(c,4);removable.setContentDescription("Removable storage connected");removable.setTooltipText("Removable storage — open Android Storage settings");removable.setFocusable(true);removable.setBackground(Aero.selection(c));removable.setOnClickListener(v->host.action("android:storage"));addView(removable,new LayoutParams(dp(28),dp(XpMetrics.ROW)));
        compactStatus=new Chevron(c);compactStatus.setGravity(Gravity.CENTER);compactStatus.setContentDescription("Device controls");compactStatus.setFocusable(true);compactStatus.setBackground(Aero.selection(c));compactStatus.setOnClickListener(v->{expanded=!expanded;prefs.edit().putBoolean("tray_expanded",expanded).apply();updateIconVisibility();host.layout();});ContextMenus.bind(compactStatus,(x,y)->menu(compactStatus,"controls",x,y));addView(compactStatus,0,new LayoutParams(dp(28),dp(XpMetrics.ROW)));updateIconVisibility();ContextMenus.bind(removable,(x,y)->menu(removable,"removable",x,y));ContextMenus.bind(language,(x,y)->menu(language,"language",x,y));ContextMenus.bind(taskManager,(x,y)->menu(taskManager,"taskmanager",x,y));
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        sizeIcons();
        super.onMeasure(widthSpec,heightSpec);
    }
    void fitWidth(int workspaceWidth){workspaceBudget=workspaceWidth;sizeIcons();}
    private void sizeIcons(){
        int configured=dp(getResources().getConfiguration().screenWidthDp);
        // Use one budget in measure and layout, including narrower inset/IME transitions.
        int workspaceWidth=workspaceBudget>0?Math.min(workspaceBudget,configured):configured;
        // Leave room for Start, the clock, and part of the scrolling task strip.
        int width = Math.min(dp(28), Math.max(dp(22), (workspaceWidth - dp(200)) / getChildCount()));
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childWidth = !expanded && child == compactStatus ? dp(28) : width;
            ViewGroup.LayoutParams params = child.getLayoutParams();
            if (params.width != childWidth) {
                params.width = childWidth;
                child.setLayoutParams(params);
            }
        }
        int horizontalPadding = Math.max(0, (width - dp(28)) / 2);
        if (taskManager.getPaddingLeft() != horizontalPadding) {
            taskManager.setPadding(horizontalPadding, dp(9), horizontalPadding, dp(9));
        }
    }
    void clockNow(){if(compactStatus!=null){compactStatus.setTextSize(16);compactStatus.setText(expanded?"›":"‹");compactStatus.setTooltipText(expanded?"Hide notification icons":"Show hidden notification icons");}}
    private void updateIconVisibility() {
        taskManager.setVisibility(expanded ? VISIBLE : GONE);
        actionCenter.setVisibility(GONE);
        for(int i=0;i<icons.size();i++){boolean show=prefs.getBoolean(new String[]{"tray_battery","tray_network","tray_volume","tray_brightness"}[i],true)&&(expanded||i==1||i==2);icons.get(i).setVisibility(show?VISIBLE:GONE);}
        if(language!=null){String code=Locale.getDefault().getLanguage().toUpperCase(Locale.getDefault());language.setText(code);language.setVisibility(prefs.getBoolean("tray_language",false)?VISIBLE:GONE);}
        if(removable!=null)removable.setVisibility(prefs.getBoolean("tray_removable",true)&&hasRemovableStorage()?VISIBLE:GONE);
        clockNow();
    }

    void start(){if(running)return;running=true;notices.registerOnSharedPreferenceChangeListener(alertChanges);prefs.registerOnSharedPreferenceChangeListener(alertChanges);try{IntentFilter filter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);filter.addAction(Intent.ACTION_LOCALE_CHANGED);battery=Build.VERSION.SDK_INT>=33?getContext().registerReceiver(batteryChanged,filter,Context.RECEIVER_EXPORTED):getContext().registerReceiver(batteryChanged,filter);batteryRegistered=true;}catch(RuntimeException ignored){}try{if(connections!=null){connections.registerDefaultNetworkCallback(networkChanged);networkRegistered=true;}}catch(RuntimeException ignored){}try{IntentFilter filter=new IntentFilter();for(String action:new String[]{Intent.ACTION_MEDIA_MOUNTED,Intent.ACTION_MEDIA_UNMOUNTED,Intent.ACTION_MEDIA_REMOVED,Intent.ACTION_MEDIA_EJECT})filter.addAction(action);filter.addDataScheme("file");if(Build.VERSION.SDK_INT>=33)getContext().registerReceiver(storageChanged,filter,Context.RECEIVER_EXPORTED);else getContext().registerReceiver(storageChanged,filter);storageRegistered=true;}catch(RuntimeException ignored){}update();}
    void stop(){running=false;notices.unregisterOnSharedPreferenceChangeListener(alertChanges);prefs.unregisterOnSharedPreferenceChangeListener(alertChanges);dismiss();balloon.dismiss();if(batteryRegistered){getContext().unregisterReceiver(batteryChanged);batteryRegistered=false;}if(networkRegistered){connections.unregisterNetworkCallback(networkChanged);networkRegistered=false;}if(storageRegistered){getContext().unregisterReceiver(storageChanged);storageRegistered=false;}main.removeCallbacksAndMessages(null);}
    private void refresh(){main.post(()->{if(running)update();});}
    boolean showing(){return cover!=null||balloon.showing()||commands!=null&&commands.showing();}
    void overlayInset(int value){overlayInset=value;}
    private boolean hasRemovableStorage(){try{if(storage!=null)for(android.os.storage.StorageVolume volume:storage.getStorageVolumes())if(volume.isRemovable()&&(android.os.Environment.MEDIA_MOUNTED.equals(volume.getState())||android.os.Environment.MEDIA_MOUNTED_READ_ONLY.equals(volume.getState())))return true;}catch(RuntimeException ignored){}return false;}
    private int topInset;
    void bounds(int left,int right,int top){topInset=top;bounds(left,right);}
    void bounds(int left,int right){leftInset=left;rightInset=right;balloon.bounds(left,right,topInset,overlayInset);if(cover==null)return;FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)frame.getLayoutParams();int width=Math.min(dp(page.equals("volume")?196:324),Math.max(1,workspace.getWidth()-left-right-dp(12))),height=Math.min(dp(page.equals("calendar")?395:page.equals("actioncenter")?310:page.equals("volume")?340:285),Math.max(1,workspace.getHeight()-topInset-overlayInset-dp(10))),rightMargin=right+dp(6),bottomMargin=overlayInset+dp(5);if(p.width!=width||p.height!=height||p.rightMargin!=rightMargin||p.bottomMargin!=bottomMargin){p.width=width;p.height=height;p.rightMargin=rightMargin;p.bottomMargin=bottomMargin;frame.setLayoutParams(p);}}

    boolean dismiss(){boolean had=commands!=null&&commands.showing();if(commands!=null){commands.dismiss();commands=null;}if(cover==null)return had;workspace.removeView(cover);cover=null;summary=null;volume=null;mute=null;page="";tracking=false;main.removeCallbacks(tick);return true;}
    void show(String value){
        if(value.equals("calendar")){dismiss();host.open("@clock");return;}
        if(value.equals(page)&&cover!=null){dismiss();return;}dismiss();host.opening();page=value;
        cover=new FrameLayout(getContext());cover.setElevation(dp(XpMetrics.ROW));cover.setFocusableInTouchMode(true);cover.setContentDescription("System tray flyout");cover.setOnClickListener(v->dismiss());workspace.addView(cover,new FrameLayout.LayoutParams(-1,-1));
        frame=Aero.column(getContext());frame.setBackground(XpTheme.chrome(getContext(),AeroGlass.WINDOW));Aero.pad(frame,3);frame.setClickable(true);FrameLayout.LayoutParams position=new FrameLayout.LayoutParams(dp(324),dp(285),Gravity.BOTTOM|Gravity.END);position.bottomMargin=dp(5);cover.addView(frame,position);
        LinearLayout title=new LinearLayout(getContext());TextView name=Aero.text(getContext(),value.equals("actioncenter")?"Action Center":value.equals("battery")?"Power and battery":value.equals("network")?"Network connections":value.equals("volume")?"Volume":value.equals("brightness")?"Brightness":value.equals("controls")?"Device controls":"Clock and calendar",12,XpTheme.titleInk(getContext()));Aero.bold(name);name.setPadding(dp(6),0,0,0);title.addView(name,new LayoutParams(0,dp(XpMetrics.CAPTION),1));TextView close=new WindowButton(getContext(),"×",this::dismiss);close.setContentDescription("Close tray flyout");title.addView(close,new LayoutParams(dp(30),dp(XpMetrics.CAPTION)));frame.addView(title);
        LinearLayout body=Aero.column(getContext());Aero.pad(body,8);body.setBackgroundColor(XpTheme.surface(getContext()));ScrollView scroll=new ScrollView(getContext());scroll.setFillViewport(true);scroll.addView(body);frame.addView(scroll,new LayoutParams(-1,0,1));
        summary=Aero.text(getContext(),"",13,Aero.INK);summary.setPadding(0,0,0,dp(12));summary.setContentDescription("Tray status details");body.addView(summary);
        switch(value){
            case "actioncenter":
                body.addView(new PhoneNotifications(getContext()));
                frame.setContentDescription("Action Center flyout");
                java.util.List<ActionCenter.Alert> alerts=ActionCenter.alerts(getContext(),new DesktopStore(getContext()));
                for(ActionCenter.Alert alert:alerts){
                    TextView heading=Aero.text(getContext(),alert.title,14,0xff234f75);Aero.bold(heading);body.addView(heading);
                    TextView message=Aero.text(getContext(),alert.message,12,Aero.INK);message.setMaxLines(3);message.setEllipsize(android.text.TextUtils.TruncateAt.END);body.addView(message);
                    button(body,"Review "+alert.title,()->{dismiss();host.open(alert.target);});
                }
                button(body,"Open Action Center",()->{dismiss();host.open("@actioncenter");});break;
            case "controls":
                for(String[] choice:new String[][]{{"Battery status","battery"},{"Network status","network"},{"Volume control","volume"},{"Brightness control","brightness"},{"Clock and calendar","calendar"}}){TextView link=Aero.button(getContext(),choice[0],()->show(choice[1]));link.setContentDescription(choice[0]);body.addView(link,new LayoutParams(-1,dp(XpMetrics.ROW)));}break;
            case "brightness":brightness.content(body);break;
            case "battery":button(body,"Power Options…",()->handoff("android:battery"));break;
            case "network":button(body,"Wireless Network Connection…",()->handoff("android:wifi"));button(body,"Network Connections…",()->handoff("android:network"));break;
            case "volume":
                volume=new XpWidgets.Vertical(getContext());volume.setContentDescription("Media volume");int max=audio==null?0:audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);volume.setMax(max);volume.setEnabled(audio!=null&&!audio.isVolumeFixed()&&max>0);volume.setProgress(audio==null?0:audio.getStreamVolume(AudioManager.STREAM_MUSIC));if(volume.getProgress()>0)restoreVolume=volume.getProgress();else restoreVolume=prefs.getInt("tray_last_volume",Math.max(1,max/2));
                volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){tracking=true;}public void onStopTrackingTouch(SeekBar b){tracking=false;update();}public void onProgressChanged(SeekBar b,int n,boolean user){if(user)setVolume(n);}});
                LinearLayout.LayoutParams slider=new LayoutParams(dp(48),dp(140));slider.gravity=Gravity.CENTER_HORIZONTAL;body.addView(volume,slider);
                mute=Aero.check(getContext(),"Mute",volume.getProgress()==0);mute.setContentDescription("Mute media");mute.setEnabled(volume.isEnabled());mute.setOnClickListener(v->toggleMute());body.addView(mute,new LayoutParams(-1,dp(34)));button(body,"Sound Properties…",()->handoff("android:sound"));break;
            case "calendar":
                TextView month=Aero.text(getContext(),"",15,0xff234f75);month.setGravity(Gravity.CENTER);MonthCalendarView[] ref=new MonthCalendarView[1];MonthCalendarView calendar=new MonthCalendarView(getContext(),()->{if(ref[0]!=null)month.setText(ref[0].title());});ref[0]=calendar;
                LinearLayout nav=new LinearLayout(getContext());nav.addView(Aero.button(getContext(),"‹",()->calendar.offset(-1)),new LayoutParams(dp(40),dp(36)));nav.addView(month,new LayoutParams(0,dp(36),1));nav.addView(Aero.button(getContext(),"›",()->calendar.offset(1)),new LayoutParams(dp(40),dp(36)));body.addView(nav);month.setText(calendar.title());body.addView(calendar,new LayoutParams(-1,dp(210)));button(body,"Today",calendar::today);button(body,"Open Calendar",()->host.open("@calendar"));button(body,"Date and time settings",()->host.action("android:date"));break;
        }
        bounds(leftInset,rightInset);cover.bringToFront();update();if(running)main.postDelayed(tick,1000);
    }
    private void button(LinearLayout body,String title,Runnable action){TextView button=Aero.button(getContext(),title,action);button.setContentDescription(title);LayoutParams p=new LayoutParams(-1,dp(XpMetrics.ROW));p.topMargin=dp(6);body.addView(button,p);}
    private void setVolume(int level){if(audio==null||audio.isVolumeFixed())return;try{audio.setStreamVolume(AudioManager.STREAM_MUSIC,Math.max(0,Math.min(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),level)),0);update();}catch(SecurityException ex){Toast.makeText(getContext(),"Use Sound settings to change this volume",Toast.LENGTH_SHORT).show();}}
    private int percent(){int level=battery==null?-1:battery.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),scale=battery==null?0:battery.getIntExtra(BatteryManager.EXTRA_SCALE,100);return level<0||scale<1?-1:Math.min(100,Math.max(0,Math.round(level*100f/scale)));}
    private boolean charging(){int status=battery==null?0:battery.getIntExtra(BatteryManager.EXTRA_STATUS,0);return status==BatteryManager.BATTERY_STATUS_CHARGING||status==BatteryManager.BATTERY_STATUS_FULL;}
    private String network(){
        try{NetworkCapabilities caps=connections==null?null:connections.getNetworkCapabilities(connections.getActiveNetwork());if(caps==null)return "Not connected";String transport=caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?"VPN":caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"Mobile network":caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)?"Ethernet":"Network";return transport+" • "+(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"Internet available":caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)?"Internet not verified":"Local connection");}catch(SecurityException ex){return "Open Network settings for connection details";}
    }
    private void update(){
        int count=ActionCenter.alerts(getContext(),new DesktopStore(getContext())).size();
        if(count!=alertCount){alertCount=count;actionCenter.invalidate();actionCenter.setTooltipText(count==0?"Action Center: no recent Nth alerts":"Action Center: "+count+" Nth alert"+(count==1?"":"s"));if(Build.VERSION.SDK_INT>=30)actionCenter.setStateDescription(count==0?"No recent Nth alerts":count+" Nth alert"+(count==1?"":"s"));}
        String connection=network();networkOnline=!connection.equals("Not connected")&&!connection.startsWith("Open Network");networkLimited=connection.contains("Local connection")||connection.contains("Internet not verified");volumeMuted=audio!=null&&audio.getStreamVolume(AudioManager.STREAM_MUSIC)==0;updateIconVisibility();if(!prefs.getBoolean("tray_balloons",true))balloon.dismiss();for(TrayIcon icon:icons)icon.invalidate();int percentage=percent();icons.get(0).setContentDescription("Battery status"+(percentage<0?"":", "+percentage+"%"));icons.get(1).setContentDescription("Network status");icons.get(0).setTooltipText(percentage<0?"Power status":percentage+"% remaining"+(charging()?" (plugged in)":""));icons.get(1).setTooltipText(connection);icons.get(2).setTooltipText(volumeMuted?"Volume: muted":"Volume control");icons.get(3).setTooltipText("Display brightness");
        if(charging()||percentage>20)lowBatteryShown=false;
        if(running&&percentage>=0&&percentage<=15&&!charging()&&!lowBatteryShown&&prefs.getBoolean("tray_balloons",true)){lowBatteryShown=true;balloon.show("Low Battery",percentage+"% remaining. Connect your device to power.",()->show("battery"));}
        if(summary==null)return;
        switch(page){case "actioncenter":summary.setText(alertCount==0?"No recent Nth alerts.":alertCount+" Nth alert"+(alertCount==1?"":"s"));break;case "battery":summary.setText((percentage<0?"Battery level unavailable":percentage+"% remaining")+"\n"+(charging()?"Plugged in":"On battery")+"\nBattery Saver: "+(power!=null&&power.isPowerSaveMode()?"On":"Off"));break;case "network":summary.setText(network());break;case "volume":int current=audio==null?0:audio.getStreamVolume(AudioManager.STREAM_MUSIC),max=audio==null?0:audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);summary.setText("Media volume: "+current+" / "+max+(audio!=null&&audio.isVolumeFixed()?"\nOutput device controls volume":""));if(volume!=null&&!tracking)volume.setProgress(current);if(mute!=null)mute.setChecked(current==0);break;case "brightness":summary.setText("Screen brightness");break;case "controls":summary.setText(java.text.DateFormat.getDateTimeInstance().format(new Date())+"\n"+(percentage<0?"":percentage+"% battery • ")+network());break;case "calendar":summary.setText(java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(new Date())+"\n"+TimeZone.getDefault().getDisplayName());break;}
    }
    private int dp(float n){return Aero.dp(getContext(),n);}
    private final class TrayFlag extends View {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        TrayFlag(Context context){super(context);}
        @Override protected void onDraw(Canvas canvas){
            float d=dp(22)/24f;int save=canvas.save();canvas.translate((getWidth()-dp(22))/2f,(getHeight()-dp(22))/2f);canvas.scale(d,d);
            paint.setColor(0xdd203348);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3);canvas.drawLine(5,3,5,22,paint);
            paint.setStrokeWidth(1.5f);paint.setColor(Color.WHITE);canvas.drawLine(5,3,5,22,paint);
            Path flag=new Path();flag.moveTo(6,4);flag.cubicTo(11,1,15,7,21,4);flag.lineTo(21,14);flag.cubicTo(15,17,11,11,6,14);flag.close();paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(7,3,21,15,0xfff9fcff,0xffb2c3d1,Shader.TileMode.CLAMP));canvas.drawPath(flag,paint);paint.setShader(null);
            if(alertCount>0){paint.setColor(0xffecb23d);canvas.drawCircle(20,20,4,paint);paint.setColor(0xff614113);paint.setStrokeWidth(1.5f);canvas.drawLine(20,17.5f,20,20,paint);canvas.drawPoint(20,22,paint);}
            canvas.restoreToCount(save);
        }
    }
    private final class TrayIcon extends View {
        final int kind;
        TrayIcon(Context c,int kind){super(c);this.kind=kind;}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);int save=canvas.save();float size=dp(16);canvas.translate((getWidth()-size)/2,(getHeight()-size)/2);canvas.scale(size/16,size/16);XpTrayArtwork.draw(canvas,kind,percent(),charging(),networkOnline,volumeMuted,networkLimited);canvas.restoreToCount(save);}
    }
    private final class Chevron extends TextView {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Chevron(Context c){super(c);setTypeface(Aero.face(c));setTextSize(14);}
        @Override protected void onDraw(Canvas canvas){float x=getWidth()/2f,y=getHeight()/2f,r=dp(8);p.setStyle(Paint.Style.FILL);int[] colors=XpTheme.palette(getContext());p.setShader(new LinearGradient(0,y-r,0,y+r,XpTheme.scheme(getContext())==5?new int[]{Color.WHITE,0xffd4d0c8,0xffa0a0a0}:new int[]{colors[0],colors[1],colors[2]},null,Shader.TileMode.CLAMP));canvas.drawCircle(x,y,r,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(isHovered()||isFocused()?0xffffdd79:XpTheme.scheme(getContext())==5?Color.WHITE:0xffa6ceff);canvas.drawCircle(x,y,r,p);p.setColor(XpTheme.taskbarInk(getContext()));p.setStrokeWidth(dp(1.5f));float dir=expanded?1:-1;Path arrow=new Path();arrow.moveTo(x-dir*dp(2),y-dp(3));arrow.lineTo(x+dir*dp(1),y);arrow.lineTo(x-dir*dp(2),y+dp(3));canvas.drawPath(arrow,p);}
    }
    private void handoff(String id){dismiss();host.action(id);}
    private void toggleMute(){if(audio==null||audio.isVolumeFixed())return;int now=audio.getStreamVolume(AudioManager.STREAM_MUSIC);if(now>0){restoreVolume=now;prefs.edit().putInt("tray_last_volume",now).apply();setVolume(0);}else setVolume(Math.max(1,restoreVolume));}
    private void menu(View anchor,String target,float x,float y){
        dismiss();host.opening();commands=new DesktopContextMenu(getContext(),"XP notification menu: "+target,32,246);
        switch(target){
            case "volume":commands.item("Open Volume Control","empty",()->show("volume"));commands.item("Adjust Audio Properties","empty",()->handoff("android:sound"));commands.separator();commands.checked("Mute",audio!=null&&audio.getStreamVolume(AudioManager.STREAM_MUSIC)==0,this::toggleMute);break;
            case "network":commands.item("Status","empty",()->show("network"));commands.item("Open Network Connections","empty",()->handoff("android:network"));commands.item("View Available Wireless Networks","empty",()->handoff("android:wifi"));break;
            case "battery":commands.item("Power Options","empty",()->handoff("android:battery"));commands.item("Power Status","empty",()->show("battery"));break;
            case "brightness":commands.item("Display Brightness","empty",()->show("brightness"));commands.item("Display Properties","empty",()->host.open("setting:page:personalization"));break;
            case "removable":commands.item("Open Removable Storage Settings","empty",()->handoff("android:storage"));break;
            case "language":commands.item("Choose Input Language","empty",()->{android.view.inputmethod.InputMethodManager input=getContext().getSystemService(android.view.inputmethod.InputMethodManager.class);if(input!=null)input.showInputMethodPicker();});break;
            case "taskmanager":commands.item("Open Task Manager","empty",()->host.open("@taskmanager"));break;
            default:commands.item("Device Controls","empty",()->show("controls"));commands.item("Adjust Date/Time","empty",()->host.open("@clock"));
        }
        commands.separator();commands.item("Customize Notifications…","empty",this::customize);commands.item("Taskbar Properties","empty",()->host.open("setting:page:taskbar"));ContextMenus.show(commands,anchor,x,y);
    }
    void customize(){
        dismiss();host.opening();String[] keys={"tray_battery","tray_network","tray_volume","tray_brightness","tray_language","tray_removable"},names={"Power","Network Connections","Volume","Brightness","Language Bar","Removable Storage"};LinearLayout rows=Aero.column(getContext());rows.setPadding(dp(10),dp(8),dp(10),dp(8));CheckBox[] boxes=new CheckBox[keys.length];for(int i=0;i<keys.length;i++){boxes[i]=Aero.check(getContext(),names[i],prefs.getBoolean(keys[i],i!=4));boxes[i].setContentDescription("Notification icon: "+names[i]);rows.addView(boxes[i],new LayoutParams(-1,dp(36)));}ScrollView scroll=new ScrollView(getContext());scroll.addView(rows);Aero.dialog(getContext()).setTitle("Customize Notifications").setMessage("Choose the notification icons to display. Use the taskbar arrow to show or hide the extra icons.").setView(scroll).setPositiveButton("OK",(d,w)->{SharedPreferences.Editor edit=prefs.edit();for(int i=0;i<keys.length;i++)edit.putBoolean(keys[i],boxes[i].isChecked());edit.apply();updateIconVisibility();host.layout();}).setNegativeButton("Cancel",null).show();
    }
}
