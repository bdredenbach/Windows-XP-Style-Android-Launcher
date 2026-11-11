package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.util.function.BooleanSupplier;

/** Original XP-era visual effects, driven only by the player's decoded PCM. */
final class XpMusicVisualizer extends View {
    static final String[] NAMES={"Bars and Waves — Bars","Bars and Waves — Scope","Ambience","Particle Rings"};
    private final MediaPlayback.Signal signal;
    private final BooleanSupplier playing;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final float[] levels=new float[24];
    private float[] last;
    private int mode;
    private float phase,energy;
    private long previous;
    XpMusicVisualizer(Context c,MediaPlayback.Signal signal,BooleanSupplier playing){super(c);this.signal=signal;this.playing=playing;setContentDescription("Music visualizer");setBackgroundColor(Color.BLACK);}
    void mode(int value){mode=Math.floorMod(value,NAMES.length);last=null;invalidate();}
    int mode(){return mode;}
    @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);previous=0;if(visibility==VISIBLE)invalidate();}
    @Override protected void onDraw(Canvas canvas){
        float[] samples=signal.samples();boolean active=playing.getAsBoolean()&&Aero.motion(getContext());
        long now=SystemClock.uptimeMillis();float delta=previous==0?0:Math.min(.06f,(now-previous)/1000f);previous=now;
        if(active||last==null){
            energy=0;for(float sample:samples)energy+=sample*sample;energy=(float)Math.sqrt(energy/Math.max(1,samples.length));
            if(samples!=last){float[] spectrum=mode==0?signal.spectrum():levels;for(int i=0;i<levels.length;i++)levels[i]=Math.max(levels[i]*.75f,Math.min(1,spectrum[i]*3));last=samples;}
            else if(active)for(int i=0;i<levels.length;i++)levels[i]*=.97f;
            if(active&&energy>.0001f)phase+=delta*(.6f+energy*2);
        }
        p.setStyle(Paint.Style.FILL);p.setShader(null);p.setAlpha(255);
        switch(mode){case 0:bars(canvas);break;case 1:scope(canvas,samples);break;case 2:ambience(canvas,samples);break;default:particles(canvas,samples);}
        if(active&&isShown())postInvalidateDelayed(33);
    }
    private void bars(Canvas c){
        float w=getWidth(),h=getHeight(),base=h*.8f,cell=w/(levels.length+2);
        p.setShader(new LinearGradient(0,h*.08f,0,base,new int[]{0xffffdc54,0xff8aff67,0xff23a5ff},null,Shader.TileMode.CLAMP));
        for(int i=0;i<levels.length;i++){
            float size=Math.max(2,levels[i]*h*.67f),left=cell*(i+1),right=left+cell*.72f;
            p.setAlpha(245);c.drawRect(left,base-size,right,base,p);p.setAlpha(48);c.drawRect(left,base+3,right,Math.min(h,base+3+size*.24f),p);
        }
        p.setShader(null);p.setColor(0xaa000000);for(float y=base;y>h*.06f;y-=6)c.drawRect(0,y,w,y+2,p);
    }
    private void scope(Canvas c,float[] values){
        float w=getWidth(),h=getHeight();p.setColor(0xff101d35);p.setStrokeWidth(1);
        for(int i=1;i<8;i++){c.drawLine(0,h*i/8,w,h*i/8,p);c.drawLine(w*i/8,0,w*i/8,h,p);}
        for(int trace=0;trace<3;trace++){
            path.rewind();for(int i=0;i<values.length;i++){float x=w*i/Math.max(1,values.length-1),y=h*.5f+values[i]*h*(.38f-trace*.07f);if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}
            p.setStyle(Paint.Style.STROKE);p.setColor(trace==2?0xfff4ffff:trace==1?0xff58dfff:0xff1760aa);p.setStrokeWidth(trace==0?7:trace==1?3:1);c.drawPath(path,p);
        }p.setStyle(Paint.Style.FILL);
    }
    private void ambience(Canvas c,float[] values){
        float w=getWidth(),h=getHeight(),r=Math.max(1,Math.min(w,h));
        p.setShader(new RadialGradient(w*.5f,h*.5f,r*.7f,new int[]{0xff25275b,0xff070715,Color.BLACK},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
        for(int ribbon=0;ribbon<7;ribbon++){
            path.rewind();for(int i=0;i<values.length;i++){float x=w*i/(values.length-1f),y=h*.5f+(float)Math.sin(i*.035+phase+ribbon*.6)*h*(.08f+energy*.35f)+values[i]*h*.18f+(ribbon-3)*h*.04f;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}
            p.setColor(Color.HSVToColor(180,new float[]{(phase*24+ribbon*36)%360,.8f,1}));p.setStrokeWidth(2+ribbon*.3f);p.setStyle(Paint.Style.STROKE);c.drawPath(path,p);
        }p.setStyle(Paint.Style.FILL);
    }
    private void particles(Canvas c,float[] values){
        float cx=getWidth()*.5f,cy=getHeight()*.5f,r=Math.min(cx,cy)*.62f;
        for(int ring=0;ring<3;ring++)for(int i=0;i<48;i++){
            double angle=i*Math.PI/24+phase*(ring%2==0?1:-1)*.35;
            float sample=Math.abs(values[(i*5+ring*17)%values.length]);float radius=r*(.65f+ring*.22f)+sample*r*.5f;
            p.setColor(Color.HSVToColor(210,new float[]{(i*7+ring*48+phase*20)%360,.65f,1}));
            c.drawCircle(cx+(float)Math.cos(angle)*radius,cy+(float)Math.sin(angle)*radius,(1.4f+sample*4)*getResources().getDisplayMetrics().density,p);
        }
    }
}
