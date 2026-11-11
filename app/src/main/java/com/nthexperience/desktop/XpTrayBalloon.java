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
        dismiss();Context c=workspace.getContext();LinearLayout body=Aero.column(c);body.setContentDescription("XP balloon: "+title);body.setPadding(dp(12),dp(8),dp(8),dp(10));body.setElevation(dp(18));GradientDrawable background=Aero.panel(c,0xffffffeb,0xffffffe1,7,0xff545454);body.setBackground(background);body.setClickable(true);body.setOnClickListener(v->{dismiss();action.run();});
        LinearLayout caption=new LinearLayout(c);TextView heading=Aero.text(c,title,13,Color.BLACK);Aero.bold(heading);caption.addView(heading,new LinearLayout.LayoutParams(0,dp(28),1));TextView close=Aero.text(c,"×",18,0xff777777);close.setContentDescription("Close notification balloon");close.setGravity(Gravity.CENTER);close.setFocusable(true);close.setOnClickListener(v->dismiss());caption.addView(close,new LinearLayout.LayoutParams(dp(32),dp(28)));body.addView(caption);TextView text=Aero.text(c,message,12,Color.BLACK);text.setMaxLines(4);text.setPadding(0,dp(4),dp(7),0);body.addView(text,new LinearLayout.LayoutParams(-1,-2));panel=body;workspace.addView(panel,new FrameLayout.LayoutParams(dp(300),-2,Gravity.BOTTOM|Gravity.END));place();main.postDelayed(this::dismiss,10000);
    }
    private void place(){FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)panel.getLayoutParams();p.width=Math.min(dp(300),Math.max(1,workspace.getWidth()-left-right-dp(12)));p.rightMargin=right+dp(6);p.bottomMargin=bottom+dp(8);panel.setLayoutParams(p);}
    void dismiss(){main.removeCallbacksAndMessages(null);if(panel!=null){workspace.removeView(panel);panel=null;}}
    boolean showing(){return panel!=null;}
    private int dp(int value){return Aero.dp(workspace.getContext(),value);}
}
