package com.nthexperience.desktop;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

/** XP's five property sheets, with an unapplied draft shared by all tabs. */
final class XpDisplayProperties extends LinearLayout {
    static final String[] TABS={"Themes","Desktop","Screen Saver","Appearance","Settings"};
    private final DesktopStore store;
    private final JSONObject draft;
    private final VistaShell.Actions actions;
    private final Runnable close;
    private final LinearLayout tabs,body;
    private final TextView apply;
    private String tab;
    private AeroWallpaper preview;

    XpDisplayProperties(Context context,DesktopStore store,JSONObject draft,String initial,VistaShell.Actions actions,Runnable close){
        super(context);this.store=store;this.draft=draft;this.actions=actions;this.close=close;
        setOrientation(VERTICAL);setBackgroundColor(XpTheme.surface(context));setPadding(dp(8),dp(8),dp(8),dp(6));
        setContentDescription("XP Display Properties");
        if(!draft.has("selected"))put("selected",new JSONArray(DesktopBackgrounds.current(store.prefs)));
        if(!draft.has("position"))put("position",store.prefs.getString("wallpaper_position","Fill"));
        if(!draft.has("background"))put("background",store.prefs.getInt("wallpaper_bg",0));
        if(!draft.has("xpScheme"))put("xpScheme",XpTheme.scheme(context));
        tab=draft.optString("xpTab",initial);if(!Arrays.asList(TABS).contains(tab))tab="Themes";
        HorizontalScrollView bar=new HorizontalScrollView(context);bar.setHorizontalScrollBarEnabled(false);
        tabs=new LinearLayout(context);bar.addView(tabs);addView(bar,new LayoutParams(-1,dp(40)));
        ScrollView scroll=new ScrollView(context);scroll.setFillViewport(true);body=Aero.column(context);body.setPadding(dp(12),dp(12),dp(12),dp(12));
        body.setBackground(Aero.panel(context,XpTheme.surface(context),XpTheme.surface(context),1,0xffaaa69c));scroll.addView(body);
        addView(scroll,new LayoutParams(-1,0,1));
        LinearLayout footer=new LinearLayout(context);footer.setGravity(Gravity.END);footer.setPadding(0,dp(6),0,0);
        button(footer,"OK",()->commit(true));button(footer,"Cancel",()->{clear();close.run();});apply=button(footer,"Apply",()->commit(false));addView(footer);
        render();
    }
    String selectedTab(){return tab;}
    private void put(String key,Object value){GadgetLayout.put(draft,key,value);}
    private void clear(){Iterator<String> keys=draft.keys();while(keys.hasNext()){keys.next();keys.remove();}}
    private int scheme(){return Math.max(0,Math.min(XpTheme.NAMES.length-1,draft.optInt("xpScheme",0)));}
    private List<String> selected(){List<String> keys=new ArrayList<>();JSONArray values=draft.optJSONArray("selected");
        if(values!=null)for(int i=0;i<Math.min(values.length(),DesktopBackgrounds.LIMIT);i++){String value=values.optString(i);if(DesktopBackgrounds.valid(value)&&!keys.contains(value))keys.add(value);}return keys;}
    private void render(){
        if(preview!=null){preview.close();preview=null;}
        tabs.removeAllViews();for(String name:TABS){TextView item=Aero.text(getContext(),name,12,Aero.INK);item.setGravity(Gravity.CENTER);item.setPadding(dp(10),0,dp(10),0);item.setFocusable(true);item.setSelected(name.equals(tab));item.setContentDescription("Display tab: "+name);item.setBackground(Aero.panel(getContext(),name.equals(tab)?0xffffffff:XpTheme.surface(getContext()),XpTheme.surface(getContext()),2,0xffaaa69c));item.setOnClickListener(v->{tab=name;put("xpTab",tab);render();});tabs.addView(item,new LayoutParams(-2,dp(38)));}
        body.removeAllViews();
        switch(tab){case "Themes":themes();break;case "Desktop":desktop();break;case "Appearance":appearance();break;case "Screen Saver":screenSaver();break;default:settings();}
        apply.setEnabled(changed());apply.setAlpha(apply.isEnabled()?1:.45f);
    }
    private void themes(){
        label("A theme gives your desktop a consistent look.");
        label("Theme:");selector(XpTheme.NAMES,scheme(),"XP theme",value->{put("xpScheme",value);render();});monitor();
        label("Changes stay in this window until you choose Apply or OK.");
        button(body,"Save applied theme…",()->actions.action("theme-save"));button(body,"Import theme…",()->actions.action("theme-import"));
        JSONArray saved=ThemeProfiles.saved(store);for(int i=0;i<saved.length();i++){JSONObject profile=saved.optJSONObject(i);if(profile!=null){int index=i;button(body,profile.optString("name","Saved theme"),()->actions.action("theme-preview:"+index));}}
    }
    private void desktop(){
        monitor();label("Background:");List<String> keys=new ArrayList<>(),names=new ArrayList<>();
        for(int i=XpWallpapers.FIRST;i<DesktopBackgrounds.NAMES.length;i++){keys.add(DesktopBackgrounds.stock(i));names.add(DesktopBackgrounds.NAMES[i]);}
        for(int i=0;i<XpWallpapers.FIRST;i++){keys.add(DesktopBackgrounds.stock(i));names.add(DesktopBackgrounds.NAMES[i]);}
        List<String> selected=selected();for(String key:selected)if(DesktopBackgrounds.stockId(key)<0&&!keys.contains(key)){keys.add(key);names.add("My Picture "+(names.size()-DesktopBackgrounds.NAMES.length+1));}
        ListView list=new ListView(getContext());list.setContentDescription("Desktop background list");list.setBackgroundColor(Color.WHITE);list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setAdapter(new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_single_choice,names));
        int index=selected.isEmpty()?-1:keys.indexOf(selected.get(0));if(index>=0){list.setItemChecked(index,true);list.setSelection(index);}
        list.setOnItemClickListener((parent,item,position,id)->{put("selected",new JSONArray(Collections.singletonList(keys.get(position))));render();});body.addView(list,new LayoutParams(-1,dp(148)));
        if(selected.size()>1)label(selected.size()+" pictures in your current slide show. Choosing one background replaces the slide show.");
        button(body,"Browse…",()->actions.action("wallpaper-browse"));
        label("Position:");selector(BackgroundPicker.POSITIONS,Math.max(0,Arrays.asList(BackgroundPicker.POSITIONS).indexOf(draft.optString("position","Fill"))),"Wallpaper position",value->{put("position",BackgroundPicker.POSITIONS[value]);render();});
        button(body,"More pictures and slide show…",()->actions.open("setting:page:background"));
        button(body,"Customize Desktop…",()->actions.open("setting:page:icons"));
    }
    private void appearance(){
        monitor();label("Windows and buttons:");label(scheme()==5?"Windows Classic style":"Windows XP style");
        label("Color scheme:");selector(XpTheme.NAMES,scheme(),"XP color scheme",value->{put("xpScheme",value);render();});
        button(body,"Icon size and spacing…",()->actions.open("setting:page:icons"));
        button(body,"Effects and sounds…",()->actions.open("setting:page:sounds"));
        label("Android controls system font size and display scaling.");
    }
    private void screenSaver(){
        XpScreenSaver screen=new XpScreenSaver(getContext());body.addView(screen,new LayoutParams(-1,dp(152)));
        label("Screen saver: Nth Desktop XP");
        button(body,"Preview",()->Aero.dialog(getContext()).setTitle("Screen Saver Preview").setView(new XpScreenSaver(getContext())).setPositiveButton("Close",null).show());
        button(body,"Screen saver settings…",()->system(Settings.ACTION_DREAM_SETTINGS));
        label("Choose Nth Desktop XP in Android’s screen saver settings. Android controls when it starts, charging requirements and lock-screen security.");
        button(body,"Power…",()->system(Settings.ACTION_DISPLAY_SETTINGS));
    }
    private void settings(){
        android.util.DisplayMetrics metrics=getResources().getDisplayMetrics();
        monitor();label("Display: "+android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL);
        label("Current app area: "+metrics.widthPixels+" by "+metrics.heightPixels+" pixels");
        label("Display density: "+metrics.densityDpi+" dpi");
        label("Android manages resolution, color output, font size and screen timeout.");
        button(body,"Advanced display settings…",()->system(Settings.ACTION_DISPLAY_SETTINGS));
    }
    private void system(String action){try{getContext().startActivity(new Intent(action));}catch(RuntimeException unavailable){Toast.makeText(getContext(),"This setting is unavailable on this device.",Toast.LENGTH_LONG).show();}}
    private void monitor(){
        FrameLayout frame=new FrameLayout(getContext());frame.setPadding(dp(7),dp(7),dp(7),dp(16));frame.setBackground(Aero.panel(getContext(),0xff777777,0xff333333,5,0xff1c1c1c));
        List<String> keys=selected();int id=keys.isEmpty()?-1:DesktopBackgrounds.stockId(keys.get(0));
        preview=new AeroWallpaper(getContext(),id<0?store.prefs.getInt("wallpaper",AeroWallpaper.SEVEN):id,false);
        preview.preview(keys.isEmpty()?DesktopBackgrounds.stock(AeroWallpaper.SEVEN):keys.get(0),draft.optString("position","Fill"),BackgroundPicker.COLORS[Math.max(0,Math.min(5,draft.optInt("background",0)))]);
        frame.addView(preview,new FrameLayout.LayoutParams(-1,-1));WindowSample sample=new WindowSample(getContext(),scheme());sample.setContentDescription("Preview scheme: "+XpTheme.NAMES[scheme()]);if(tab.equals("Themes")||tab.equals("Appearance"))frame.addView(sample,new FrameLayout.LayoutParams(-1,-1));
        LayoutParams size=new LayoutParams(-1,dp(getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE?100:156));size.bottomMargin=dp(10);body.addView(frame,size);
    }
    private void label(String text){TextView view=Aero.text(getContext(),text,12,Aero.INK);view.setPadding(0,dp(5),0,dp(6));body.addView(view);}
    private TextView button(LinearLayout parent,String text,Runnable action){TextView button=Aero.button(getContext(),text,action);button.setTextSize(12);button.setMinWidth(0);button.setContentDescription(text);LayoutParams size=new LayoutParams(parent==body?-1:0,dp(39),parent==body?0:1);size.setMargins(dp(2),dp(3),dp(2),dp(2));parent.addView(button,size);return button;}
    private void selector(String[] names,int selected,String description,java.util.function.IntConsumer choose){Spinner spinner=new Spinner(getContext());spinner.setContentDescription(description);spinner.setAdapter(new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_dropdown_item,names));spinner.setSelection(selected);spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int position,long id){if(position!=selected)choose.accept(position);}});body.addView(spinner,new LayoutParams(-1,dp(42)));}
    private boolean changed(){return scheme()!=XpTheme.scheme(getContext())||!selected().equals(DesktopBackgrounds.current(store.prefs))||!draft.optString("position","Fill").equals(store.prefs.getString("wallpaper_position","Fill"))||draft.optInt("background",0)!=store.prefs.getInt("wallpaper_bg",0);}
    private void commit(boolean finish){
        List<String> keys=selected();if(keys.isEmpty())return;boolean newScheme=scheme()!=XpTheme.scheme(getContext());
        android.content.SharedPreferences.Editor edit=store.prefs.edit().putInt(XpTheme.KEY,scheme()).putString("wallpaper_position",draft.optString("position","Fill")).putInt("wallpaper_bg",draft.optInt("background",0));
        DesktopBackgrounds.choose(edit,keys,store.prefs.getInt("wallpaper",AeroWallpaper.SEVEN));edit.apply();actions.action("wallpaper");
        if(finish){clear();close.run();}else render();if(newScheme)actions.action("xp-theme");
    }
    void dispose(){if(preview!=null)preview.close();}
    private int dp(int value){return Aero.dp(getContext(),value);}
    private static final class WindowSample extends View {
        private final int scheme;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        WindowSample(Context c,int scheme){super(c);this.scheme=scheme;}
        @Override protected void onDraw(Canvas canvas){float d=getResources().getDisplayMetrics().density;float l=getWidth()*.15f,t=getHeight()*.22f,r=getWidth()*.87f,b=getHeight()*.8f;int[] colors=XpTheme.COLORS[scheme];paint.setColor(colors[2]);canvas.drawRect(l,t,r,b,paint);paint.setColor(colors[3]);canvas.drawRect(l+2*d,t+22*d,r-2*d,b-2*d,paint);paint.setColor(colors[1]);canvas.drawRect(l+2*d,t+2*d,r-2*d,t+22*d,paint);paint.setTextSize(11*d);paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setColor(colors[5]);canvas.drawText("Active Window",l+7*d,t+16*d,paint);paint.setColor(Color.BLACK);paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(10*d);canvas.drawText("File   Edit   View",l+7*d,t+38*d,paint);}
    }
}
