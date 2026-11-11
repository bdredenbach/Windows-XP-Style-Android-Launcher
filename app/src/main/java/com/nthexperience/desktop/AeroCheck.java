package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** XP Luna and Classic check/radio artwork with native accessibility semantics. */
final class AeroCheck extends Drawable {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private final boolean radio;
    private final Context context;
    AeroCheck(Context c,boolean radio){context=c;density=c.getResources().getDisplayMetrics().density;this.radio=radio;}
    @Override public int getIntrinsicWidth(){return Math.round(23*density);}
    @Override public int getIntrinsicHeight(){return Math.round(20*density);}
    @Override public boolean isStateful(){return true;}
    @Override protected boolean onStateChange(int[] state){invalidateSelf();return true;}
    @Override public void draw(Canvas canvas){boolean checked=false,enabled=false,pressed=false,hot=false;for(int s:getState()){checked|=s==android.R.attr.state_checked;enabled|=s==android.R.attr.state_enabled;pressed|=s==android.R.attr.state_pressed;hot|=s==android.R.attr.state_hovered||s==android.R.attr.state_focused;}boolean classic=XpTheme.scheme(context)==5;Rect b=getBounds();float size=13*density,x=b.left+2*density,y=b.exactCenterY()-size/2;RectF box=new RectF(x,y,x+size,y+size);paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(x,y,x+size,y+size,new int[]{pressed?0xffd4d0c8:classic?Color.WHITE:0xffdcdcd7,enabled?Color.WHITE:0xffece9d8},null,Shader.TileMode.CLAMP));if(radio)canvas.drawOval(box,paint);else canvas.drawRect(box,paint);paint.setShader(null);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(density);paint.setColor(!enabled?0xffb6b4a6:classic?0xff808080:0xff1c5180);if(radio)canvas.drawOval(box,paint);else canvas.drawRect(box,paint);if(hot&&enabled&&!classic){paint.setStrokeWidth(2*density);paint.setColor(0xffffc73c);RectF glow=new RectF(box);glow.inset(1.5f*density,1.5f*density);if(radio)canvas.drawOval(glow,paint);else canvas.drawRect(glow,paint);}paint.setStyle(Paint.Style.FILL);if(checked){paint.setColor(!enabled?0xff9b9a91:classic?Color.BLACK:0xff21a121);if(radio)canvas.drawCircle(box.centerX(),box.centerY(),3*density,paint);else{Path tick=new Path();tick.moveTo(x+2.5f*density,y+6*density);tick.lineTo(x+5*density,y+9*density);tick.lineTo(x+10*density,y+3*density);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2*density);paint.setStrokeCap(Paint.Cap.SQUARE);canvas.drawPath(tick,paint);paint.setStyle(Paint.Style.FILL);}}}
    @Override public void setAlpha(int alpha){paint.setAlpha(alpha);}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
