package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import java.io.InputStream;

/** Append-only IDs keep older .nthdesktop backgrounds stable across variants. */
final class XpWallpapers {
    static final int FIRST=11;
    static final String[] FILES={"bliss","ascent","autumn","azul","crystal","follow","friend","home",
            "moon-flower","peace","power","purple-flower","radiance","red-moon-desert","ripple",
            "stonehenge","tulips","vortec-space","wind","windows-xp"};
    private static final LruCache<String,Bitmap> cache=new LruCache<String,Bitmap>(8*1024*1024){
        @Override protected int sizeOf(String key,Bitmap bitmap){return bitmap.getAllocationByteCount();}
    };
    static boolean contains(int id){return id>=FIRST&&id<FIRST+FILES.length;}
    static synchronized Bitmap image(Context context,int id,boolean thumbnail){
        if(!contains(id))return null;
        String key=id+(thumbnail?":thumb":":full");Bitmap saved=cache.get(key);if(saved!=null)return saved;
        BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=thumbnail?4:1;
        options.inPreferredConfig=Bitmap.Config.RGB_565;
        try(InputStream stream=context.getAssets().open("wallpapers/xp/"+FILES[id-FIRST]+".jpg")){
            Bitmap bitmap=BitmapFactory.decodeStream(stream,null,options);
            if(bitmap!=null)cache.put(key,bitmap);return bitmap;
        }catch(java.io.IOException|OutOfMemoryError unavailable){return null;}
    }
    private XpWallpapers(){}
}
