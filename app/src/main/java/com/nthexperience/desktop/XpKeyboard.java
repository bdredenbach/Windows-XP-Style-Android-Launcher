package com.nthexperience.desktop;

import android.content.*;
import android.view.*;
import android.widget.*;

final class XpKeyboard extends XpToolPanel {
    private boolean shift;
    XpKeyboard(Context c,DesktopStore store,Host host){super(c,store,host,"@osk");command("Shift",()->{shift=!shift;say(shift?"Shift on":"Shift off");});command("Backspace",()->send("\b"));command("Enter",()->send("\n"));
        for(int i=0;i<toolbar.getChildCount();i++)toolbar.getChildAt(i).setFocusable(false);
        LinearLayout keys=Aero.column(c);for(String letters:new String[]{"1234567890","qwertyuiop","asdfghjkl","zxcvbnm,./"}){LinearLayout row=new LinearLayout(c);for(char value:letters.toCharArray()){String key=String.valueOf(value);TextView button=Aero.button(c,key,()->send(shift?key.toUpperCase(java.util.Locale.ROOT):key));button.setFocusable(false);button.setPadding(0,0,0,0);button.setTextSize(14);button.setMinHeight(0);button.setContentDescription("Type "+key);row.addView(button,new LayoutParams(0,dp(34),1));}keys.addView(row,new LayoutParams(-1,dp(34)));}TextView space=Aero.button(c,"Space",()->send(" "));space.setFocusable(false);space.setMinHeight(0);keys.addView(space,new LayoutParams(-1,dp(32)));content.addView(keys,new FrameLayout.LayoutParams(-1,-1));say("Select a Nth text field, then tap keys");}
    private void send(String value){say(host.type(value)?"Typed into selected Nth field":"Select a Nth text field first. Android controls other apps.");}
}
