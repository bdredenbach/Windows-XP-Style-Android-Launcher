package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;

/** The same bounded, lifecycle-aware animation is used by preview and Android's dream. */
final class XpScreenSaver extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final long start=SystemClock.uptimeMillis();
    private boolean running=true;
    private final Runnable tick=new Runnable(){public void run(){if(running&&isAttachedToWindow()&&getWindowVisibility()==VISIBLE&&isShown()){invalidate();postDelayed(this,reduced()?1000:50);}}};
    XpScreenSaver(Context c){super(c);setMinimumHeight(Aero.dp(c,220));setContentDescription("Nth Desktop XP screen saver preview");}
    private boolean reduced(){return getContext().getSharedPreferences("desktop",0).getBoolean("reduce_motion",false)||!android.animation.ValueAnimator.areAnimatorsEnabled();}
    void running(boolean value){running=value;removeCallbacks(tick);if(value&&isAttachedToWindow())post(tick);}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();running(running);}
    @Override protected void onDetachedFromWindow(){removeCallbacks(tick);super.onDetachedFromWindow();}
    @Override protected void onVisibilityChanged(View changed,int visibility){super.onVisibilityChanged(changed,visibility);if(tick!=null){removeCallbacks(tick);if(running&&isShown())post(tick);}}
    @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);removeCallbacks(tick);if(visibility==VISIBLE&&running)post(tick);}
    @Override protected void onDraw(Canvas c){
        c.drawColor(Color.BLACK);float d=getResources().getDisplayMetrics().density;
        float width=Math.min(getWidth(),210*d),height=Math.min(getHeight(),82*d);
        float seconds=(SystemClock.uptimeMillis()-start)/1000f;
        float x=reduced()?(getWidth()-width)/2:wave(seconds/24)*(getWidth()-width);
        float y=reduced()?(getHeight()-height)/2:wave(seconds/31+.3f)*(getHeight()-height);
        paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setTextSize(Math.min(23*d,width/9));paint.setColor(0xffdddddd);
        c.drawText("Nth Desktop",x+8*d,y+30*d,paint);paint.setColor(0xffff9b32);paint.setTextSize(Math.min(18*d,width/11));c.drawText("XP",x+8*d,y+53*d,paint);
        paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(Math.min(12*d,width/17));paint.setColor(0xffaaaaaa);
        c.drawText(android.text.format.DateFormat.getTimeFormat(getContext()).format(new java.util.Date()),x+8*d,y+72*d,paint);
    }
    private float wave(float time){float phase=time%2;return phase<=1?phase:2-phase;}
}
