package com.nthexperience.desktop;

import android.graphics.*;

/** Original XP-era desktop objects in the launcher's 64-unit icon coordinate space. */
final class XpArtwork {
    static boolean draw(Canvas c,String kind){
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        if(kind.equals("computer")){
            box(c,p,43,13,58,52,0xfff2f0e8,0xff9c9b95);p.setColor(0xff64758a);c.drawRect(47,19,54,22,p);c.drawRect(47,26,54,28,p);p.setColor(0xff54b244);c.drawCircle(51,46,2,p);
            box(c,p,5,10,44,43,0xfff7f5ed,0xff8d929a);box(c,p,9,14,40,36,0xff344866,0xff121d37);
            p.setShader(new LinearGradient(12,16,37,34,0xff46a5f1,0xff0048a1,Shader.TileMode.CLAMP));c.drawRect(11,16,38,34,p);p.setShader(null);
            p.setColor(0xffb6b9b8);c.drawRect(20,43,28,51,p);box(c,p,13,50,37,55,0xffe7e7e1,0xff8f949a);return true;
        }
        if(kind.equals("files")||kind.equals("folderapps")||kind.equals("documents")||kind.equals("pictures")||kind.equals("music")||kind.equals("downloads")||kind.equals("videos")){
            Path folder=new Path();folder.moveTo(5,17);folder.lineTo(25,17);folder.lineTo(30,22);folder.lineTo(58,22);folder.lineTo(56,53);folder.lineTo(6,53);folder.close();
            p.setShader(new LinearGradient(0,16,0,54,0xffffe8a2,0xffdc9927,Shader.TileMode.CLAMP));c.drawPath(folder,p);p.setShader(null);p.setColor(0xffb77b1e);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);c.drawPath(folder,p);p.setStyle(Paint.Style.FILL);
            if(!kind.equals("files")){box(c,p,17,12,46,44,0xffffffff,0xffcfdae4);p.setColor(0xff5085bc);p.setStrokeWidth(2);for(int y=20;y<38;y+=6)c.drawLine(22,y,41,y,p);}
            Path front=new Path();front.moveTo(8,31);front.lineTo(59,27);front.lineTo(54,54);front.lineTo(4,54);front.close();p.setShader(new LinearGradient(0,28,0,54,0xffffe995,0xfff1b741,Shader.TileMode.CLAMP));c.drawPath(front,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xffbe8627);c.drawPath(front,p);p.setStyle(Paint.Style.FILL);return true;
        }
        if(kind.equals("recycle")||kind.equals("recycle-full")){
            Path bin=new Path();bin.moveTo(15,18);bin.lineTo(50,18);bin.lineTo(45,56);bin.lineTo(20,56);bin.close();p.setShader(new LinearGradient(15,0,50,0,0xffeaf5ff,0xff8fa5b7,Shader.TileMode.CLAMP));c.drawPath(bin,p);p.setShader(null);
            p.setColor(0xff7e93a5);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);for(int i=0;i<5;i++)c.drawLine(20+i*6,20,23+i*4,53,p);for(int y=25;y<55;y+=7)c.drawLine(18,y,47,y,p);p.setStyle(Paint.Style.FILL);
            if(kind.equals("recycle-full")){box(c,p,22,9,38,28,0xffffffff,0xffc4ced9);box(c,p,34,12,47,28,0xfffaf8ed,0xffbac5ce);}
            box(c,p,13,14,52,20,0xfff3f9ff,0xff8b9da9);p.setColor(0xff258c45);p.setTextSize(23);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("↻",22,47,p);return true;
        }
        return false;
    }
    private static void box(Canvas c,Paint p,float l,float t,float r,float b,int top,int bottom){p.setShader(new LinearGradient(l,t,r,b,top,bottom,Shader.TileMode.CLAMP));c.drawRoundRect(l,t,r,b,2,2,p);p.setShader(null);}
    private XpArtwork(){}
}
