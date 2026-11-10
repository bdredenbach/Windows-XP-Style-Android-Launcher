package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Small, code-drawn XP navigation signs; no platform emoji or symbol-font dependency. */
final class XpNavigation extends Drawable {
    final String kind;final Context context;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    XpNavigation(Context context,String kind){this.context=context;this.kind=kind;}
    @Override public int getIntrinsicWidth(){return Aero.dp(context,24);}
    @Override public int getIntrinsicHeight(){return Aero.dp(context,24);}
    @Override public void draw(Canvas c){int save=c.save();Rect b=getBounds();c.translate(b.left,b.top);c.scale(b.width()/32f,b.height()/32f);p.setStyle(Paint.Style.FILL);p.setShader(null);
        boolean arrow=kind.equals("back")||kind.equals("forward");
        if(arrow){p.setShader(new LinearGradient(0,0,30,30,new int[]{0xffbde9a8,0xff58a345,0xff257b27},null,Shader.TileMode.CLAMP));c.drawCircle(16,16,14,p);p.setShader(null);p.setColor(0xff3b7e32);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);c.drawCircle(16,16,14,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);Path path=new Path();if(kind.equals("forward")){c.translate(32,0);c.scale(-1,1);}path.moveTo(8,16);path.lineTo(17,8);path.lineTo(17,13);path.lineTo(25,13);path.lineTo(25,19);path.lineTo(17,19);path.lineTo(17,24);path.close();c.drawPath(path,p);}
        else if(kind.equals("search")){p.setColor(0xffa36e25);p.setStrokeWidth(5);c.drawLine(18,19,28,29,p);p.setColor(0xff4887b0);p.setStrokeWidth(3);p.setStyle(Paint.Style.STROKE);c.drawCircle(12,12,9,p);p.setStyle(Paint.Style.FILL);p.setColor(0x99bce3fb);c.drawCircle(12,12,7,p);p.setColor(Color.WHITE);c.drawCircle(9,9,2,p);}
        else if(kind.equals("folders")||kind.equals("up")){p.setColor(0xffce9232);c.drawRoundRect(3,9,29,28,2,2,p);p.setColor(0xffffd975);c.drawRect(3,6,15,11,p);p.setShader(new LinearGradient(0,12,0,28,0xffffe698,0xffeab446,Shader.TileMode.CLAMP));c.drawRoundRect(3,13,29,28,2,2,p);p.setShader(null);if(kind.equals("up")){p.setColor(0xff359331);Path path=new Path();path.moveTo(17,3);path.lineTo(27,13);path.lineTo(22,13);path.lineTo(22,23);path.lineTo(13,23);path.lineTo(13,13);path.lineTo(8,13);path.close();c.drawPath(path,p);}}
        else {boolean power=kind.equals("power");p.setShader(new LinearGradient(0,1,0,31,power?new int[]{0xffffa185,0xffe45636,0xffb72d16}:new int[]{0xffffe38b,0xffeeb63c,0xffc98a13},null,Shader.TileMode.CLAMP));c.drawRoundRect(2,2,30,30,4,4,p);p.setShader(null);p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);if(power){c.drawArc(9,9,23,24,-45,270,false,p);c.drawLine(16,7,16,16,p);}else{c.drawRect(8,8,18,25,p);c.drawLine(14,16,25,16,p);c.drawLine(21,12,25,16,p);c.drawLine(21,20,25,16,p);}}
        p.setShader(null);p.setStyle(Paint.Style.FILL);c.restoreToCount(save);
    }
    @Override public void setAlpha(int alpha){p.setAlpha(alpha);}
    @Override public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
