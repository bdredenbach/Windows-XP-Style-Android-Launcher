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
        store.prefs.edit().putBoolean("sidebar_visible",false).putBoolean("taskbar_previews",false).apply();
        if(!store.prefs.contains("desktop_icon_percent")&&!store.prefs.contains("desktop_icon_size"))store.prefs.edit().putInt("desktop_icon_percent",78).apply();
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
            if(!store.prefs.contains("wallpaper_position"))edit.putString("wallpaper_position","Stretch");
            if(!store.prefs.contains("start_pins"))edit.putString("start_pins","[\"@browser\",\"@files\",\"@notes\",\"@mediaplayer\"]");
            edit.apply();
        }
    }
    static void apply(DesktopStore store,int scheme){
        if(scheme<0||scheme>=NAMES.length)return;
        store.prefs.edit().putInt(KEY,scheme).apply();
    }
    static Drawable chrome(Context c,int style){return new Chrome(c,style,true);}
    static Drawable chrome(Context c,int style,boolean active){return new Chrome(c,style,active);}
    static Drawable caption(Context c){return new Drawable(){final Paint ink=new Paint(Paint.ANTI_ALIAS_FLAG);public void draw(Canvas canvas){Rect b=getBounds();int[] colors=palette(c);boolean classic=scheme(c)==5;ink.setShader(classic?new LinearGradient(b.left,0,b.right,0,0xff0a246a,0xffa6caf0,Shader.TileMode.CLAMP):new LinearGradient(0,b.top,0,b.bottom,new int[]{colors[0],colors[1],colors[1],colors[2]},null,Shader.TileMode.CLAMP));canvas.drawRect(b,ink);ink.setShader(null);}public void setAlpha(int alpha){ink.setAlpha(alpha);}public void setColorFilter(ColorFilter filter){ink.setColorFilter(filter);}public int getOpacity(){return PixelFormat.OPAQUE;}};}
    private static final class Chrome extends Drawable {
        final Context context;final int style;final boolean active;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Chrome(Context context,int style,boolean active){this.context=context;this.style=style;this.active=active;}
        @Override public void draw(Canvas canvas){
            Rect b=getBounds();float d=context.getResources().getDisplayMetrics().density;
            int scheme=scheme(context);int[] c=palette(context);boolean classic=scheme==5;
            p.setShader(null);p.setStyle(Paint.Style.FILL);
            if(classic){
                p.setColor(c[3]);canvas.drawRect(b,p);
                p.setColor(Color.WHITE);canvas.drawRect(b.left,b.top,b.right,b.top+d,p);canvas.drawRect(b.left,b.top,b.left+d,b.bottom,p);
                p.setColor(0xff808080);canvas.drawRect(b.right-d,b.top,b.right,b.bottom,p);canvas.drawRect(b.left,b.bottom-d,b.right,b.bottom,p);
                if(style==AeroGlass.START){p.setColor(0xff0a246a);canvas.drawRect(b,p);}
                if(style==AeroGlass.WINDOW){p.setShader(new LinearGradient(b.left,0,b.right,0,active?0xff0a246a:0xff808080,active?0xffa6caf0:0xffc0c0c0,Shader.TileMode.CLAMP));canvas.drawRect(b.left+3*d,b.top+2*d,b.right-3*d,b.top+(XpMetrics.CAPTION+2)*d,p);p.setShader(null);}return;
            }
            if(style==AeroGlass.START){p.setColor(c[1]);canvas.drawRect(b,p);int[] header=scheme==0?new int[]{0xff1866c8,0xff378ee8,0xff216ac8}:new int[]{c[1],c[0],c[1]};p.setShader(new LinearGradient(0,b.top,0,b.top+58*d,header,null,Shader.TileMode.CLAMP));canvas.drawRect(b.left,b.top,b.right,Math.min(b.bottom,b.top+58*d),p);p.setShader(null);return;}
            float caption=Math.min(b.bottom,b.top+(XpMetrics.CAPTION+2)*d);
            int[] colors;float[] stops=null;
            if(style==AeroGlass.WINDOW&&scheme==0){colors=active?new int[]{0xff0997ff,0xff0053ee,0xff0050ee,0xff0066ff,0xff003dd7}:new int[]{0xff9bbbe9,0xff7a96df,0xff7a96df,0xff7aa4e6,0xff5c7ed0};stops=new float[]{0,.08f,.4f,.9f,1};}
            else if(style==AeroGlass.TASKBAR&&scheme==0){colors=new int[]{0xff3168d5,0xff4993e6,0xff245edc,0xff2257d4,0xff2050c4};stops=new float[]{0,.08f,.2f,.8f,1};}
            else colors=new int[]{c[0],c[1],c[1],c[2]};
            p.setShader(new LinearGradient(0,b.top,0,style==AeroGlass.WINDOW?caption:b.bottom,colors,stops,Shader.TileMode.CLAMP));
            if(style==AeroGlass.WINDOW){
                Path shape=new Path();shape.addRoundRect(new RectF(b),new float[]{7*d,7*d,7*d,7*d,0,0,0,0},Path.Direction.CW);canvas.drawPath(shape,p);p.setShader(null);
                p.setColor(surface(context));canvas.drawRect(b.left+3*d,caption,b.right-3*d,b.bottom-3*d,p);
                int[] edges=scheme==0?new int[]{0xff0831d9,0xff166aee,0xff0855dd,0xff00138c,0xff001ea0,0xff003bda}:new int[]{c[2],c[1],c[0],c[2],c[1],c[0]};
                for(int i=0;i<3;i++){p.setColor(edges[i]);canvas.drawRect(b.left+i*d,b.top+7*d,b.left+(i+1)*d,b.bottom-i*d,p);p.setColor(edges[i+3]);canvas.drawRect(b.right-(i+1)*d,b.top+7*d,b.right-i*d,b.bottom-i*d,p);canvas.drawRect(b.left+i*d,b.bottom-(i+1)*d,b.right-i*d,b.bottom-i*d,p);}
            }else{canvas.drawRect(b,p);p.setShader(null);}
            p.setColor(scheme==0?0xff57a9ff:c[0]);canvas.drawRect(b.left+3*d,b.top+d,b.right-3*d,b.top+2*d,p);
        }
        @Override public void setAlpha(int alpha){p.setAlpha(alpha);invalidateSelf();}
        @Override public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);invalidateSelf();}
        @Override public int getOpacity(){return PixelFormat.OPAQUE;}
    }
    private XpTheme(){}
}
