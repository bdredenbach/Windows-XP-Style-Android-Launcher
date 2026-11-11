package com.nthexperience.desktop;

import android.graphics.*;
import java.util.*;

/** Original small utility artwork in the XP palette; no downloaded program binaries. */
final class XpProgramArtwork {
    static boolean draw(Canvas c,String kind){if(!Arrays.asList("paint","wordpad","charmap","command","recorder","magnifier","narrator","keyboard","restore","transfer","minesweeper","solitaire").contains(kind))return false;Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setStrokeWidth(2);
        switch(kind){
            case "paint":p.setShader(new LinearGradient(0,10,55,55,0xffffeec6,0xffc78e4d,Shader.TileMode.CLAMP));c.drawOval(5,11,56,55,p);p.setShader(null);p.setColor(0xff796544);c.drawOval(31,36,45,48,p);int[] colors={0xffe83d2f,0xffffd42b,0xff45b145,0xff2766cc};for(int i=0;i<4;i++){p.setColor(colors[i]);c.drawCircle(15+i*10,23+(i%2)*6,5,p);}p.setColor(0xff6a421b);c.save();c.rotate(35,43,32);c.drawRect(40,6,46,45,p);p.setColor(0xffa6bdc7);c.drawRect(39,35,47,45,p);p.setColor(0xff305d9c);c.drawOval(38,43,48,59,p);c.restore();break;
            case "wordpad":case "charmap":p.setShader(new LinearGradient(0,5,0,57,Color.WHITE,0xffd8e8f7,Shader.TileMode.CLAMP));c.drawRoundRect(13,5,52,58,2,2,p);p.setShader(null);p.setColor(0xff4a7fab);p.setTypeface(Typeface.create("serif",Typeface.BOLD));p.setTextSize(kind.equals("wordpad")?29:26);c.drawText(kind.equals("wordpad")?"W":"Ω",18,36,p);p.setStrokeWidth(2);for(int y=43;y<=51;y+=4)c.drawLine(19,y,44,y,p);if(kind.equals("wordpad")){p.setColor(0xffc48328);c.save();c.rotate(30,47,36);c.drawRect(44,17,51,55,p);p.setColor(0xff303844);c.drawRect(44,50,51,59,p);c.restore();}break;
            case "command":p.setColor(0xff485f8f);c.drawRoundRect(5,10,59,52,3,3,p);p.setColor(0xfff0ede6);c.drawRect(7,13,57,20,p);p.setColor(Color.BLACK);c.drawRect(7,22,57,50,p);p.setColor(Color.WHITE);p.setTypeface(Typeface.MONOSPACE);p.setTextSize(17);c.drawText(">_",12,41,p);break;
            case "recorder":p.setColor(0xff75879e);c.drawRoundRect(7,25,57,52,4,4,p);p.setColor(0xff102c4d);c.drawRect(12,30,52,41,p);p.setColor(0xff65df86);for(int i=0;i<8;i++)c.drawRect(15+i*4,38-(i%4)*2,17+i*4,40,p);p.setColor(0xffdd3e2b);c.drawCircle(47,47,4,p);p.setColor(0xffc8d7dc);c.drawRoundRect(24,3,39,30,7,7,p);p.setColor(0xff5d718b);for(int y=7;y<25;y+=4)c.drawLine(27,y,36,y,p);c.drawRect(30,28,33,36,p);break;
            case "magnifier":p.setColor(0xff365a8b);p.setStrokeWidth(9);c.drawLine(37,38,56,58,p);p.setShader(new RadialGradient(24,24,20,new int[]{0xfff3fdff,0xffa5ddf5,0xff4783b7},null,Shader.TileMode.CLAMP));c.drawCircle(25,25,20,p);p.setShader(null);p.setColor(0xff506a87);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawCircle(25,25,20,p);break;
            case "keyboard":p.setColor(0xff8b9dae);c.drawRoundRect(3,19,61,49,3,3,p);for(int y=0;y<3;y++)for(int x=0;x<10;x++){p.setColor(0xfff0eee4);c.drawRect(6+x*5,23+y*6,10+x*5,27+y*6,p);}p.setColor(0xffe6e8df);c.drawRect(18,42,45,46,p);break;
            case "solitaire":for(int n=0;n<2;n++){p.setColor(Color.WHITE);c.drawRoundRect(10+n*15,7+n*7,37+n*15,52+n*7,3,3,p);p.setColor(n==0?Color.BLACK:0xffc52d30);p.setTextSize(23);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText(n==0?"♠":"♥",13+n*15,36+n*7,p);}break;
            case "minesweeper":p.setColor(0xffb8c1c8);c.drawRoundRect(7,7,57,57,4,4,p);p.setColor(0xff303030);c.drawCircle(31,32,15,p);p.setStrokeWidth(3);for(int i=0;i<8;i++){double a=i*Math.PI/4;c.drawLine(31+11*(float)Math.cos(a),32+11*(float)Math.sin(a),31+23*(float)Math.cos(a),32+23*(float)Math.sin(a),p);}p.setColor(Color.WHITE);c.drawCircle(26,27,4,p);break;
            default:p.setColor(0xffd0e5fa);c.drawRoundRect(9,10,54,52,4,4,p);p.setColor(0xff3773ad);c.drawRect(14,15,49,39,p);p.setColor(0xff79b956);p.setStrokeWidth(6);c.drawArc(19,22,53,57,20,280,false,p);p.setColor(0xff286c2c);Path arrow=new Path();arrow.moveTo(17,36);arrow.lineTo(16,51);arrow.lineTo(30,45);arrow.close();c.drawPath(arrow,p);break;
        }return true;}
    private XpProgramArtwork(){}
}
