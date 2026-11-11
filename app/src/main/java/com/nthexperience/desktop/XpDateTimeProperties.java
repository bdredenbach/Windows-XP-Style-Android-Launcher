package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Read-only date/time preview; Android owns changes to the system clock. */
final class XpDateTimeProperties {
    static View create(Context c,VistaShell.Actions actions,Runnable close){return new XpPropertySheet(c,new String[]{"Date & Time","Time Zone","Internet Time"},"Date & Time",(body,tab)->{
        if(tab.equals("Date & Time")){
            TextView date=Aero.text(c,java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(new Date()),12,Aero.INK);date.setGravity(Gravity.CENTER);body.addView(date,new LinearLayout.LayoutParams(-1,Aero.dp(c,30)));
            Calendar today=Calendar.getInstance(),first=(Calendar)today.clone();first.set(Calendar.DAY_OF_MONTH,1);GridLayout grid=new GridLayout(c);grid.setColumnCount(7);grid.setBackground(Aero.panel(c,Color.WHITE,Color.WHITE,0,0xff7f9db9));int start=(first.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY+7)%7,total=today.getActualMaximum(Calendar.DAY_OF_MONTH);
            String[] days={"S","M","T","W","T","F","S"};for(int i=0;i<49;i++){int day=i-7-start+1;String label=i<7?days[i]:day>0&&day<=total?Integer.toString(day):"";TextView cell=Aero.text(c,label,11,Aero.INK);cell.setGravity(Gravity.CENTER);if(i>=7&&day==today.get(Calendar.DAY_OF_MONTH)){cell.setBackgroundColor(0xff316ac5);cell.setTextColor(Color.WHITE);}GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=Aero.dp(c,24);lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);grid.addView(cell,lp);}body.addView(grid);
            body.addView(new ClockFace(c),new LinearLayout.LayoutParams(-1,Aero.dp(c,120)));TextClock clock=new TextClock(c);clock.setTypeface(Aero.face(c));clock.setTextColor(Aero.INK);clock.setGravity(Gravity.CENTER);clock.setFormat12Hour("h:mm:ss a");clock.setFormat24Hour("HH:mm:ss");body.addView(clock,new LinearLayout.LayoutParams(-1,Aero.dp(c,28)));
        }else if(tab.equals("Time Zone")){body.addView(Aero.text(c,"Current time zone:",12,Aero.INK));body.addView(Aero.text(c,TimeZone.getDefault().getID()+"\n"+TimeZone.getDefault().getDisplayName(),13,Aero.INK));}
        else body.addView(Aero.text(c,"Your Android device manages automatic network time and time synchronization.",12,Aero.INK));
        TextView note=Aero.text(c,"Use Android settings to change the date, time or time zone.",11,Aero.INK);note.setPadding(0,Aero.dp(c,12),0,Aero.dp(c,8));body.addView(note);body.addView(Aero.button(c,"Change date and time…",()->actions.action("android:date")),new LinearLayout.LayoutParams(-1,Aero.dp(c,34)));
    },null,close);}
    private static final class ClockFace extends View {
        private final Paint paint=new Paint(3);ClockFace(Context c){super(c);setContentDescription("Analog system clock");}
        @Override protected void onDraw(Canvas c){float cx=getWidth()/2f,cy=getHeight()/2f,r=Math.min(getWidth(),getHeight())*.43f;paint.setColor(Color.WHITE);c.drawCircle(cx,cy,r,paint);paint.setColor(0xff808080);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);c.drawCircle(cx,cy,r,paint);paint.setStyle(Paint.Style.FILL);for(int n=0;n<60;n++){double a=n*Math.PI/30;paint.setColor(Color.BLACK);paint.setStrokeWidth(n%5==0?2:1);float inner=r*(n%5==0?.83f:.92f);c.drawLine(cx+(float)Math.sin(a)*inner,cy-(float)Math.cos(a)*inner,cx+(float)Math.sin(a)*r*.97f,cy-(float)Math.cos(a)*r*.97f,paint);}Calendar now=Calendar.getInstance();hand(c,cx,cy,(now.get(Calendar.HOUR)+now.get(Calendar.MINUTE)/60f)*30,r*.55f,3,Color.BLACK);hand(c,cx,cy,now.get(Calendar.MINUTE)*6,r*.8f,2,Color.BLACK);hand(c,cx,cy,now.get(Calendar.SECOND)*6,r*.8f,1,0xffb40000);postInvalidateDelayed(1000);}
        private void hand(Canvas c,float x,float y,float angle,float length,int width,int color){paint.setColor(color);paint.setStrokeWidth(width*getResources().getDisplayMetrics().density);double a=Math.toRadians(angle);c.drawLine(x,y,x+(float)Math.sin(a)*length,y-(float)Math.cos(a)*length,paint);}
    }
}
