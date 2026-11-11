package com.nthexperience.desktop;

import android.content.*;
import android.media.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.util.*;

/** Foreground-only recording, explicitly started by the user. */
final class XpSoundRecorder extends XpToolPanel {
    private MediaRecorder recorder;private MediaPlayer player;private long started;private boolean recording;
    private final File clip;private final TextView clock;private final ProgressBar meter;private final Handler main=new Handler(Looper.getMainLooper());
    private final Runnable tick=new Runnable(){public void run(){if(closed||!recording)return;long seconds=(SystemClock.elapsedRealtime()-started)/1000;clock.setText(String.format(Locale.ROOT,"%02d:%02d",seconds/60,seconds%60));try{meter.setProgress(Math.min(100,recorder.getMaxAmplitude()*100/32768));}catch(RuntimeException ignored){}main.postDelayed(this,100);}};
    XpSoundRecorder(Context c,DesktopStore store,Host host){super(c,store,host,"@recorder");clip=new File(c.getFilesDir(),"xp-sound-recording.m4a");command("Record",this::record);command("Stop",this::stop);command("Play",this::play);command("Save As…",this::save);
        LinearLayout body=column();clock=text("00:00",36);clock.setGravity(android.view.Gravity.CENTER);clock.setTypeface(android.graphics.Typeface.MONOSPACE);body.addView(clock,new LayoutParams(-1,dp(72)));meter=new ProgressBar(c,null,android.R.attr.progressBarStyleHorizontal);meter.setMax(100);meter.setContentDescription("Recording microphone level");body.addView(meter,new LayoutParams(-1,dp(30)));body.addView(text("Press Record to use the microphone.\nStop, preview, then save an M4A recording.\nRecording stops when you leave Nth Desktop.\nMaximum length: 10 minutes / 8 MB.",13));scroll(body);say(clip.exists()?"Previous recording available":"Ready");}
    private void record(){if(recording)return;if(!host.microphone()){host.requestMicrophone();say("Allow microphone access, then press Record");return;}if(clip.exists()&&clip.length()>0){Aero.dialog(getContext()).setTitle("New recording").setMessage("Replace the previous recording? Save a copy first to keep it.").setPositiveButton("Record",(d,w)->begin()).setNegativeButton("Cancel",null).show();}else begin();}
    private void begin(){stop();try{recorder=new MediaRecorder();recorder.setAudioSource(MediaRecorder.AudioSource.MIC);recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);recorder.setAudioSamplingRate(44100);recorder.setAudioEncodingBitRate(96000);recorder.setMaxDuration(600000);recorder.setMaxFileSize(XpDocuments.LIMIT-65536);recorder.setOutputFile(clip.getAbsolutePath());recorder.setOnInfoListener((r,what,extra)->{if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED||what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)stop();});recorder.setOnErrorListener((r,what,extra)->{stop();say("Recording stopped; check your microphone");});recorder.prepare();recorder.start();started=SystemClock.elapsedRealtime();recording=true;main.post(tick);say("Recording microphone audio…");}catch(Exception e){if(recorder!=null){recorder.release();recorder=null;}recording=false;say("Microphone unavailable: "+e.getMessage());}}
    void stop(){main.removeCallbacks(tick);if(recorder!=null){try{if(recording)recorder.stop();}catch(RuntimeException invalid){clip.delete();}finally{recorder.release();recorder=null;}}recording=false;meter.setProgress(0);if(player!=null){player.release();player=null;}say(clip.exists()?"Recording ready • Save As to export":"Ready");}
    private void play(){stop();if(!clip.exists()){say("Record something first");return;}try{player=new MediaPlayer();player.setDataSource(clip.getAbsolutePath());player.setOnCompletionListener(p->{stop();say("Playback finished");});player.prepare();player.start();say("Playing recording");}catch(Exception e){stop();say("This recording could not be played");}}
    private void save(){stop();try(FileInputStream in=new FileInputStream(clip)){host.export(id,"Recording-"+new java.text.SimpleDateFormat("yyyyMMdd-HHmmss",Locale.ROOT).format(new Date())+".m4a","audio/mp4",XpDocuments.read(in,XpDocuments.LIMIT));}catch(Exception e){say("Record something first");}}
    @Override void active(boolean value){if(!value)stop();}
    @Override void close(){if(closed)return;stop();closed=true;main.removeCallbacksAndMessages(null);}
}
