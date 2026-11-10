package com.nthexperience.desktop;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Opaque XP chrome. Shell and caption text share a single palette. */
final class XpTheme {
    static final String KEY="xp_scheme";
    static final String[] NAMES={"Luna Blue","Olive Green","Silver","Royale","Zune","Windows Classic"};
    static final int[][] COLORS={
        {0xff3c9bff,0xff0054e3,0xff0039ba,0xffece9d8,0xffd3e5fa,0xffffffff},
        {0xffb2c18a,0xff61732f,0xff465a20,0xffececdd,0xffe2e8cc,0xffffffff},
        {0xfff3f4fa,0xffc5c8da,0xffadb1c8,0xffeeeef4,0xffe5e5ef,0xff172343},
        {0xff70b5f7,0xff205bab,0xff123e80,0xffebeff6,0xffdce9f7,0xffffffff},
        {0xff666666,0xff303030,0xff161616,0xffece9df,0xffdededb,0xffffffff},
        {0xff0a246a,0xff0a246a,0xff0a246a,0xffd4d0c8,0xffd4d0c8,0xffffffff}
    };
    static int scheme(Context c){int value=c.getSharedPreferences("desktop",0).getInt(KEY,0);return value>=0&&value<NAMES.length?value:0;}
    static int[] palette(Context c){return COLORS[scheme(c)];}
    static int surface(Context c){return palette(c)[3];}
    static int titleInk(Context c){return palette(c)[5];}
    static int taskbarInk(Context c){return scheme(c)==5?0xff000000:titleInk(c);}
    static void defaults(DesktopStore store){
        TaskbarRows.migrate(store.prefs);
        SharedPreferences.Editor edit=store.prefs.edit();
        if(!store.prefs.contains("xp_initialized")){
            edit.putBoolean("xp_initialized",true);
            if(!store.prefs.contains("wallpaper")&&!store.prefs.contains("wallpaper_photo")&&!store.prefs.contains(DesktopBackgrounds.SLIDES))edit.putInt("wallpaper",XpWallpapers.FIRST);
            if(!store.prefs.contains(KEY))edit.putInt(KEY,0);
            if(!store.prefs.contains("taskbar_show_labels"))edit.putBoolean("taskbar_show_labels",true);
            if(!store.prefs.contains("taskbar_grouping"))edit.putString("taskbar_grouping","When full");
            if(!store.prefs.contains("explorer_menu_bar"))edit.putBoolean("explorer_menu_bar",true);
            if(!store.prefs.contains("sidebar_visible"))edit.putBoolean("sidebar_visible",false);
            if(!store.prefs.contains("gadgets"))edit.putString("gadgets","[]");
            if(!store.prefs.contains("start_pins"))edit.putString("start_pins","[\"@browser\",\"@files\",\"@notes\",\"@mediaplayer\"]");
            edit.apply();
        }
    }
    static void apply(DesktopStore store,int scheme){
        if(scheme<0||scheme>=NAMES.length)return;
        store.prefs.edit().putInt(KEY,scheme).apply();
    }
    static Drawable chrome(Context c,int style){return new Chrome(c,style);}
    private static final class Chrome extends Drawable {
        final Context context;final int style;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Chrome(Context context,int style){this.context=context;this.style=style;}
        @Override public void draw(Canvas canvas){
            Rect b=getBounds();float d=context.getResources().getDisplayMetrics().density;
            int[] c=palette(context);boolean classic=scheme(context)==5;
            p.setShader(null);p.setColor(style==AeroGlass.WINDOW?c[2]:c[1]);canvas.drawRect(b,p);
            if(classic&&style==AeroGlass.TASKBAR){p.setColor(c[3]);canvas.drawRect(b,p);p.setColor(0xffffffff);canvas.drawRect(b.left,b.top,b.right,b.top+d,p);return;}
            float caption=Math.min(b.bottom,b.top+50*d);
            p.setShader(new LinearGradient(0,b.top,0,style==AeroGlass.WINDOW?caption:b.bottom,
                    new int[]{c[0],c[1],c[1],c[2]},new float[]{0,.15f,.72f,1},Shader.TileMode.CLAMP));
            canvas.drawRect(b,p);p.setShader(null);
            if(style==AeroGlass.WINDOW){p.setColor(surface(context));canvas.drawRect(b.left+3*d,caption,b.right-3*d,b.bottom-3*d,p);}
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d);p.setColor(classic?Color.WHITE:0xffa5c5ff);
            canvas.drawLine(b.left+d,b.top+d,b.right-d,b.top+d,p);
            p.setStyle(Paint.Style.FILL);
        }
        @Override public void setAlpha(int alpha){p.setAlpha(alpha);invalidateSelf();}
        @Override public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);invalidateSelf();}
        @Override public int getOpacity(){return PixelFormat.OPAQUE;}
    }
    private XpTheme(){}
}
