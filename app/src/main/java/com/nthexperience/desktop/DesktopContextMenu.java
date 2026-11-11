package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.view.*;
import android.widget.*;

/** Pointer-anchored XP menu shared by touch, keyboard and mouse. */
final class DesktopContextMenu {
    private final Context context;
    private final LinearLayout items;
    private final PopupWindow popup;
    private View owner;
    private final int rowHeight,menuWidth;
    private final View.OnLayoutChangeListener bounds=(v,l,t,r,b,ol,ot,or,ob)->{if(r-l!=or-ol||b-t!=ob-ot)dismiss();};
    private final View.OnAttachStateChangeListener lifetime=new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View v){}public void onViewDetachedFromWindow(View v){dismiss();}};
    DesktopContextMenu(Context context){this(context,"Desktop context menu");}
    DesktopContextMenu(Context context,String description){this(context,description,32,238);}
    DesktopContextMenu(Context context,String description,int rowHeight,int menuWidth){
        this.context=context;this.rowHeight=rowHeight;this.menuWidth=menuWidth;items=Aero.column(context);items.setContentDescription(description);
        ScrollView scroll=new ScrollView(context);scroll.setFillViewport(false);scroll.setBackground(Aero.panel(context,Color.WHITE,Color.WHITE,0,0xff808080));scroll.setPadding(dp(2),dp(2),dp(2),dp(2));scroll.addView(items);
        popup=new PopupWindow(scroll,dp(menuWidth),-2,true);popup.setBackgroundDrawable(Aero.panel(context,Color.WHITE,Color.WHITE,0,0xff808080));popup.setOutsideTouchable(true);popup.setElevation(dp(2));popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);popup.setOnDismissListener(()->{if(owner!=null){owner.removeOnAttachStateChangeListener(lifetime);owner.removeOnLayoutChangeListener(bounds);owner=null;}});
    }
    void item(String title,String icon,Runnable action){item(title,icon,true,action);}
    void item(String title,String icon,boolean enabled,Runnable action){
        LinearLayout row=new LinearLayout(context);row.setGravity(Gravity.CENTER_VERTICAL);row.setFocusable(true);row.setContentDescription(title);
        row.setBackground(XpControls.menuSelection(context));
        FrameLayout gutter=new FrameLayout(context);gutter.setBackgroundColor(Color.TRANSPARENT);View image;
        if(icon.equals("empty")){image=new View(context);}else if(icon.equals("checked")){image=new CheckMark(context);}else image=new AeroIcon(context,icon);
        image.setDuplicateParentStateEnabled(true);gutter.setDuplicateParentStateEnabled(true);gutter.addView(image,new FrameLayout.LayoutParams(dp(16),dp(16),Gravity.CENTER));row.addView(gutter,new LinearLayout.LayoutParams(dp(27),-1));
        TextView label=Aero.text(context,title,rowHeight<36?12:13,Color.BLACK);label.setPadding(dp(5),0,dp(9),0);label.setSingleLine();label.setEllipsize(android.text.TextUtils.TruncateAt.END);label.setTextColor(XpControls.menuInk(Color.BLACK));label.setDuplicateParentStateEnabled(true);row.addView(label,new LinearLayout.LayoutParams(0,-1,1));row.setEnabled(enabled);row.setFocusable(enabled);row.setOnClickListener(v->{if(enabled){dismiss();action.run();}});items.addView(row,new LinearLayout.LayoutParams(-1,dp(rowHeight)));
    }
    void section(String title){
        TextView label=Aero.text(context,title,11,0xff345974);
        label.setPadding(dp(10),0,dp(6),0);
        label.setBackgroundColor(XpTheme.surface(context));
        items.addView(label,new LinearLayout.LayoutParams(-1,dp(23)));
    }
    void checked(String title,boolean checked,Runnable action){item(title,checked?"checked":"empty",action);}
    void submenu(String title,String icon,Runnable action){item(title,icon,action);LinearLayout row=(LinearLayout)items.getChildAt(items.getChildCount()-1);TextView arrow=Aero.text(context,"▸",12,Color.BLACK);arrow.setTextColor(XpControls.menuInk(Color.BLACK));arrow.setDuplicateParentStateEnabled(true);row.addView(arrow,new LinearLayout.LayoutParams(dp(20),-1));}
    void separator(){LinearLayout row=new LinearLayout(context);row.setPadding(dp(28),dp(3),dp(3),dp(3));View line=new View(context);line.setBackgroundColor(0xffaca899);row.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));items.addView(row,new LinearLayout.LayoutParams(-1,dp(7)));}
    void show(View root,float screenX,float screenY,int leftInset,int topInset,int rightInset,int bottomInset){
        if(owner!=null){owner.removeOnAttachStateChangeListener(lifetime);owner.removeOnLayoutChangeListener(bounds);}owner=root;root.addOnAttachStateChangeListener(lifetime);root.addOnLayoutChangeListener(bounds);int[] location=new int[2];root.getLocationOnScreen(location);int left=location[0]+leftInset+dp(3),top=location[1]+topInset+dp(3),right=location[0]+root.getWidth()-rightInset-dp(3),bottom=location[1]+root.getHeight()-bottomInset-dp(3);
        int width=Math.min(dp(menuWidth),Math.max(1,right-left));popup.getContentView().measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(Math.max(1,bottom-top),View.MeasureSpec.AT_MOST));int height=Math.min(popup.getContentView().getMeasuredHeight(),Math.max(1,bottom-top));popup.setWidth(width);popup.setHeight(height);
        int x=Math.max(left,Math.min(Math.round(screenX),right-width)),y=Math.max(top,Math.min(Math.round(screenY),bottom-height));popup.showAtLocation(root,Gravity.TOP|Gravity.LEFT,x,y);
    }
    boolean showing(){return popup.isShowing();}
    void dismiss(){popup.dismiss();}
    private static final class CheckMark extends View {
        final android.graphics.Paint p=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        CheckMark(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        protected void onDraw(android.graphics.Canvas c){int save=c.save();c.scale(getWidth()/16f,getHeight()/16f);boolean enabled=false,hot=false;for(int state:getDrawableState()){enabled|=state==android.R.attr.state_enabled;hot|=state==android.R.attr.state_pressed||state==android.R.attr.state_hovered||state==android.R.attr.state_focused||state==android.R.attr.state_selected;}p.setColor(!enabled?0xff808080:hot?Color.WHITE:Color.BLACK);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(1.8f);android.graphics.Path path=new android.graphics.Path();path.moveTo(3,8);path.lineTo(6,11);path.lineTo(13,4);c.drawPath(path,p);c.restoreToCount(save);}
    }
    private int dp(int value){return Aero.dp(context,value);}
}
