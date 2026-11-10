package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.widget.TextView;

/** XP's labelled Start button, with a touch-sized target. */
final class XpStartButton extends TextView {
    XpStartButton(Context context){
        super(context);setText("N  start");setTextSize(19);setTypeface(Typeface.create("sans-serif",Typeface.BOLD_ITALIC));
        setTextColor(0xffffffff);setShadowLayer(1,1,1,0xff164311);setGravity(Gravity.CENTER);
        setPadding(Aero.dp(context,4),0,Aero.dp(context,8),0);setFocusable(true);setContentDescription("Start menu");
        boolean zune=XpTheme.scheme(context)==4,classic=XpTheme.scheme(context)==5;
        int[] normal=zune?new int[]{0xffffc66a,0xffda7921,0xffaa4e0d}:classic?new int[]{0xffe6e2da,0xffd4d0c8,0xffaaa69e}:new int[]{0xff86c878,0xff379037,0xff247523,0xff3b923b};
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},Aero.gloss(context,new int[]{0xff245c24,0xff377a31,0xff4a9944},6,0xff15531d));
        for(int state:new int[]{android.R.attr.state_focused,android.R.attr.state_hovered})states.addState(new int[]{state},Aero.gloss(context,new int[]{0xffb4e99d,0xff50aa3e,0xff398b2c},6,0xffe2f5c7));
        states.addState(new int[]{},Aero.gloss(context,normal,classic?0:6,classic?0xff808080:0xff28612b));setBackground(states);
        if(classic){setTextColor(0xff000000);setShadowLayer(0,0,0,0);}
    }
}
