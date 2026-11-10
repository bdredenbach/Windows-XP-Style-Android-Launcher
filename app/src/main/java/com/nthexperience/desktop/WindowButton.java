package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.widget.TextView;

/** Consistent caption controls, independent of the device's symbol font. */
final class WindowButton extends TextView {
    private final Paint ink=new Paint(Paint.ANTI_ALIAS_FLAG);
    WindowButton(Context context,String symbol,Runnable action){
        super(context);setText(symbol);setGravity(Gravity.CENTER);setFocusable(true);
        setMinHeight(0);setPadding(0,0,0,0);setOnClickListener(v->action.run());
        boolean close="×".equals(symbol);
        boolean classic=XpTheme.scheme(context)==5;int[] palette=XpTheme.palette(context);
        int[] normal=classic?new int[]{0xfff0ede5,0xffd4d0c8,0xffc5c1b9}:close?new int[]{0xfff59b7e,0xffd64e33,0xffb82b16}:new int[]{palette[0],palette[1],palette[2]};
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},Aero.gloss(context,classic?new int[]{0xffaaa69e,0xffd4d0c8}:close?new int[]{0xffb83821,0xffd95432}:new int[]{palette[2],palette[1]},classic?0:3,0xff6b7794));
        for(int state:new int[]{android.R.attr.state_hovered,android.R.attr.state_focused})states.addState(new int[]{state},Aero.gloss(context,normal,classic?0:3,0xffffd76b));
        states.addState(new int[]{},Aero.gloss(context,normal,classic?0:3,classic?0xff808080:0xffdfebff));
        setBackground(states);
    }
    @Override protected void onDraw(Canvas canvas){
        float d=getResources().getDisplayMetrics().density,cx=getWidth()/2f,cy=getHeight()/2f;
        String symbol=getText().toString();boolean close="×".equals(symbol);
        ink.setColor(XpTheme.scheme(getContext())==5||(!close&&XpTheme.scheme(getContext())==2)?0xff172343:Color.WHITE);ink.setAlpha(isEnabled()?255:100);
        ink.setStyle(Paint.Style.STROKE);ink.setStrokeWidth(Math.max(d,1));
        if(close){ink.setStrokeWidth(1.6f*d);canvas.drawLine(cx-4*d,cy-4*d,cx+4*d,cy+4*d,ink);canvas.drawLine(cx+4*d,cy-4*d,cx-4*d,cy+4*d,ink);}
        else if("—".equals(symbol)){ink.setStrokeWidth(2*d);canvas.drawLine(cx-5*d,cy+4*d,cx+5*d,cy+4*d,ink);}
        else if("❐".equals(symbol)||"▣".equals(symbol)){
            canvas.drawRect(cx-2*d,cy-5*d,cx+5*d,cy+2*d,ink);
            ink.setStyle(Paint.Style.FILL);ink.setColor(0xffc0d9e7);canvas.drawRect(cx-5*d,cy-2*d,cx+2*d,cy+5*d,ink);
            ink.setStyle(Paint.Style.STROKE);ink.setColor(XpTheme.scheme(getContext())==2||XpTheme.scheme(getContext())==5?0xff172343:Color.WHITE);canvas.drawRect(cx-5*d,cy-2*d,cx+2*d,cy+5*d,ink);canvas.drawLine(cx-5*d,cy,cx+2*d,cy,ink);
        }else{canvas.drawRect(cx-5*d,cy-4*d,cx+5*d,cy+4*d,ink);canvas.drawLine(cx-5*d,cy-2*d,cx+5*d,cy-2*d,ink);}
    }
}
