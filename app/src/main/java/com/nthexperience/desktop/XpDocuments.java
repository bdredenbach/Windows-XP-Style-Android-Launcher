package com.nthexperience.desktop;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import android.util.AtomicFile;
import java.io.*;
import java.util.concurrent.*;
import java.lang.ref.WeakReference;

/** A staged picker operation survives Activity recreation without retaining a View. */
final class XpDocuments {
    static final int REQUEST=6200,LIMIT=8*1024*1024;
    interface Host{void opened(String id,byte[] data,String name);void message(String id,String text);}
    private final Activity activity;private final Host host;private final SharedPreferences prefs;
    private static final Handler main=new Handler(Looper.getMainLooper());private static final ExecutorService worker=Executors.newSingleThreadExecutor();
    private static WeakReference<XpDocuments> current=new WeakReference<>(null);private static volatile boolean preparing,processing;
    private final AtomicFile stage;private boolean closed,busy;
    XpDocuments(Activity a,Host host){activity=a;this.host=host;prefs=a.getSharedPreferences("xp_document_picker",0);stage=new AtomicFile(new File(a.getFilesDir(),"xp-document-export.bin"));current=new WeakReference<>(this);if("preparing".equals(prefs.getString("phase",""))&&!preparing)prefs.edit().clear().commit();busy=prefs.contains("id")||preparing||processing;main.post(this::launchReady);}
    void pick(String id,String mime){if(busy)return;prefs.edit().putString("id",id).putBoolean("save",false).commit();launch(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));}
    void export(String id,String name,String mime,byte[] bytes){
        if(busy)return;if(bytes.length>LIMIT){host.message(id,"This file exceeds the 8 MB export limit");return;}busy=true;host.message(id,"Preparing file…");
        preparing=true;prefs.edit().putString("id",id).putBoolean("save",true).putString("name",name).putString("mime",mime).putString("phase","preparing").commit();
        worker.execute(()->{try{FileOutputStream out=stage.startWrite();try{out.write(bytes);stage.finishWrite(out);}catch(Exception e){stage.failWrite(out);throw e;}prefs.edit().putString("phase","ready").commit();preparing=false;main.post(()->{XpDocuments live=current.get();if(live!=null&&!live.closed)live.launchReady();});}catch(Exception e){preparing=false;prefs.edit().clear().commit();message(id,"File could not be prepared");}});
    }
    private void launchReady(){if(closed||!"ready".equals(prefs.getString("phase","")))return;String name=prefs.getString("name","Document"),mime=prefs.getString("mime","application/octet-stream");prefs.edit().putString("phase","picker").commit();launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(mime).putExtra(Intent.EXTRA_TITLE,name));}
    private static void message(String id,String value){main.post(()->{XpDocuments live=current.get();processing=false;if(live!=null&&!live.closed){live.busy=false;live.host.message(id,value);}});}
    private void launch(Intent intent){busy=true;try{activity.startActivityForResult(intent,REQUEST);}catch(RuntimeException unavailable){busy=false;String id=prefs.getString("id","");prefs.edit().clear().apply();host.message(id,"No document picker is available");}}
    boolean result(int request,int result,Intent data){
        if(request!=REQUEST)return false;busy=false;String id=prefs.getString("id","");boolean save=prefs.getBoolean("save",false);prefs.edit().clear().commit();if(!XpPrograms.valid(id))return true;
        if(result!=Activity.RESULT_OK||data==null||data.getData()==null){host.message(id,"Canceled; your draft is kept");return true;}Uri uri=data.getData();if(!"content".equals(uri.getScheme())){host.message(id,"Choose a document provider");return true;}
        busy=true;processing=true;worker.execute(()->{try{
            if(save){try(InputStream in=stage.openRead();OutputStream out=activity.getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException("File unavailable");byte[] block=new byte[16384];int n;while((n=in.read(block))!=-1)out.write(block,0,n);}stage.delete();message(id,"File saved");}
            else{byte[] bytes;try(InputStream in=activity.getContentResolver().openInputStream(uri)){bytes=read(in,LIMIT);}String name="Document";try(Cursor cursor=activity.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(cursor!=null&&cursor.moveToFirst())name=cursor.getString(0);}String filename=name;main.post(()->{processing=false;XpDocuments live=current.get();if(live!=null&&!live.closed){live.busy=false;live.host.opened(id,bytes,filename);}});}
        }catch(Exception e){message(id,"Could not "+(save?"save":"open")+" this file: "+e.getMessage());}});return true;
    }
    static byte[] read(InputStream in,int limit)throws IOException{if(in==null)throw new IOException("File unavailable");ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] block=new byte[16384];int n;while((n=in.read(block))!=-1){if(out.size()+n>limit)throw new IOException("File is too large");out.write(block,0,n);}return out.toByteArray();}
    void close(){closed=true;if(current.get()==this)current.clear();}
}
