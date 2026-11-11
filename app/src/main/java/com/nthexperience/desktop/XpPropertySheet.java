package com.nthexperience.desktop;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.function.BiConsumer;

/** Beige XP property page with tabs attached to a framed body and a button strip. */
final class XpPropertySheet extends LinearLayout {
    private final LinearLayout page;private final GridLayout tabs;private final String[] names;
    private final BiConsumer<LinearLayout,String> renderer;private final TextView apply;
    private String selected;
    XpPropertySheet(Context c,String[] names,String initial,BiConsumer<LinearLayout,String> renderer,Runnable save,Runnable close){
        super(c);this.names=names;this.renderer=renderer;setOrientation(VERTICAL);setPadding(dp(8),dp(8),dp(8),dp(7));setBackgroundColor(XpTheme.surface(c));
        tabs=new GridLayout(c);tabs.setColumnCount(names.length>4?4:names.length);addView(tabs,new LayoutParams(-1,-2));
        ScrollView scroll=new ScrollView(c);scroll.setFillViewport(true);scroll.setBackground(Aero.panel(c,0xfffcfcfe,0xfffcfcfe,0,0xff919b9c));page=Aero.column(c);page.setPadding(dp(13),dp(13),dp(13),dp(15));scroll.addView(page,new ScrollView.LayoutParams(-1,-2));addView(scroll,new LayoutParams(-1,0,1));
        LinearLayout buttons=new LinearLayout(c);buttons.setGravity(Gravity.END);buttons.setPadding(0,dp(8),0,0);
        TextView ok=Aero.button(c,"OK",()->{if(save!=null)save.run();close.run();}),cancel=Aero.button(c,"Cancel",close);apply=Aero.button(c,"Apply",()->{if(save!=null)save.run();dirty(false);});apply.setEnabled(false);
        for(TextView button:new TextView[]{ok,cancel,apply}){button.setTextSize(11);button.setMinWidth(0);LayoutParams lp=new LayoutParams(0,dp(32),1);lp.leftMargin=dp(5);buttons.addView(button,lp);}addView(buttons);select(initial);
    }
    void dirty(boolean value){apply.setEnabled(value);}
    String selected(){return selected;}
    private void select(String name){selected=name;tabs.removeAllViews();for(String label:names){TextView tab=Aero.text(getContext(),label,11,Aero.INK);tab.setGravity(Gravity.CENTER);tab.setMaxLines(2);tab.setPadding(dp(4),0,dp(4),0);tab.setBackground(XpControls.tab(getContext()));tab.setSelected(label.equals(selected));tab.setFocusable(true);tab.setContentDescription("Property tab: "+label);tab.setOnClickListener(v->select(label));GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=dp(names.length>4?36:32);lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);tabs.addView(tab,lp);}page.removeAllViews();renderer.accept(page,selected);}
    private int dp(int n){return Aero.dp(getContext(),n);}
}
