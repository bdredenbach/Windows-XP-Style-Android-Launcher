package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Append-only IDs, orientation-aware assets and bounded decodes for portable backups. */
final class XpWallpapers {
    static final int FIRST=11;
    static final String[] FILES={"bliss","ascent","autumn","azul","crystal","follow","friend","home",
            "moon-flower","peace","power","purple-flower","radiance","red-moon-desert","ripple",
            "stonehenge","tulips","vortec-space","wind","windows-xp"};
    private static final Set<String> PORTRAITS=new HashSet<>(Arrays.asList("bliss","ascent","autumn","azul","radiance"));
    private static final LruCache<String,Bitmap> cache=new LruCache<String,Bitmap>(24*1024*1024){
        @Override protected int sizeOf(String key,Bitmap bitmap){return bitmap.getAllocationByteCount();}
    };
    static boolean contains(int id){return id>=FIRST&&id<FIRST+FILES.length;}
    static String asset(int id,boolean portrait){
        if(!contains(id))return null;
        String name=FILES[id-FIRST];
        return "wallpapers/xp/"+name+(portrait&&PORTRAITS.contains(name)?"-portrait":"")+".jpg";
    }
    static int sample(int width,int height,boolean thumbnail){
        int sample=1,limit=thumbnail?224:2048;
        while(Math.max(width,height)/sample>limit||((long)width*height)/(sample*sample)>(thumbnail?160000:4194304))sample*=2;
        return sample;
    }
    static Bitmap image(Context c,int id,boolean thumbnail){
        android.util.DisplayMetrics m=c.getResources().getDisplayMetrics();
        return image(c,id,thumbnail,m.widthPixels,m.heightPixels);
    }
    static synchronized Bitmap image(Context context,int id,boolean thumbnail,int width,int height){
        String path=asset(id,height>width);if(path==null)return null;
        String key=path+(thumbnail?":thumb":":full");Bitmap saved=cache.get(key);if(saved!=null&&!saved.isRecycled())return saved;
        try{
            BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;
            try(InputStream stream=context.getAssets().open(path)){BitmapFactory.decodeStream(stream,null,bounds);}
            if(bounds.outWidth<1||bounds.outHeight<1)return null;
            BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=sample(bounds.outWidth,bounds.outHeight,thumbnail);options.inPreferredConfig=Bitmap.Config.RGB_565;
            try(InputStream stream=context.getAssets().open(path)){
                Bitmap bitmap=BitmapFactory.decodeStream(stream,null,options);if(bitmap!=null)cache.put(key,bitmap);return bitmap;
            }
        }catch(IOException|OutOfMemoryError unavailable){return null;}
    }
    private XpWallpapers(){}
}
