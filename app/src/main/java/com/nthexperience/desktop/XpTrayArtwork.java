package com.nthexperience.desktop;

import android.graphics.*;

/** Original 16-unit XP notification artwork, distinct from 32-unit desktop icons. */
final class XpTrayArtwork {
    static void draw(Canvas c,int kind,int level,boolean charging,boolean online,boolean muted){draw(c,kind,level,charging,online,muted,false);}
    static void draw(Canvas c,int kind,int level,boolean charging,boolean online,boolean muted,boolean limited){Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setStrokeWidth(1);
        if(kind==0){box(c,p,2,4,14,14,0xff34492b);box(c,p,6,2,10,4,0xffc4bda6);box(c,p,3,5,13,13,0xffe9e4ce);if(level>=0)box(c,p,4,12-6*Math.max(0,Math.min(100,level))/100f,12,12,level<=15?0xffd63c28:0xff65ad33);else{p.setColor(0xff55513e);p.setTextSize(9);c.drawText("?",5,12,p);}if(charging){p.setColor(0xffffcc35);Path bolt=new Path();bolt.moveTo(10,1);bolt.lineTo(6,8);bolt.lineTo(9,8);bolt.lineTo(7,15);bolt.lineTo(13,6);bolt.lineTo(10,6);bolt.close();c.drawPath(bolt,p);}}
        else if(kind==1){monitor(c,p,1,1);monitor(c,p,7,7);if(!online)cross(c,p,10,10);else if(limited){p.setColor(0xffffdb3d);Path warning=new Path();warning.moveTo(11,7);warning.lineTo(16,15);warning.lineTo(6,15);warning.close();c.drawPath(warning,p);p.setColor(Color.BLACK);c.drawRect(10.5f,10,11.5f,12.5f,p);c.drawCircle(11,14,.5f,p);}else{box(c,p,3,11,6,13,0xff36b53a);box(c,p,5,12,9,13,0xff25672e);}}
        else if(kind==2){p.setShader(new LinearGradient(2,4,11,12,0xffffed85,0xffc58b18,Shader.TileMode.CLAMP));Path speaker=new Path();speaker.moveTo(1,6);speaker.lineTo(5,6);speaker.lineTo(9,2);speaker.lineTo(9,14);speaker.lineTo(5,10);speaker.lineTo(1,10);speaker.close();c.drawPath(speaker,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setColor(0xff78542a);c.drawPath(speaker,p);p.setStyle(Paint.Style.FILL);box(c,p,2,6,4,10,0xffc5943c);if(muted)cross(c,p,10,6);else{p.setStyle(Paint.Style.STROKE);p.setColor(0xff172e5e);c.drawArc(8,4,13,12,-65,130,false,p);c.drawArc(8,1,16,15,-65,130,false,p);p.setStyle(Paint.Style.FILL);}}
        else if(kind==3){p.setColor(0xff9b6f12);p.setStyle(Paint.Style.STROKE);for(int i=0;i<8;i++){double a=i*Math.PI/4;c.drawLine(8+5*(float)Math.cos(a),8+5*(float)Math.sin(a),8+7*(float)Math.cos(a),8+7*(float)Math.sin(a),p);}p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(7,6,5,0xfffff69c,0xffdb9f21,Shader.TileMode.CLAMP));c.drawCircle(8,8,4,p);p.setShader(null);}
        else if(kind==4){box(c,p,4,4,11,14,0xff837d6f);box(c,p,5,5,10,13,0xffe8e8e3);box(c,p,6,1,7,5,0xff625e57);box(c,p,9,1,10,5,0xff625e57);p.setColor(0xff16832e);Path arrow=new Path();arrow.moveTo(2,9);arrow.lineTo(2,6);arrow.lineTo(7,6);arrow.lineTo(7,3);arrow.lineTo(13,7.5f);arrow.lineTo(7,12);arrow.lineTo(7,9);arrow.close();c.drawPath(arrow,p);}
        else{monitor(c,p,3,2);box(c,p,5,4,10,7,0xff042917);box(c,p,6,5,7,7,0xff43dd5d);box(c,p,8,4,9,7,0xff43dd5d);}
    }
    private static void box(Canvas c,Paint p,float l,float t,float r,float b,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRect(l,t,r,b,p);}
    private static void monitor(Canvas c,Paint p,int x,int y){box(c,p,x,y,x+8,y+6,0xff575963);box(c,p,x+1,y+1,x+7,y+5,0xffa8c5f2);box(c,p,x+2,y+2,x+6,y+4,0xff3979c3);box(c,p,x+3,y+6,x+5,y+7,0xff737574);box(c,p,x+1,y+7,x+7,y+8,0xffb8b8b1);}
    private static void cross(Canvas c,Paint p,float x,float y){p.setColor(Color.WHITE);p.setStrokeWidth(3);c.drawLine(x,y,x+4,y+4,p);c.drawLine(x+4,y,x,y+4,p);p.setColor(0xffc52b21);p.setStrokeWidth(1.7f);c.drawLine(x,y,x+4,y+4,p);c.drawLine(x+4,y,x,y+4,p);p.setStrokeWidth(1);}
    private XpTrayArtwork(){}
}
