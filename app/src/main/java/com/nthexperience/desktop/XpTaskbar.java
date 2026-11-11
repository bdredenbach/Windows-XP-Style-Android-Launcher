package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

/** Paint the notification well once, flush with the bottom row and right edge. */
final class XpTaskbar extends LinearLayout {
    private final Drawable notification;
    private View firstNotification;
    XpTaskbar(Context c){super(c);notification=XpControls.notification(c,true);setWillNotDraw(false);}
    void notificationStartsAt(View view){firstNotification=view;invalidate();}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(firstNotification==null)return;
        notification.setBounds(firstNotification.getLeft(),Math.max(0,getHeight()-Aero.dp(getContext(),XpMetrics.TASKBAR)),getWidth(),getHeight());
        notification.draw(canvas);
    }
}
