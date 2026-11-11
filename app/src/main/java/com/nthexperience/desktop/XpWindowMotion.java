package com.nthexperience.desktop;

import android.animation.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.view.animation.DecelerateInterpolator;

/** XP's moving caption, rather than scaling a whole live window into a thumbnail. */
final class XpWindowMotion {
    private final View owner;
    private ValueAnimator animator;
    private ViewGroup scene;
    private Caption caption;
    XpWindowMotion(View owner){this.owner=owner;}
    boolean running(){return animator!=null;}
    void cancel(){
        if(animator!=null){ValueAnimator previous=animator;animator=null;previous.cancel();}
        if(scene!=null&&caption!=null)scene.getOverlay().remove(caption);
        caption=null;scene=null;
    }
    Rect captionBounds(){
        int[] at=new int[2];owner.getLocationOnScreen(at);
        return new Rect(at[0],at[1],at[0]+Math.max(1,owner.getWidth()),at[1]+Aero.dp(owner.getContext(),XpMetrics.CAPTION));
    }
    void play(Rect from,Rect to,String title,Runnable finished){
        cancel();
        if(!Aero.motion(owner.getContext())||!owner.isAttachedToWindow()||!(owner.getRootView() instanceof ViewGroup)){finished.run();return;}
        scene=(ViewGroup)owner.getRootView();int[] origin=new int[2];scene.getLocationOnScreen(origin);
        Rect start=new Rect(from),end=new Rect(to);start.offset(-origin[0],-origin[1]);end.offset(-origin[0],-origin[1]);
        caption=new Caption(title);scene.getOverlay().add(caption);
        Caption drawing=caption;ViewGroup overlay=scene;
        animator=ValueAnimator.ofFloat(0,1);ValueAnimator current=animator;
        current.setDuration(210);current.setInterpolator(new DecelerateInterpolator(1.1f));
        current.addUpdateListener(value->{float f=(float)value.getAnimatedValue();drawing.setBounds(lerp(start.left,end.left,f),lerp(start.top,end.top,f),lerp(start.right,end.right,f),lerp(start.bottom,end.bottom,f));overlay.invalidate();});
        current.addListener(new AnimatorListenerAdapter(){boolean canceled;
            @Override public void onAnimationCancel(Animator a){canceled=true;}
            @Override public void onAnimationEnd(Animator a){overlay.getOverlay().remove(drawing);if(animator==current){animator=null;caption=null;scene=null;}if(!canceled)finished.run();}
        });current.start();
    }
    private static int lerp(int a,int b,float f){return Math.round(a+(b-a)*f);}
    private final class Caption extends Drawable {
        final String text;final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Drawable chrome=XpTheme.chrome(owner.getContext(),AeroGlass.WINDOW);
        Caption(String text){this.text=text;}
        @Override public void draw(Canvas canvas){Rect b=getBounds();int save=canvas.save();canvas.clipRect(b);chrome.setBounds(b);chrome.draw(canvas);paint.setColor(XpTheme.titleInk(owner.getContext()));paint.setTypeface(Aero.strong(owner.getContext()));paint.setTextSize(Aero.dp(owner.getContext(),13));Paint.FontMetrics fm=paint.getFontMetrics();canvas.drawText(text,b.left+Aero.dp(owner.getContext(),8),b.exactCenterY()-(fm.ascent+fm.descent)/2,paint);canvas.restoreToCount(save);}
        @Override public void setAlpha(int a){paint.setAlpha(a);}
        @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
}
