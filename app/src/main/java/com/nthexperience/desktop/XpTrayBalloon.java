package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;

/** Foreground XP notice, bounded inside the desktop and never a system overlay. */
final class XpTrayBalloon {
    private final FrameLayout workspace;private final Handler main=new Handler(Looper.getMainLooper());
    private View panel;private int left,right,bottom,top;
    XpTrayBalloon(FrameLayout workspace){this.workspace=workspace;}
    void bounds(int left,int right,int top,int bottom){this.left=left;this.right=right;this.top=top;this.bottom=bottom;if(panel!=null)place();}
    void show(String title,String message,Runnable action){
        dismiss();Context c=workspace.getContext();LinearLayout body=Aero.column(c);body.setContentDescription("XP balloon: "+title);body.setPadding(dp(12),dp(6),dp(8),dp(20));body.setElevation(dp(3));body.setBackground(new Balloon(c));body.setClickable(true);body.setOnClickListener(v->{dismiss();action.run();});
        LinearLayout caption=new LinearLayout(c);TextView heading=Aero.text(c,title,13,Color.BLACK);Aero.bold(heading);caption.addView(heading,new LinearLayout.LayoutParams(0,dp(28),1));TextView close=Aero.text(c,"×",18,0xff777777);close.setContentDescription("Close notification balloon");close.setGravity(Gravity.CENTER);close.setFocusable(true);close.setOnClickListener(v->dismiss());caption.addView(close,new LinearLayout.LayoutParams(dp(32),dp(28)));body.addView(caption);TextView text=Aero.text(c,message,12,Color.BLACK);text.setMaxLines(4);text.setPadding(0,dp(4),dp(7),0);body.addView(text,new LinearLayout.LayoutParams(-1,-2));panel=body;workspace.addView(panel,new FrameLayout.LayoutParams(dp(300),-2,Gravity.BOTTOM|Gravity.END));place();main.postDelayed(this::dismiss,10000);
    }
    private void place(){FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)panel.getLayoutParams();p.width=Math.min(dp(300),Math.max(1,workspace.getWidth()-left-right-dp(12)));p.rightMargin=right+dp(6);p.bottomMargin=bottom+dp(2);int available=Math.max(1,workspace.getHeight()-top-p.bottomMargin-dp(4));panel.measure(View.MeasureSpec.makeMeasureSpec(p.width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(available,View.MeasureSpec.AT_MOST));p.height=Math.min(available,panel.getMeasuredHeight());panel.setLayoutParams(p);}
    private static final class Balloon extends android.graphics.drawable.Drawable {
        final android.graphics.Paint paint=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);final float d;
        Balloon(Context c){d=c.getResources().getDisplayMetrics().density;}
        public void draw(android.graphics.Canvas c){android.graphics.Rect b=getBounds();float y=Math.max(b.top,b.bottom-10*d),x=b.right-25*d;paint.setColor(0xffffffe1);paint.setStyle(android.graphics.Paint.Style.FILL);c.drawRoundRect(b.left+.5f*d,b.top+.5f*d,b.right-.5f*d,y,7*d,7*d,paint);android.graphics.Path tail=new android.graphics.Path();tail.moveTo(x-10*d,y-d);tail.lineTo(x+5*d,b.bottom-d);tail.lineTo(x+3*d,y-d);tail.close();c.drawPath(tail,paint);paint.setColor(0xff545454);paint.setStrokeWidth(d);paint.setStyle(android.graphics.Paint.Style.STROKE);c.drawRoundRect(b.left+.5f*d,b.top+.5f*d,b.right-.5f*d,y,7*d,7*d,paint);paint.setColor(0xffffffe1);paint.setStrokeWidth(2*d);c.drawLine(x-9*d,y,x+2*d,y,paint);paint.setColor(0xff545454);paint.setStrokeWidth(d);c.drawLine(x-10*d,y,x+5*d,b.bottom-d,paint);c.drawLine(x+5*d,b.bottom-d,x+3*d,y,paint);}
        public void setAlpha(int a){paint.setAlpha(a);}public void setColorFilter(android.graphics.ColorFilter f){paint.setColorFilter(f);}public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
    }
    void dismiss(){main.removeCallbacksAndMessages(null);if(panel!=null){workspace.removeView(panel);panel=null;}}
    boolean showing(){return panel!=null;}
    private int dp(int value){return Aero.dp(workspace.getContext(),value);}
}
