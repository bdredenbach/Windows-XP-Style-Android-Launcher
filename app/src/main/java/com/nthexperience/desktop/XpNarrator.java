package com.nthexperience.desktop;

import android.content.*;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.*;
import java.util.*;

final class XpNarrator extends XpToolPanel {
    private TextToSpeech speech;private EditText editor;private boolean ready;private float rate=1;
    XpNarrator(Context c,DesktopStore store,Host host){super(c,store,host,"@narrator");command("Read",this::read);command("Stop",this::stop);command("Read Nth Window",()->{String text=host.windowText(this);if(text.isEmpty())say("Open a Nth window with text first");else{editor.setText(text);read();}});command("Speed",()->{String[] names={"Slow","Normal","Fast","Very fast"};Aero.dialog(c).setTitle("Speech speed").setItems(names,(d,w)->{rate=new float[]{.65f,1,1.4f,1.8f}[w];if(speech!=null)speech.setSpeechRate(rate);}).show();});command("Voice settings",()->{try{c.startActivity(new Intent("com.android.settings.TTS_SETTINGS"));}catch(RuntimeException e){say("Text-to-speech settings are unavailable");}});
        editor=new EditText(c);editor.setTextColor(android.graphics.Color.BLACK);editor.setBackgroundColor(android.graphics.Color.WHITE);editor.setTextSize(15);editor.setGravity(Gravity.TOP);editor.setHint("Type or paste text to read aloud");editor.setContentDescription("Narrator text");editor.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(8000)});content.addView(editor,new FrameLayout.LayoutParams(-1,-1));say("Loading Android speech engine…");
        speech=new TextToSpeech(c,result->post(()->{if(closed||speech==null)return;int language=result==TextToSpeech.SUCCESS?speech.setLanguage(Locale.getDefault()):TextToSpeech.LANG_NOT_SUPPORTED;ready=result==TextToSpeech.SUCCESS&&language>=0;say(ready?"Ready • reads Nth text using Android voices":"A speech voice is unavailable. Check Voice settings.");}));
    }
    private void read(){if(!ready||speech==null){say("A speech voice is unavailable. Check Voice settings.");return;}String text=editor.getText().toString();if(text.isEmpty())return;speech.setSpeechRate(rate);int result=speech.speak(text,TextToSpeech.QUEUE_FLUSH,null,"nth-xp-narrator");say(result==TextToSpeech.SUCCESS?"Reading text • Stop to cancel":"Speech could not start. Check Voice settings.");}
    private void stop(){if(speech!=null)speech.stop();say(ready?"Ready":"Speech engine unavailable");}
    @Override void active(boolean value){if(!value)stop();}
    @Override void close(){if(closed)return;closed=true;if(speech!=null){speech.stop();speech.shutdown();speech=null;}}
}
