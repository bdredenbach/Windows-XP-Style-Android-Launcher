package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import java.util.*;

/** Bundled user-supplied XP icons. Choose an embedded ICO size before scaling. */
final class XpIconAssets {
    private static final Map<String,Bitmap> bitmaps=new HashMap<>();
    static String asset(String kind,int pixels){
        switch(kind){case "computer":case "network":case "documents":case "recycle":case "recycle-full":break;default:return null;}
        int[] sizes=kind.equals("recycle")||kind.equals("recycle-full")?new int[]{16,32,48}:new int[]{16,24,32,48};
        int selected=48;for(int size:sizes)if(size>=pixels){selected=size;break;}
        return "xp-icons/"+kind+"-"+selected+".png";
    }
    static boolean draw(Context context,Canvas canvas,String kind,int pixels){
        String asset=asset(kind,pixels);if(asset==null)return false;
        Bitmap image;
        synchronized(bitmaps){
            image=bitmaps.get(asset);
            if(image==null){try(java.io.InputStream input=context.getAssets().open(asset)){image=BitmapFactory.decodeStream(input);}catch(java.io.IOException missing){return false;}if(image==null)return false;bitmaps.put(asset,image);}
        }
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(image,null,new RectF(0,0,64,64),paint);return true;
    }
    private XpIconAssets(){}
}
