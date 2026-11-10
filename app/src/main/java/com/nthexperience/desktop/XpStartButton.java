package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.view.Gravity;
import android.widget.TextView;

/** The plastic Start cap, with Nth branding and a full-row hit area. */
final class XpStartButton extends TextView {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    XpStartButton(Context context){super(context);setText("start");setGravity(Gravity.CENTER);setFocusable(true);setContentDescription("Start menu");setBackgroundColor(Color.TRANSPARENT);}
    @Override protected void onDraw(Canvas canvas){
        float d=getResources().getDisplayMetrics().density,w=getWidth(),h=getHeight();boolean classic=XpTheme.scheme(getContext())==5,zune=XpTheme.scheme(getContext())==4;
        int[] colors=classic?new int[]{0xffefede5,0xffd4d0c8}:isPressed()?new int[]{0xff194f16,0xff397c28,0xff286622}:zune?new int[]{0xffffd487,0xffe48b2a,0xffb95e16}:isHovered()||isFocused()?new int[]{0xffb0e28a,0xff62b447,0xff3e8e2c}:new int[]{0xff9fc989,0xff56a33b,0xff367c2d,0xff338a28,0xff1b5e1a};
        paint.setShader(new LinearGradient(0,0,0,h,colors,null,Shader.TileMode.CLAMP));Path shape=new Path();shape.addRoundRect(new RectF(0,0,w,h),new float[]{0,0,11*d,11*d,11*d,11*d,0,0},Path.Direction.CW);canvas.drawPath(shape,paint);paint.setShader(null);
        paint.setColor(classic?Color.WHITE:0xffb1df90);canvas.drawRect(0,d,w-9*d,2*d,paint);paint.setColor(classic?0xff808080:0xff28631c);canvas.drawRect(0,h-d,w-8*d,h,paint);
        // A compact four-color Nth mark keeps the XP silhouette without replacing app identity.
        float x=8*d,y=(h-22*d)/2,s=22*d;int[] panes={0xffe85c2b,0xff82b71b,0xff2c8be5,0xffffc638};
        for(int i=0;i<4;i++){float l=x+(i%2)*11*d,t=y+(i/2)*11*d;paint.setColor(panes[i]);canvas.drawRoundRect(l,t,l+10*d,t+10*d,d,d,paint);}
        paint.setTypeface(Typeface.create(Aero.face(getContext()),Typeface.BOLD_ITALIC));paint.setTextSize(18*d);paint.setColor(Color.WHITE);paint.setShadowLayer(d,d,d,0xff244724);canvas.drawText("N",x+3*d,y+18*d,paint);
        paint.setTextSize(21*d);paint.setColor(classic?Color.BLACK:Color.WHITE);if(classic)paint.clearShadowLayer();canvas.drawText(classic?"Start":"start",x+s+5*d,h/2+7*d,paint);paint.clearShadowLayer();
    }
}
