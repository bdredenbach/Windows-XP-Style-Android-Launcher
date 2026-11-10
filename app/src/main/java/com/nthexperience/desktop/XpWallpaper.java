package com.nthexperience.desktop;

import android.graphics.*;

/** Original procedural hill landscape; no copy of Microsoft's Bliss photograph. */
final class XpWallpaper {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path hill=new Path();
    void draw(Canvas c,float w,float h){
        p.setShader(new LinearGradient(0,0,0,h,new int[]{0xff1d65b7,0xff61a8ec,0xffc5e6fb},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);
        p.setShader(null);p.setColor(0x70ffffff);
        cloud(c,w*.22f,h*.16f,w*.17f,h*.035f);cloud(c,w*.75f,h*.26f,w*.15f,h*.029f);cloud(c,w*.52f,h*.09f,w*.1f,h*.021f);
        hill.reset();hill.moveTo(0,h*.65f);hill.cubicTo(w*.24f,h*.50f,w*.39f,h*.57f,w*.63f,h*.64f);hill.cubicTo(w*.79f,h*.68f,w*.91f,h*.67f,w,h*.59f);hill.lineTo(w,h);hill.lineTo(0,h);hill.close();
        p.setShader(new LinearGradient(0,h*.53f,w*.6f,h,0xff77bd36,0xff245f10,Shader.TileMode.CLAMP));c.drawPath(hill,p);
        hill.reset();hill.moveTo(0,h*.83f);hill.cubicTo(w*.27f,h*.66f,w*.66f,h*.72f,w,h*.78f);hill.lineTo(w,h);hill.lineTo(0,h);hill.close();
        p.setShader(new LinearGradient(0,h*.7f,0,h,0xff5ca624,0xff387514,Shader.TileMode.CLAMP));c.drawPath(hill,p);p.setShader(null);
    }
    private void cloud(Canvas c,float x,float y,float w,float h){for(int i=0;i<6;i++)c.drawOval(x-w+i*w*.27f,y-h*(i%2),x-w+i*w*.27f+w*.75f,y+h*(1.2f+i%3*.2f),p);}
}
