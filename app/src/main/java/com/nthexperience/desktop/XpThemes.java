package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

final class XpThemes extends GridLayout {
    XpThemes(Context context,DesktopStore store,int columns,Runnable applied){
        super(context);setColumnCount(columns);setContentDescription("XP appearance schemes");
        for(int i=0;i<XpTheme.NAMES.length;i++){
            final int scheme=i;boolean selected=XpTheme.scheme(context)==i;
            LinearLayout tile=Aero.column(context);Aero.pad(tile,6);tile.setFocusable(true);tile.setContentDescription("Apply theme: "+XpTheme.NAMES[i]);
            tile.setBackground(Aero.panel(context,selected?0xffdceaff:0xfff6f6f0,selected?0xffc8ddfa:0xffe8e6da,2,selected?0xff316ac5:0xffaca899));
            LinearLayout sample=Aero.column(context);sample.setBackgroundColor(Color.WHITE);
            TextView title=Aero.text(context,"Nth Desktop XP",11,XpTheme.COLORS[i][5]);title.setPadding(Aero.dp(context,5),0,0,0);
            title.setBackground(Aero.panel(context,XpTheme.COLORS[i][0],XpTheme.COLORS[i][1],i==5?0:3,0));sample.addView(title,new LinearLayout.LayoutParams(-1,Aero.dp(context,26)));
            TextView content=Aero.text(context,"Aa    □  ×",14,0xff172343);content.setGravity(Gravity.CENTER);content.setBackgroundColor(XpTheme.COLORS[i][3]);sample.addView(content,new LinearLayout.LayoutParams(-1,Aero.dp(context,37)));
            tile.addView(sample,new LinearLayout.LayoutParams(-1,-2));TextView name=Aero.text(context,(selected?"✓ ":"")+XpTheme.NAMES[i],12,0xff172343);name.setGravity(Gravity.CENTER);tile.addView(name,new LinearLayout.LayoutParams(-1,Aero.dp(context,34)));
            tile.setOnClickListener(v->{XpTheme.apply(store,scheme);applied.run();});
            GridLayout.LayoutParams cell=new GridLayout.LayoutParams();cell.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);cell.width=0;cell.setMargins(0,0,Aero.dp(context,7),Aero.dp(context,8));addView(tile,cell);
        }
    }
}
