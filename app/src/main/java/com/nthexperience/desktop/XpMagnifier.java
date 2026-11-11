package com.nthexperience.desktop;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;

final class XpMagnifier extends XpToolPanel {
    private Bitmap capture;private Preview preview;private float zoom=2,cx=.5f,cy=.5f;private boolean live,active=true;
    private final Handler main=new Handler(Looper.getMainLooper());private final Runnable tick=new Runnable(){public void run(){if(closed||!active||!live)return;if(isShown())refresh();main.postDelayed(this,650);}};
    XpMagnifier(Context c,DesktopStore store,Host host){super(c,store,host,"@magnifier");command("Capture",this::refresh);command("2×",()->{zoom=2;preview.invalidate();});command("4×",()->{zoom=4;preview.invalidate();});command("8×",()->{zoom=8;preview.invalidate();});command("Live",()->{live=!live;say(live?"Live Nth desktop • drag to pan":"Live capture off");main.removeCallbacks(tick);if(live)main.post(tick);});preview=new Preview(c);content.addView(preview,new FrameLayout.LayoutParams(-1,-1));say("Capture the Nth desktop, then drag to pan");post(this::refresh);}
    private void refresh(){if(closed||!isAttachedToWindow())return;Bitmap next=host.desktopPicture(this);if(next!=null){Bitmap old=capture;capture=next;preview.invalidate();if(old!=null)old.recycle();say((int)zoom+"× • drag to pan • Nth desktop only");}}
    @Override void active(boolean value){active=value;main.removeCallbacks(tick);if(value&&live)main.post(tick);}
    @Override void close(){if(closed)return;closed=true;main.removeCallbacksAndMessages(null);if(capture!=null){capture.recycle();capture=null;}}
    private final class Preview extends View {final Paint paint=new Paint();float x,y;Preview(Context c){super(c);setBackgroundColor(Color.BLACK);setContentDescription("Magnified Nth desktop. Drag to pan.");}
        @Override protected void onDraw(Canvas canvas){if(capture==null||capture.isRecycled())return;float sw=capture.getWidth()/zoom,sh=Math.min(capture.getHeight(),sw*getHeight()/Math.max(1,getWidth()));float l=Math.max(0,Math.min(capture.getWidth()-sw,cx*capture.getWidth()-sw/2)),t=Math.max(0,Math.min(capture.getHeight()-sh,cy*capture.getHeight()-sh/2));canvas.drawBitmap(capture,new Rect(Math.round(l),Math.round(t),Math.round(l+sw),Math.round(t+sh)),new Rect(0,0,getWidth(),getHeight()),paint);}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_DOWN){x=e.getX();y=e.getY();return true;}if(e.getActionMasked()==MotionEvent.ACTION_MOVE){cx=Math.max(0,Math.min(1,cx-(e.getX()-x)/Math.max(1,getWidth())/zoom));cy=Math.max(0,Math.min(1,cy-(e.getY()-y)/Math.max(1,getHeight())/zoom));x=e.getX();y=e.getY();invalidate();return true;}if(e.getActionMasked()==MotionEvent.ACTION_UP){performClick();return true;}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
