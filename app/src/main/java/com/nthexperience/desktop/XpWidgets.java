package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.*;

/** XP trackbars and gauges retain Android's value, keyboard and accessibility behavior. */
final class XpWidgets {
    static SeekBar seek(Context c){SeekBar bar=new SeekBar(c);style(bar);return bar;}
    static void style(SeekBar bar){Context c=bar.getContext();bar.setProgressTintList(null);bar.setProgressBackgroundTintList(null);bar.setSecondaryProgressTintList(null);bar.setThumbTintList(null);bar.setBackground(null);bar.setSplitTrack(false);bar.setProgressDrawable(new Track(c));bar.setThumb(new Thumb(c));bar.setThumbOffset(Aero.dp(c,5));bar.setPadding(Aero.dp(c,11),Aero.dp(c,5),Aero.dp(c,11),Aero.dp(c,5));}
    static RadioButton radio(Context c){RadioButton b=new RadioButton(c);b.setButtonTintList(null);b.setButtonDrawable(new AeroCheck(c,true));b.setTypeface(Aero.face(c));b.setTextSize(13);b.setTextColor(Aero.INK);return b;}
    static CheckBox check(Context c){CheckBox b=new CheckBox(c);b.setButtonTintList(null);b.setButtonDrawable(new AeroCheck(c,false));b.setTypeface(Aero.face(c));b.setTextSize(13);b.setTextColor(Aero.INK);return b;}
    static ProgressBar progress(Context c){ProgressBar p=new ProgressBar(c,null,android.R.attr.progressBarStyleHorizontal);p.setProgressTintList(null);p.setSecondaryProgressTintList(null);p.setProgressBackgroundTintList(null);p.setProgressDrawable(new Gauge(c));return p;}
    private abstract static class Art extends Drawable {
        final Context c;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final float d;
        Art(Context c){this.c=c;d=c.getResources().getDisplayMetrics().density;}
        public void setAlpha(int alpha){p.setAlpha(alpha);}public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
    static final class Track extends Art {
        Track(Context c){super(c);}
        public void draw(Canvas canvas){Rect b=getBounds();float y=b.exactCenterY();p.setStyle(Paint.Style.FILL);p.setColor(0xff9d9c99);canvas.drawRect(b.left,y-2*d,b.right,y+d,p);p.setColor(Color.WHITE);canvas.drawRect(b.left+d,y,b.right,y+2*d,p);p.setColor(0xff716f64);for(int i=0;i<=10;i++){float x=b.left+(b.width()-d)*i/10f;canvas.drawRect(x,y+8*d,x+d,y+10*d,p);}}
    }
    static final class Thumb extends Art {
        Thumb(Context c){super(c);}
        public int getIntrinsicWidth(){return Math.round(11*d);}public int getIntrinsicHeight(){return Math.round(20*d);}
        public boolean isStateful(){return true;}protected boolean onStateChange(int[] state){invalidateSelf();return true;}
        public void draw(Canvas canvas){Rect b=getBounds();boolean enabled=false,hot=false,pressed=false;for(int s:getState()){enabled|=s==android.R.attr.state_enabled;hot|=s==android.R.attr.state_focused||s==android.R.attr.state_hovered;pressed|=s==android.R.attr.state_pressed;}Path shape=new Path();shape.moveTo(b.left+.5f*d,b.top+.5f*d);shape.lineTo(b.right-.5f*d,b.top+.5f*d);shape.lineTo(b.right-.5f*d,b.bottom-5*d);shape.lineTo(b.exactCenterX(),b.bottom-.5f*d);shape.lineTo(b.left+.5f*d,b.bottom-5*d);shape.close();p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(b.left,0,b.right,0,pressed?0xffcecbbf:Color.WHITE,0xffdfddcf,Shader.TileMode.CLAMP));canvas.drawPath(shape,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d);p.setColor(!enabled?0xffb4b2a8:XpTheme.scheme(c)==5?0xff808080:0xff4b6e45);canvas.drawPath(shape,p);p.setStyle(Paint.Style.FILL);p.setColor(!enabled?0xffc9c7bc:hot?0xffffc73c:0xff81b466);canvas.drawRect(b.left+2*d,b.top+2*d,b.right-2*d,b.top+4*d,p);}
    }
    static final class Gauge extends Art {
        Gauge(Context c){super(c);}
        protected boolean onLevelChange(int level){invalidateSelf();return true;}
        public void draw(Canvas canvas){Rect b=getBounds();if(b.height()<6*d){p.setColor(0xffd4d0c8);canvas.drawRect(b,p);p.setColor(0xff38a128);canvas.drawRect(b.left,b.top,b.left+b.width()*getLevel()/10000f,b.bottom,p);return;}float h=Math.min(b.height(),14*d),top=b.exactCenterY()-h/2;p.setStyle(Paint.Style.FILL);p.setColor(0xffb0b1a3);canvas.drawRect(b.left,top,b.right,top+h,p);p.setColor(Color.WHITE);canvas.drawRect(b.left+d,top+d,b.right-d,top+h-d,p);float end=b.left+d+(b.width()-2*d)*getLevel()/10000f;int save=canvas.save();canvas.clipRect(b.left+d,top+d,end,top+h-d);p.setShader(new LinearGradient(0,top,0,top+h,0xffb8ec99,0xff38a128,Shader.TileMode.CLAMP));for(float x=b.left+2*d;x<end;x+=8*d)canvas.drawRect(x,top+2*d,x+6*d,top+h-2*d,p);p.setShader(null);canvas.restoreToCount(save);}
    }
    /** Rotates a real SeekBar, including its touch coordinates. Up always increases volume. */
    static final class Vertical extends SeekBar {
        private OnSeekBarChangeListener listener;private boolean tracking;
        Vertical(Context c){super(c);XpWidgets.style(this);setPadding(Aero.dp(c,10),Aero.dp(c,6),Aero.dp(c,10),Aero.dp(c,6));}
        protected synchronized void onMeasure(int w,int h){super.onMeasure(h,w);setMeasuredDimension(getMeasuredHeight(),getMeasuredWidth());}
        protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(h,w,oldh,oldw);}
        protected synchronized void onDraw(Canvas c){int save=c.save();c.rotate(-90);c.translate(-getHeight(),0);super.onDraw(c);c.restoreToCount(save);}
        @Override public void setOnSeekBarChangeListener(OnSeekBarChangeListener value){listener=value;super.setOnSeekBarChangeListener(value);}
        private void userProgress(int value){int before=getProgress();setProgress(value);if(before!=getProgress()&&listener!=null)listener.onProgressChanged(this,getProgress(),true);}
        @Override public boolean onTouchEvent(MotionEvent event){if(!isEnabled())return false;switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:tracking=true;setPressed(true);if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(true);if(listener!=null)listener.onStartTrackingTouch(this);break;
            case MotionEvent.ACTION_MOVE:if(!tracking)return false;break;
            case MotionEvent.ACTION_UP:if(!tracking)return false;break;
            case MotionEvent.ACTION_CANCEL:if(tracking&&listener!=null)listener.onStopTrackingTouch(this);tracking=false;setPressed(false);return true;
            default:return true;
        }float padding=Aero.dp(getContext(),10),fraction=(getHeight()-padding-event.getY())/Math.max(1,getHeight()-2*padding);userProgress(Math.round(getMax()*Math.max(0,Math.min(1,fraction))));if(event.getActionMasked()==MotionEvent.ACTION_UP){tracking=false;setPressed(false);if(listener!=null)listener.onStopTrackingTouch(this);performClick();}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
        @Override public synchronized void setProgress(int value){super.setProgress(value);onSizeChanged(getWidth(),getHeight(),getWidth(),getHeight());invalidate();}
        @Override public boolean onKeyDown(int code,KeyEvent event){if(code==KeyEvent.KEYCODE_DPAD_UP||code==KeyEvent.KEYCODE_DPAD_DOWN||code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT){if(!isEnabled())return false;userProgress(getProgress()+((code==KeyEvent.KEYCODE_DPAD_UP||code==KeyEvent.KEYCODE_DPAD_RIGHT)?1:-1)*Math.max(1,getKeyProgressIncrement()));return true;}return super.onKeyDown(code,event);}
    }
    private XpWidgets(){}
}
