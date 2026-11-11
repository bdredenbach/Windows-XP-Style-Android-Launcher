package com.nthexperience.desktop;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;

/** XP session presentation; labels identify the real action available on Android. */
final class XpSessionDialogs {
    interface Host {void action(String id);}
    static Dialog show(Activity activity,DesktopStore store,String mode,Host host){
        boolean welcome=mode.equals("welcome"),power=mode.equals("power");
        Dialog dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel=Aero.column(activity);panel.setContentDescription(welcome?"XP Welcome screen":power?"XP Turn Off panel":"XP Log Off panel");
        panel.setBackground(Aero.panel(activity,0xff5a7edc,0xff5a7edc,0,0xff173c95));
        LinearLayout header=new LinearLayout(activity);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(activity,16),dp(activity,10),dp(activity,16),dp(activity,10));header.setBackgroundColor(0xff0b267d);
        TextView title=Aero.text(activity,welcome?"Nth Desktop XP":power?"Turn off computer":"Log Off Windows",welcome?21:20,Color.WHITE);Aero.bold(title);header.addView(title,new LinearLayout.LayoutParams(0,dp(activity,44),1));
        header.addView(new AeroIcon(activity,"orb"),new LinearLayout.LayoutParams(dp(activity,36),dp(activity,36)));panel.addView(header);
        View rule=new View(activity);rule.setBackgroundColor(0xffffd77a);panel.addView(rule,new LinearLayout.LayoutParams(-1,dp(activity,1)));
        LinearLayout content=Aero.column(activity);content.setGravity(Gravity.CENTER);content.setPadding(dp(activity,12),dp(activity,18),dp(activity,12),dp(activity,15));
        if(welcome){
            TextView greeting=Aero.text(activity,"welcome",34,Color.WHITE);Aero.bold(greeting);greeting.setGravity(Gravity.CENTER);content.addView(greeting,new LinearLayout.LayoutParams(-1,dp(activity,60)));
            TextView prompt=Aero.text(activity,"To begin, select your desktop",15,Color.WHITE);prompt.setGravity(Gravity.CENTER);content.addView(prompt,new LinearLayout.LayoutParams(-1,dp(activity,40)));
            LinearLayout account=new LinearLayout(activity);account.setGravity(Gravity.CENTER_VERTICAL);account.setPadding(dp(activity,10),dp(activity,8),dp(activity,10),dp(activity,8));account.setBackground(Aero.panel(activity,0xff8ca7ef,0xff6587dc,4,0xffc2d1ff));account.setFocusable(true);account.setContentDescription("Return to desktop");
            account.addView(ProfilePortrait.view(activity,store),new LinearLayout.LayoutParams(dp(activity,52),dp(activity,52)));TextView name=Aero.text(activity,store.prefs.getString("desktop_name",activity.getString(R.string.app_name)),18,Color.WHITE);name.setPadding(dp(activity,12),0,0,0);name.setMaxLines(2);account.addView(name,new LinearLayout.LayoutParams(0,-2,1));account.setOnClickListener(v->dialog.dismiss());content.addView(account,new LinearLayout.LayoutParams(-1,dp(activity,76)));
        }else{
            LinearLayout choices=new LinearLayout(activity);choices.setGravity(Gravity.CENTER);
            if(power){choice(choices,"Stand By","Open Android lock settings",0,()->{dialog.dismiss();host.action("security");});choice(choices,"Turn Off","Choose Android Home",1,()->{dialog.dismiss();host.action("home");});choice(choices,"Restart","Restart Nth Desktop XP",2,()->{dialog.dismiss();host.action("restart");});}
            else{choice(choices,"Switch User","Choose Android Home",2,()->{dialog.dismiss();host.action("home");});choice(choices,"Log Off","Return to Welcome",1,()->{dialog.dismiss();host.action("welcome");});}
            content.addView(choices,new LinearLayout.LayoutParams(-1,dp(activity,126)));
        }
        TextView note=Aero.text(activity,welcome?"Your apps and windows are kept while you’re away.":"Nth Desktop controls your launcher session. Android manages device power and security.",12,Color.WHITE);note.setGravity(Gravity.CENTER);note.setPadding(dp(activity,4),dp(activity,10),dp(activity,4),0);content.addView(note,new LinearLayout.LayoutParams(-1,-2));
        ScrollView scroll=new ScrollView(activity);scroll.setFillViewport(true);scroll.addView(content);panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout footer=new LinearLayout(activity);footer.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);footer.setPadding(dp(activity,12),dp(activity,7),dp(activity,12),dp(activity,7));footer.setBackgroundColor(0xff0b267d);TextView cancel=Aero.button(activity,welcome?"Turn off computer…":"Cancel",()->{dialog.dismiss();if(welcome)host.action("power");});footer.addView(cancel,new LinearLayout.LayoutParams(welcome?dp(activity,175):dp(activity,86),dp(activity,34)));panel.addView(footer);
        dialog.setContentView(panel);Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attrs=w.getAttributes();attrs.dimAmount=.5f;w.setAttributes(attrs);}
        dialog.setCanceledOnTouchOutside(!welcome);
        if(welcome)dialog.setOnKeyListener((d,key,event)->key==KeyEvent.KEYCODE_BACK);
        return dialog;
    }
    static void size(Dialog dialog,Activity activity,String mode){
        android.util.DisplayMetrics m=activity.getResources().getDisplayMetrics();int width=Math.min(dp(activity,mode.equals("welcome")?540:430),m.widthPixels-dp(activity,20));int height=Math.min(dp(activity,mode.equals("welcome")?420:330),m.heightPixels-dp(activity,48));
        dialog.getWindow().setLayout(Math.max(1,width),Math.max(1,height));
    }
    private static void choice(LinearLayout row,String label,String detail,int kind,Runnable action){
        android.content.Context c=row.getContext();LinearLayout item=Aero.column(c);item.setGravity(Gravity.CENTER);item.setFocusable(true);item.setClickable(true);item.setContentDescription(label+": "+detail);item.setBackground(Aero.selection(c));item.setOnClickListener(v->action.run());
        SessionIcon icon=new SessionIcon(c,kind);icon.setDuplicateParentStateEnabled(true);item.addView(icon,new LinearLayout.LayoutParams(dp(c,46),dp(c,46)));TextView title=Aero.text(c,label,14,Color.WHITE);Aero.bold(title);title.setGravity(Gravity.CENTER);item.addView(title,new LinearLayout.LayoutParams(-1,dp(c,28)));TextView hint=Aero.text(c,detail,10,Color.WHITE);hint.setGravity(Gravity.CENTER);hint.setMaxLines(3);item.addView(hint,new LinearLayout.LayoutParams(-1,dp(c,40)));row.addView(item,new LinearLayout.LayoutParams(0,-1,1));
    }
    private static int dp(android.content.Context c,int n){return Aero.dp(c,n);}
    private static final class SessionIcon extends View {
        private final int kind;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        SessionIcon(android.content.Context c,int kind){super(c);this.kind=kind;}
        @Override protected void onDraw(Canvas canvas){int save=canvas.save();canvas.scale(getWidth()/46f,getHeight()/46f);int[] colors=kind==0?new int[]{0xffffe496,0xffe9a227,0xffbf7813}:kind==1?new int[]{0xffffb2a0,0xffe65a32,0xffbb301c}:new int[]{0xffbee99e,0xff73ad45,0xff407f25};p.setShader(new LinearGradient(0,3,0,43,colors,null,Shader.TileMode.CLAMP));canvas.drawRoundRect(2,2,44,44,5,5,p);p.setShader(null);p.setColor(0xfff8f6e8);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);canvas.drawRoundRect(2,2,44,44,5,5,p);p.setStrokeWidth(3);p.setColor(Color.WHITE);
            if(kind==0){canvas.drawCircle(23,23,11,p);canvas.drawLine(23,15,23,23,p);canvas.drawLine(23,23,29,23,p);}else if(kind==1){canvas.drawArc(12,12,34,34,-55,290,false,p);canvas.drawLine(23,9,23,23,p);}else{canvas.drawArc(11,12,34,35,-45,285,false,p);p.setStyle(Paint.Style.FILL);Path arrow=new Path();arrow.moveTo(34,10);arrow.lineTo(34,22);arrow.lineTo(23,15);arrow.close();canvas.drawPath(arrow,p);}p.setStyle(Paint.Style.FILL);canvas.restoreToCount(save);}
    }
    private XpSessionDialogs(){}
}
