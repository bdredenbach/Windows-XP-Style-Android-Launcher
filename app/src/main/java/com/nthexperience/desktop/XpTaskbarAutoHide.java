package com.nthexperience.desktop;

import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import java.util.function.BooleanSupplier;

/** The bar overlays the work area. Only changing the setting changes its reservation. */
final class XpTaskbarAutoHide {
    static final String KEY="xp_taskbar_auto_hide",LOCK="xp_taskbar_locked";
    private final FrameLayout root;private final View bar,slot,edge;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final BooleanSupplier occupied;
    private boolean enabled,shown=true,running=true,touching,hovering,closed;
    private int barHeight,left,right,bottom;
    private final Runnable hide=new Runnable(){public void run(){
        if(closed||!enabled||!running)return;
        if(touching||hovering||occupied.getAsBoolean()){main.postDelayed(this,250);return;}
        shown=false;bar.setVisibility(View.INVISIBLE);edge.setVisibility(View.VISIBLE);
    }};
    XpTaskbarAutoHide(FrameLayout root,View bar,View slot,BooleanSupplier occupied){
        this.root=root;this.bar=bar;this.slot=slot;this.occupied=occupied;
        edge=new View(root.getContext());edge.setContentDescription("Reveal taskbar");edge.setFocusable(true);edge.setBackground(XpTheme.chrome(root.getContext(),AeroGlass.TASKBAR));edge.setElevation(Aero.dp(root.getContext(),8));edge.setOnClickListener(v->reveal());
        edge.setOnTouchListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_DOWN){reveal();return true;}return true;});
        edge.setOnHoverListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_HOVER_ENTER)reveal();return false;});
        root.addView(edge,new FrameLayout.LayoutParams(-1,Aero.dp(root.getContext(),6),Gravity.BOTTOM));edge.setVisibility(View.GONE);
        bar.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(b-t>0&&barHeight!=b-t){barHeight=b-t;layout();}});
    }
    void configure(boolean value,int height){
        boolean changed=enabled!=value;enabled=value;barHeight=height;layout();
        if(changed||!enabled)reveal();else schedule();
    }
    void insets(int left,int right,int bottom){this.left=left;this.right=right;this.bottom=bottom;layout();}
    private void layout(){
        int reservation=enabled?Aero.dp(root.getContext(),3):barHeight;
        ViewGroup.LayoutParams reserve=slot.getLayoutParams();if(reserve.height!=reservation){reserve.height=reservation;slot.setLayoutParams(reserve);}
        FrameLayout.LayoutParams position=(FrameLayout.LayoutParams)bar.getLayoutParams();
        if(position.height!=barHeight||position.leftMargin!=left||position.rightMargin!=right||position.bottomMargin!=bottom){position.height=barHeight;position.leftMargin=left;position.rightMargin=right;position.bottomMargin=bottom;bar.setLayoutParams(position);}
        FrameLayout.LayoutParams reveal=(FrameLayout.LayoutParams)edge.getLayoutParams();reveal.leftMargin=left;reveal.rightMargin=right;reveal.bottomMargin=bottom;edge.setLayoutParams(reveal);
    }
    int overlap(){return enabled?Math.max(0,barHeight-Aero.dp(root.getContext(),3)):0;}
    boolean enabled(){return enabled;}boolean shown(){return shown;}
    void reveal(){if(closed)return;shown=true;bar.setVisibility(View.VISIBLE);edge.setVisibility(View.GONE);schedule();}
    void observe(MotionEvent e){
        if(!enabled||closed)return;int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){touching=true;main.removeCallbacks(hide);}
        if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){touching=false;schedule();}
    }
    void hover(MotionEvent e){
        if(!enabled||closed)return;int[] at=new int[2];bar.getLocationOnScreen(at);hovering=e.getActionMasked()!=MotionEvent.ACTION_HOVER_EXIT&&e.getRawX()>=at[0]&&e.getRawX()<at[0]+bar.getWidth()&&e.getRawY()>=at[1]&&e.getRawY()<at[1]+bar.getHeight();
        if(hovering){reveal();main.removeCallbacks(hide);}else schedule();
    }
    private void schedule(){main.removeCallbacks(hide);if(enabled&&running&&!closed&&!touching&&!hovering)main.postDelayed(hide,1800);}
    void resume(){running=true;reveal();}
    void pause(){running=false;touching=hovering=false;main.removeCallbacks(hide);}
    void close(){closed=true;main.removeCallbacksAndMessages(null);}
}
