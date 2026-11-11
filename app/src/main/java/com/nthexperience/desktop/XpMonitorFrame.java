package com.nthexperience.desktop;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Original pale CRT frame matching XP's Display Properties proportions. */
final class XpMonitorFrame extends Drawable {
    private final Paint p=new Paint(3);private final float d;
    XpMonitorFrame(Context c){d=c.getResources().getDisplayMetrics().density;}
    @Override public void draw(Canvas c){Rect b=getBounds();p.setShader(new LinearGradient(0,b.top,0,b.bottom-14*d,new int[]{0xfff5f5f2,0xffd8d8d3,0xffa4a4a0},null,Shader.TileMode.CLAMP));c.drawRoundRect(b.left,b.top,b.right,b.bottom-14*d,6*d,6*d,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setColor(0xff8f8f8a);p.setStrokeWidth(d);c.drawRoundRect(b.left+d,b.top+d,b.right-d,b.bottom-14*d,6*d,6*d,p);p.setStyle(Paint.Style.FILL);p.setColor(0xff777774);c.drawRect(b.centerX()-15*d,b.bottom-14*d,b.centerX()+15*d,b.bottom-5*d,p);p.setShader(new LinearGradient(0,b.bottom-8*d,0,b.bottom,0xffbdbdb8,0xff8a8a85,Shader.TileMode.CLAMP));c.drawOval(b.centerX()-43*d,b.bottom-9*d,b.centerX()+43*d,b.bottom,p);p.setShader(null);p.setColor(0xff568a41);c.drawCircle(b.right-17*d,b.bottom-20*d,2*d,p);}
    @Override public void setAlpha(int alpha){p.setAlpha(alpha);}@Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
