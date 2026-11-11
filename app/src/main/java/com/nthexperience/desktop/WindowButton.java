package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.widget.TextView;

/** Consistent caption controls, independent of the device's symbol font. */
final class WindowButton extends TextView {
    private final Paint ink=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean captionActive=true;
    void active(boolean value){captionActive=value;invalidate();}
    WindowButton(Context context,String symbol,Runnable action){
        super(context);setText(symbol);setGravity(Gravity.CENTER);setFocusable(true);
        setMinHeight(0);setPadding(0,0,0,0);setOnClickListener(v->action.run());
        setBackgroundColor(Color.TRANSPARENT);
    }
    private static final java.util.Map<String,android.graphics.Bitmap> captions=new java.util.HashMap<>();
    private android.graphics.Bitmap caption(String key){android.graphics.Bitmap image=captions.get(key);if(image==null)try(java.io.InputStream stream=getContext().getAssets().open("xp-controls/"+key+".png")){image=android.graphics.BitmapFactory.decodeStream(stream);if(image!=null)captions.put(key,image);}catch(java.io.IOException ignored){}return image;}
    @Override protected void onDraw(Canvas canvas){
        float d=getResources().getDisplayMetrics().density,cx=getWidth()/2f,cy=getHeight()/2f;
        String symbol=getText().toString();boolean close="×".equals(symbol),help="?".equals(symbol);
        int scheme=XpTheme.scheme(getContext());float side=Math.min(25*d,Math.min(getWidth(),getHeight())-4*d);
        if(!help&&scheme!=5&&(scheme==0||scheme==3||close)){
            String kind=close?"close":"—".equals(symbol)?"minimize":"❐".equals(symbol)||"▣".equals(symbol)?"restore":"maximize";
            String key=kind+(isPressed()?"-active":isHovered()||isFocused()?"-hover":"");android.graphics.Bitmap image=caption(key);
            if(image!=null){ink.setStyle(Paint.Style.FILL);ink.setShader(null);ink.setColorFilter(null);if(!captionActive||!isEnabled()){android.graphics.ColorMatrix muted=new android.graphics.ColorMatrix();muted.setSaturation(isEnabled()?.35f:0f);ink.setColorFilter(new android.graphics.ColorMatrixColorFilter(muted));}ink.setAlpha(isEnabled()?(captionActive?255:170):95);ink.setFilterBitmap(true);canvas.drawBitmap(image,null,new android.graphics.RectF(cx-side/2,cy-side/2,cx+side/2,cy+side/2),ink);ink.setColorFilter(null);ink.setAlpha(255);return;}
        }
        int[] palette=XpTheme.palette(getContext());ink.setStyle(Paint.Style.FILL);ink.setAlpha(isEnabled()?(captionActive?255:155):95);
        ink.setShader(new android.graphics.LinearGradient(0,cy-side/2,0,cy+side/2,scheme==5?new int[]{0xffefede5,0xffd4d0c8}:isPressed()?new int[]{palette[2],palette[1]}:new int[]{palette[0],palette[1],palette[2]},null,android.graphics.Shader.TileMode.CLAMP));
        canvas.drawRoundRect(cx-side/2,cy-side/2,cx+side/2,cy+side/2,scheme==5?0:3*d,scheme==5?0:3*d,ink);ink.setShader(null);
        ink.setStyle(Paint.Style.STROKE);ink.setStrokeWidth(d);ink.setColor(isFocused()||isHovered()?0xffffd76b:scheme==5?0xff808080:0xfff0f4ff);canvas.drawRoundRect(cx-side/2,cy-side/2,cx+side/2,cy+side/2,scheme==5?0:3*d,scheme==5?0:3*d,ink);
        ink.setColor(XpTheme.scheme(getContext())==5||(!close&&XpTheme.scheme(getContext())==2)?0xff172343:Color.WHITE);ink.setAlpha(isEnabled()?255:100);
        ink.setStyle(Paint.Style.STROKE);ink.setStrokeWidth(Math.max(d,1));
        if(help){ink.setStyle(Paint.Style.FILL);ink.setTypeface(Aero.strong(getContext()));ink.setTextAlign(Paint.Align.CENTER);ink.setTextSize(18*d);canvas.drawText("?",cx,cy+6*d,ink);}
        else if(close){ink.setStrokeWidth(1.6f*d);canvas.drawLine(cx-4*d,cy-4*d,cx+4*d,cy+4*d,ink);canvas.drawLine(cx+4*d,cy-4*d,cx-4*d,cy+4*d,ink);}
        else if("—".equals(symbol)){ink.setStrokeWidth(2*d);canvas.drawLine(cx-5*d,cy+4*d,cx+5*d,cy+4*d,ink);}
        else if("❐".equals(symbol)||"▣".equals(symbol)){
            canvas.drawRect(cx-2*d,cy-5*d,cx+5*d,cy+2*d,ink);
            ink.setStyle(Paint.Style.FILL);ink.setColor(0xffc0d9e7);canvas.drawRect(cx-5*d,cy-2*d,cx+2*d,cy+5*d,ink);
            ink.setStyle(Paint.Style.STROKE);ink.setColor(XpTheme.scheme(getContext())==2||XpTheme.scheme(getContext())==5?0xff172343:Color.WHITE);canvas.drawRect(cx-5*d,cy-2*d,cx+2*d,cy+5*d,ink);canvas.drawLine(cx-5*d,cy,cx+2*d,cy,ink);
        }else{canvas.drawRect(cx-5*d,cy-4*d,cx+5*d,cy+4*d,ink);canvas.drawLine(cx-5*d,cy-2*d,cx+5*d,cy-2*d,ink);}
    }
}
