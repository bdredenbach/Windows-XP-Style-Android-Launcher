package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.media3.common.Player;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Real decoded music, animation interruptions, classic switching and both layouts. */
public final class XpExperienceSmoke extends Instrumentation {
    MainActivity home;View root;DesktopStore store;int checks;File tone;
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){Bundle out=new Bundle();try{
        Context c=getTargetContext();store=new DesktopStore(c);store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("reduce_motion",false).commit();XpTheme.defaults(store);
        c.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();c.getSharedPreferences(InitializationState.PREFS,0).edit().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();c.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(c,MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();ui(()->home.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));await(()->root.getHeight()>root.getWidth(),"Portrait layout settles");waitForIdleSync();Thread.sleep(500);
        check(android.animation.ValueAnimator.areAnimatorsEnabled(),"Native animator scale is enabled");
        ui(()->home.open("@notes"));settle();DesktopWindow notes=window("@notes");check(notes!=null,"Notepad opens");
        ui(()->{notes.minimize();check(notes.motionRunning(),"Minimize animates the caption");check(notes.getScaleX()==1&&notes.getScaleY()==1,"Live window is never scaled");});Thread.sleep(70);ui(notes::restore);settle();check(notes.getVisibility()==View.VISIBLE&&!notes.minimizing()&&!notes.motionRunning(),"Restoring interrupts minimize cleanly");
        ui(notes::minimize);settle();check(notes.getVisibility()==View.GONE,"Minimize finishes hidden");ui(notes::restore);settle();check(notes.getVisibility()==View.VISIBLE,"Restore finishes visible");ui(notes::maximize);settle();check(notes.maximized(),"Maximize settles");ui(notes::restoreDown);settle();check(!notes.maximized(),"Restore down keeps floating geometry");
        ui(()->{notes.minimize();notes.settle();});check(notes.getVisibility()==View.GONE,"Canceling a minimizing transition cannot strand an invisible task");ui(notes::restore);settle();
        ui(()->home.open("@computer"));settle();
        ui(()->home.dispatchKeyEvent(tab(false)));settle();Dialog switcher=dialog();check(switcher!=null&&switcher.isShowing(),"Alt+Tab opens icon switcher");check(find(switcher.getWindow().getDecorView(),"XP program switcher")!=null,"Classic icon grid is present");
        ui(()->switcher.dispatchKeyEvent(tab(true)));ui(()->switcher.dispatchKeyEvent(tab(false)));ui(()->switcher.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ALT_LEFT)));settle();check(!switcher.isShowing(),"Releasing Alt commits and closes the switcher");
        ui(()->{store.prefs.edit().putBoolean("taskbar_expanded",true).apply();invoke("applyTaskbarRows");});settle();shot("polish-expanded-taskbar");
        ui(()->find(root,"Start menu").performClick());settle();StartMenu menu=(StartMenu)findType(root,StartMenu.class);check(menu.getHeight()>=Math.min(Aero.dp(c,590),((View)menu.getParent()).getHeight()-Aero.dp(c,48)),"Portrait Start menu is taller");shot("polish-start-portrait");ui(menu::hide);
        tone=new File(c.getCacheDir(),"xp-polish-test.wav");makeTone(tone);MediaPlayback playback=home.mediaPlayback();List<MediaLibrary.Entry> queue=Arrays.asList(new MediaLibrary.Entry(Uri.fromFile(tone),"XP audio test","audio/wav"),new MediaLibrary.Entry(Uri.fromFile(tone),"Second track","audio/wav"));
        ui(()->{playback.player.setRepeatMode(Player.REPEAT_MODE_ALL);playback.play(queue,0);home.open(MediaLibrary.PLAYER);});
        await(()->playback.player.isPlaying()&&playback.player.getCurrentPosition()>200,"WAV decodes and plays");await(()->{for(float sample:playback.signal.samples())if(Math.abs(sample)>.05f)return true;return false;},"Visualizations receive actual decoded PCM");
        MediaPanel player=(MediaPanel)findType(root,MediaPanel.class);check(find(player,"Stop")!=null,"XP transport includes Stop");shot("polish-player-portrait");
        ui(()->home.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));await(()->root.getWidth()>root.getHeight(),"Landscape layout settles");settle();player=(MediaPanel)findType(root,MediaPanel.class);final MediaPanel panel=player;check(find(panel,"Now Playing side list").isShown(),"Wide XP layout shows Now Playing list");
        Set<Integer> visualHashes=new HashSet<>();
        for(int mode=0;mode<4;mode++){int selected=mode;ui(()->panel.visualization(selected));Thread.sleep(200);waitForIdleSync();int[] hash={0};ui(()->hash[0]=hash(findType(panel,XpMusicVisualizer.class)));visualHashes.add(hash[0]);shot("polish-visualizer-"+mode);}
        check(visualHashes.size()==4,"All four native visualizations render differently");
        ui(()->panel.visualization(0));Thread.sleep(200);XpMusicVisualizer visual=(XpMusicVisualizer)findType(panel,XpMusicVisualizer.class);int[] before={0},after={0};ui(()->before[0]=hash(visual));Thread.sleep(200);ui(()->after[0]=hash(visual));check(before[0]!=after[0],"Playing audio advances the visualization");
        ui(()->playback.player.pause());settle();ui(()->before[0]=hash(visual));Thread.sleep(200);ui(()->after[0]=hash(visual));check(before[0]==after[0],"Paused visualization remains still");
        ui(()->find(panel,"Stop").performClick());settle();check(!playback.player.getPlayWhenReady()&&playback.player.getCurrentPosition()<100,"Stop resets playback");
        ui(()->{store.prefs.edit().putBoolean("reduce_motion",true).apply();notes.minimize();check(!notes.motionRunning()&&notes.getVisibility()==View.GONE,"Reduced motion minimizes synchronously");notes.restore();});
        out.putString("result","PASS: "+checks+" XP polish checks; caption interruptions, keyboard switching, taller Start, expanded taskbar, real WAV/PCM, four visualizations, pause freeze and Stop.");finish(Activity.RESULT_OK,out);
    }catch(Throwable failure){android.util.Log.e("XpSmoke","XP polish failure",failure);try{shot("polish-failure");}catch(Exception ignored){}out.putString("result","FAIL: "+failure);finish(Activity.RESULT_CANCELED,out);}finally{if(tone!=null)tone.delete();}}
    private void makeTone(File file)throws Exception{int rate=44100,n=rate*12;ByteBuffer wav=ByteBuffer.allocate(44+n*2).order(ByteOrder.LITTLE_ENDIAN);wav.put("RIFF".getBytes("US-ASCII")).putInt(36+n*2).put("WAVEfmt ".getBytes("US-ASCII")).putInt(16).putShort((short)1).putShort((short)1).putInt(rate).putInt(rate*2).putShort((short)2).putShort((short)16).put("data".getBytes("US-ASCII")).putInt(n*2);for(int i=0;i<n;i++){double t=i/(double)rate,envelope=.25+.7*(.5+.5*Math.sin(t*8));wav.putShort((short)(Math.sin(t*2*Math.PI*(220+80*Math.sin(t*3)))*18000*envelope));}try(FileOutputStream out=new FileOutputStream(file)){out.write(wav.array());}}
    private int hash(View v){Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));int[] pixels=new int[b.getWidth()*b.getHeight()];b.getPixels(pixels,0,b.getWidth(),0,0,b.getWidth(),b.getHeight());int hash=Arrays.hashCode(pixels);b.recycle();return hash;}
    private void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
    private void ui(Runnable r){runOnMainSync(r);}
    private void settle()throws Exception{waitForIdleSync();Thread.sleep(350);}
    private void await(BooleanSupplier condition,String name)throws Exception{for(int i=0;i<100;i++){boolean[] ok={false};ui(()->ok[0]=condition.getAsBoolean());if(ok[0]){check(true,name);return;}Thread.sleep(100);}throw new AssertionError(name);}
    private KeyEvent tab(boolean backwards){return new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_TAB,0,KeyEvent.META_ALT_ON|(backwards?KeyEvent.META_SHIFT_ON:0));}
    private Dialog dialog(){try{java.lang.reflect.Field f=WindowPreviews.class.getDeclaredField("dialog");f.setAccessible(true);java.lang.reflect.Field p=MainActivity.class.getDeclaredField("previews");p.setAccessible(true);return (Dialog)f.get(p.get(home));}catch(Exception e){throw new RuntimeException(e);}}
    private void invoke(String name){try{java.lang.reflect.Method m=MainActivity.class.getDeclaredMethod(name);m.setAccessible(true);m.invoke(home);}catch(Exception e){throw new RuntimeException(e);}}
    private DesktopWindow window(String id){return (DesktopWindow)findWindow(root,id);}
    private View findWindow(View v,String id){if(v instanceof DesktopWindow&&((DesktopWindow)v).toolId.equals(id))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View result=findWindow(((ViewGroup)v).getChildAt(i),id);if(result!=null)return result;}return null;}
    private View findType(View v,Class<?> type){if(type.isInstance(v))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View result=findType(((ViewGroup)v).getChildAt(i),type);if(result!=null)return result;}return null;}
    private View find(View v,String name){if(name.equals(v.getContentDescription()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View result=find(((ViewGroup)v).getChildAt(i),name);if(result!=null)return result;}return null;}
    private void shot(String name)throws Exception{Bitmap bitmap=getUiAutomation().takeScreenshot();File dir=new File(getTargetContext().getFilesDir(),"xp-verification");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();}
}
