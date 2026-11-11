package com.nthexperience.desktop;

import android.content.Context;
import android.view.*;
import android.widget.*;

/** The separated, inset task groups used by XP's Explorer and Control Panel. */
final class XpTaskPane extends LinearLayout {
    final LinearLayout items;
    private final TextView arrow;
    XpTaskPane(Context c,String title,boolean primary){
        super(c);setOrientation(VERTICAL);setContentDescription("Task group: "+title);items=Aero.column(c);
        setBackground(Aero.panel(c,0xffd6dff7,0xffd6dff7,0,0xffffffff));
        LinearLayout header=new LinearLayout(c);header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8),0,dp(6),0);header.setBackground(Aero.panel(c,primary?0xff1c65c9:0xffffffff,primary?0xff2557ae:0xffc6d3f7,3,0xffe7edff));
        TextView caption=Aero.text(c,title,11,primary?0xffffffff:0xff214477);Aero.bold(caption);caption.setMaxLines(2);
        header.addView(caption,new LayoutParams(0,-2,1));arrow=Aero.text(c,"⌃",15,primary?0xffffffff:0xff3166a8);arrow.setGravity(Gravity.CENTER);header.addView(arrow,new LayoutParams(dp(20),dp(28)));
        header.setContentDescription("Collapse task group: "+title);header.setFocusable(true);header.setOnClickListener(v->{boolean open=items.getVisibility()==VISIBLE;items.setVisibility(open?GONE:VISIBLE);arrow.setText(open?"⌄":"⌃");header.setContentDescription((open?"Expand":"Collapse")+" task group: "+title);});
        addView(header,new LayoutParams(-1,dp(32)));items.setPadding(dp(7),dp(5),dp(7),dp(7));addView(items,new LayoutParams(-1,-2));
    }
    static XpTaskPane add(LinearLayout parent,String title,boolean primary){XpTaskPane pane=new XpTaskPane(parent.getContext(),title,primary);LayoutParams lp=new LayoutParams(-1,-2);lp.bottomMargin=Aero.dp(parent.getContext(),16);parent.addView(pane,lp);return pane;}
    private int dp(int n){return Aero.dp(getContext(),n);}
}
