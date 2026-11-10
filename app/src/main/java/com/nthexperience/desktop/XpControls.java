package com.nthexperience.desktop;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/** Native XP control states based on the 2001 visual guidelines. */
final class XpControls {
    static StateListDrawable menuSelection(Context context){
        StateListDrawable d=new StateListDrawable();
        for(int state:new int[]{android.R.attr.state_pressed,android.R.attr.state_focused,android.R.attr.state_hovered,android.R.attr.state_selected})d.addState(new int[]{state},Aero.panel(context,0xff316ac5,0xff316ac5,0,0));
        d.addState(new int[]{},Aero.panel(context,0,0,0,0));return d;
    }
    static ColorStateList menuInk(int normal){return new ColorStateList(new int[][]{{-android.R.attr.state_enabled},{android.R.attr.state_pressed},{android.R.attr.state_focused},{android.R.attr.state_hovered},{android.R.attr.state_selected},{}},new int[]{0xff808080,Color.WHITE,Color.WHITE,Color.WHITE,Color.WHITE,normal});}
    static Drawable button(Context c){return new Control(c,0);}
    static Drawable tab(Context c){return new Control(c,1);}
    static Drawable notification(Context c,boolean edge){return new Control(c,edge?2:3);}
    private static final class Control extends Drawable {
        final Context c;final int kind;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);boolean pressed,hot,selected,disabled,focused;
        Control(Context c,int kind){this.c=c;this.kind=kind;}
        @Override public boolean isStateful(){return kind<2;}
        @Override protected boolean onStateChange(int[] states){pressed=hot=selected=focused=false;disabled=true;for(int state:states){if(state==android.R.attr.state_enabled)disabled=false;if(state==android.R.attr.state_pressed)pressed=true;if(state==android.R.attr.state_hovered)hot=true;if(state==android.R.attr.state_focused)focused=true;if(state==android.R.attr.state_selected)selected=true;}invalidateSelf();return true;}
        @Override public void draw(Canvas canvas){
            Rect b=getBounds();float d=c.getResources().getDisplayMetrics().density;
            p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAlpha(255);
            if(kind>=2){
                int scheme=XpTheme.scheme(c);int[] colors=scheme==0?new int[]{0xff19b3f2,0xff0b99ec,0xff1186df,0xff0c79d9}:scheme==1?new int[]{0xffd7e3b0,0xff9eb772,0xff8aa45c}:scheme==2?new int[]{0xfff5f5fb,0xffc9cbdc,0xffb3b7ce}:scheme==5?new int[]{0xffd4d0c8,0xffd4d0c8}:new int[]{XpTheme.palette(c)[0],XpTheme.palette(c)[1],XpTheme.palette(c)[2]};
                p.setShader(new LinearGradient(0,b.top,0,b.bottom,colors,null,Shader.TileMode.CLAMP));canvas.drawRect(b,p);p.setShader(null);
                if(kind==2){p.setColor(scheme==0?0xff0e54c0:0xff808080);canvas.drawRect(b.left,b.top,b.left+d,b.bottom,p);p.setColor(scheme==0?0xff4ed9ff:Color.WHITE);canvas.drawRect(b.left+d,b.top+d,b.left+2*d,b.bottom,p);}return;
            }
            float l=b.left+.5f*d,t=b.top+.5f*d,r=b.right-.5f*d,bottom=b.bottom-.5f*d;
            if(XpTheme.scheme(c)==5){p.setColor(0xffd4d0c8);canvas.drawRect(l,t,r,bottom,p);p.setStrokeWidth(d);p.setColor(pressed?0xff808080:Color.WHITE);canvas.drawLine(l,t,r,t,p);canvas.drawLine(l,t,l,bottom,p);p.setColor(pressed?Color.WHITE:0xff808080);canvas.drawLine(r,t,r,bottom,p);if(!(kind==1&&selected))canvas.drawLine(l,bottom,r,bottom,p);if(focused){p.setColor(Color.BLACK);p.setPathEffect(new DashPathEffect(new float[]{d,d},0));p.setStyle(Paint.Style.STROKE);canvas.drawRect(l+4*d,t+4*d,r-4*d,bottom-4*d,p);p.setPathEffect(null);p.setStyle(Paint.Style.FILL);}return;}
            int[] colors=kind==1?(selected?new int[]{0xfffcfcfe,0xfffcfcfe}:new int[]{0xffffffff,0xfff0f0ea,0xffecebe5}):disabled?new int[]{0xfff5f4ea,0xffefeee5}:pressed?new int[]{0xffcdcac3,0xffe3e3db,0xfff2f2f1}:new int[]{0xffffffff,0xffecebe5,0xffd8d0c4};
            p.setShader(new LinearGradient(0,t,0,bottom,colors,pressed?new float[]{0,.09f,1}:null,Shader.TileMode.CLAMP));canvas.drawRoundRect(l,t,r,bottom,2*d,2*d,p);p.setShader(null);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d);p.setColor(disabled?0xffc9c7ba:kind==1?0xff919b9c:0xff003c74);canvas.drawRoundRect(l,t,r,bottom,2*d,2*d,p);
            if((hot||focused||kind==1&&selected)&&!disabled){p.setStrokeWidth(2*d);p.setColor(hot||kind==1?0xffffc73c:0xff98b8ea);if(kind==1)canvas.drawLine(l+2*d,t+1.5f*d,r-2*d,t+1.5f*d,p);else canvas.drawRoundRect(l+1.5f*d,t+1.5f*d,r-1.5f*d,bottom-1.5f*d,d,d,p);}
            if(kind==1&&selected){p.setStyle(Paint.Style.FILL);p.setColor(0xfffcfcfe);canvas.drawRect(l+d,bottom-d,r-d,bottom+d,p);}p.setStyle(Paint.Style.FILL);
        }
        @Override public void setAlpha(int alpha){p.setAlpha(alpha);}
        @Override public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
    private XpControls(){}
}
