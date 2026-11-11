package com.nthexperience.desktop;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.util.AtomicFile;
import java.io.*;
import java.util.*;

/** Small bitmap editor with real drawing, shapes, flood fill and bounded undo. */
final class XpPaint extends XpToolPanel {
    static final class Document {
        Bitmap image;final ArrayDeque<Bitmap> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
        Document(){image=Bitmap.createBitmap(800,600,Bitmap.Config.ARGB_8888);image.eraseColor(Color.WHITE);}
        void checkpoint(){undo.addLast(image.copy(Bitmap.Config.ARGB_8888,true));while(undo.size()>6)undo.removeFirst().recycle();clear(redo);}
        void undo(){if(undo.isEmpty())return;redo.addLast(image);image=undo.removeLast();}
        void redo(){if(redo.isEmpty())return;undo.addLast(image);image=redo.removeLast();}
        void replace(Bitmap b){checkpoint();image.recycle();image=b;}
        void fill(int x,int y,int color){
            int w=image.getWidth(),h=image.getHeight();if(x<0||y<0||x>=w||y>=h)return;int[] pixels=new int[w*h];image.getPixels(pixels,0,w,0,0,w,h);int original=pixels[y*w+x];if(original==color)return;
            int[] stack=new int[w*h];int used=0;stack[used++]=y*w+x;pixels[y*w+x]=color;
            while(used>0){int at=stack[--used],cx=at%w,cy=at/w;if(cx>0&&pixels[at-1]==original){pixels[at-1]=color;stack[used++]=at-1;}if(cx<w-1&&pixels[at+1]==original){pixels[at+1]=color;stack[used++]=at+1;}if(cy>0&&pixels[at-w]==original){pixels[at-w]=color;stack[used++]=at-w;}if(cy<h-1&&pixels[at+w]==original){pixels[at+w]=color;stack[used++]=at+w;}}
            image.setPixels(pixels,0,w,0,0,w,h);
        }
        void close(){image.recycle();clear(undo);clear(redo);}
        static void clear(ArrayDeque<Bitmap> values){while(!values.isEmpty())values.remove().recycle();}
    }
    final Document document=new Document();private final CanvasView canvas;private final AtomicFile draft;
    private String tool="Pencil";private int color=Color.BLACK,width=3;private boolean changed;
    XpPaint(Context c,DesktopStore store,Host host){
        super(c,store,host,"@paint");draft=new AtomicFile(new File(c.getFilesDir(),"xp-paint-draft.png"));
        command("File",this::fileMenu);command("Undo",()->{document.undo();changed=true;canvasChanged();});command("Redo",()->{document.redo();changed=true;canvasChanged();});command("Size",()->{String[] sizes={"1","3","6","12","20"};Aero.dialog(c).setTitle("Brush width").setItems(sizes,(d,n)->width=Integer.parseInt(sizes[n])).show();});
        LinearLayout area=new LinearLayout(c);LinearLayout tools=Aero.column(c);tools.setPadding(dp(2),0,dp(2),0);for(String name:new String[]{"Pencil","Brush","Eraser","Fill","Line","Rectangle","Ellipse","Text"}){TextView b=Aero.button(c,name,()->{tool=name;say(name+" selected");});b.setTextSize(10);b.setPadding(0,0,0,0);b.setMinHeight(0);b.setContentDescription("Paint tool: "+name);tools.addView(b,new LayoutParams(dp(62),dp(32)));}ScrollView toolScroll=new ScrollView(c);toolScroll.addView(tools);area.addView(toolScroll,new LayoutParams(dp(66),-1));canvas=new CanvasView(c);area.addView(canvas,new LayoutParams(0,-1,1));content.addView(area,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout palette=new LinearLayout(c);for(int shade:new int[]{Color.BLACK,Color.WHITE,0xff808080,0xffc0c0c0,0xff800000,0xffff0000,0xff808000,0xffffff00,0xff008000,0xff00ff00,0xff008080,0xff00ffff,0xff000080,0xff0000ff,0xff800080,0xffff00ff}){View swatch=new View(c);swatch.setBackgroundColor(shade);swatch.setContentDescription("Paint color "+String.format(Locale.ROOT,"#%06X",shade&0xffffff));swatch.setFocusable(true);swatch.setOnClickListener(v->{color=shade;say("Color "+String.format(Locale.ROOT,"#%06X",shade&0xffffff));});LinearLayout.LayoutParams size=new LayoutParams(0,dp(24),1);size.setMargins(dp(1),dp(2),dp(1),dp(2));palette.addView(swatch,size);}addView(palette,indexOfChild(status),new LayoutParams(-1,dp(28)));
        if(draft.getBaseFile().exists())try(FileInputStream in=draft.openRead()){importDocument(XpDocuments.read(in,XpDocuments.LIMIT),"Draft.png");changed=false;}catch(Exception ignored){}canvasChanged();
    }
    private void fileMenu(){DesktopContextMenu menu=new DesktopContextMenu(getContext(),"Paint File menu");menu.item("New","paint",()->Aero.dialog(getContext()).setTitle("New picture").setMessage("Clear this picture? Save a PNG first if you want a separate copy. Undo can recover this edit.").setPositiveButton("New",(d,w)->{document.checkpoint();document.image.eraseColor(Color.WHITE);changed=true;canvasChanged();}).setNegativeButton("Cancel",null).show());menu.item("Open…","pictures",()->host.pick(id,"image/*"));menu.item("Save As PNG…","drive",()->host.export(id,"Untitled.png","image/png",png()));menu.separator();menu.item("Exit","close",this::requestExit);ContextMenus.centered(toolbar,(x,y)->ContextMenus.show(menu,toolbar,x,y));}
    private byte[] png(){ByteArrayOutputStream out=new ByteArrayOutputStream();document.image.compress(Bitmap.CompressFormat.PNG,100,out);return out.toByteArray();}
    @Override void importDocument(byte[] data,String name)throws Exception{BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,o);if(o.outWidth<1||o.outHeight<1)throw new IOException("Unsupported picture");o.inSampleSize=1;while((long)o.outWidth*o.outHeight/o.inSampleSize/o.inSampleSize>1048576||Math.max(o.outWidth,o.outHeight)/o.inSampleSize>1280)o.inSampleSize*=2;o.inJustDecodeBounds=false;Bitmap loaded=BitmapFactory.decodeByteArray(data,0,data.length,o);if(loaded==null)throw new IOException("Unsupported picture");Bitmap mutable=loaded.copy(Bitmap.Config.ARGB_8888,true);loaded.recycle();document.replace(mutable);changed=true;canvasChanged();say(name+" opened");}
    private void canvasChanged(){if(canvas!=null)canvas.invalidate();say(document.image.getWidth()+" × "+document.image.getHeight()+" • "+tool+" • Save As PNG to export");}
    private void persist(){if(!changed||closed)return;try{FileOutputStream out=draft.startWrite();try{out.write(png());draft.finishWrite(out);changed=false;}catch(Exception e){draft.failWrite(out);}}catch(Exception ignored){}}
    @Override void active(boolean active){if(!active)persist();}
    @Override void close(){if(closed)return;persist();closed=true;document.close();}
    private final class CanvasView extends View {
        final Paint ink=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);float scale=1,left,top,startX,startY,lastX,lastY;boolean drawing;
        CanvasView(Context c){super(c);setContentDescription("Paint canvas");setBackgroundColor(0xff808080);setFocusable(true);}
        @Override protected void onDraw(Canvas c){if(closed)return;scale=Math.min(getWidth()/(float)document.image.getWidth(),getHeight()/(float)document.image.getHeight());if(scale<=0)return;left=(getWidth()-document.image.getWidth()*scale)/2;top=(getHeight()-document.image.getHeight()*scale)/2;int save=c.save();c.translate(left,top);c.scale(scale,scale);c.drawBitmap(document.image,0,0,ink);if(drawing&&(tool.equals("Line")||tool.equals("Rectangle")||tool.equals("Ellipse")))shape(c,startX,startY,lastX,lastY);c.restoreToCount(save);}
        private void ink(){ink.setColor(tool.equals("Eraser")?Color.WHITE:color);ink.setStyle(Paint.Style.STROKE);ink.setStrokeWidth(tool.equals("Pencil")?1:tool.equals("Eraser")?width*4:width);ink.setStrokeCap(Paint.Cap.ROUND);ink.setStrokeJoin(Paint.Join.ROUND);}
        private void shape(Canvas c,float ax,float ay,float bx,float by){ink();if(tool.equals("Line"))c.drawLine(ax,ay,bx,by,ink);else if(tool.equals("Rectangle"))c.drawRect(Math.min(ax,bx),Math.min(ay,by),Math.max(ax,bx),Math.max(ay,by),ink);else c.drawOval(Math.min(ax,bx),Math.min(ay,by),Math.max(ax,bx),Math.max(ay,by),ink);ink.setStyle(Paint.Style.FILL);}
        @Override public boolean onTouchEvent(MotionEvent e){if(closed||scale<=0)return false;float x=Math.max(0,Math.min(document.image.getWidth()-1,(e.getX()-left)/scale)),y=Math.max(0,Math.min(document.image.getHeight()-1,(e.getY()-top)/scale));
            if(e.getActionMasked()==MotionEvent.ACTION_DOWN){if(e.getX()<left||e.getY()<top||e.getX()>=left+document.image.getWidth()*scale||e.getY()>=top+document.image.getHeight()*scale)return false;document.checkpoint();drawing=true;startX=lastX=x;startY=lastY=y;changed=true;
                if(tool.equals("Fill")){document.fill((int)x,(int)y,color);drawing=false;}else if(tool.equals("Text")){drawing=false;EditText text=new EditText(getContext());text.setSingleLine();Aero.dialog(getContext()).setTitle("Paint text").setView(text).setPositiveButton("Place",(d,w)->{ink.setColor(color);ink.setTypeface(Aero.face(getContext()));ink.setTextSize(Math.max(14,width*6));ink.setStyle(Paint.Style.FILL);new Canvas(document.image).drawText(text.getText().toString(),x,y,ink);invalidate();}).setNegativeButton("Cancel",null).show();}else if(!Arrays.asList("Line","Rectangle","Ellipse").contains(tool)){ink();new Canvas(document.image).drawPoint(x,y,ink);ink.setStyle(Paint.Style.FILL);}getParent().requestDisallowInterceptTouchEvent(true);invalidate();return true;}
            if(drawing&&(e.getActionMasked()==MotionEvent.ACTION_MOVE||e.getActionMasked()==MotionEvent.ACTION_UP)){if(Arrays.asList("Line","Rectangle","Ellipse").contains(tool)){lastX=x;lastY=y;if(e.getActionMasked()==MotionEvent.ACTION_UP)shape(new Canvas(document.image),startX,startY,x,y);}else{ink();new Canvas(document.image).drawLine(lastX,lastY,x,y,ink);ink.setStyle(Paint.Style.FILL);lastX=x;lastY=y;}if(e.getActionMasked()==MotionEvent.ACTION_UP){drawing=false;performClick();}invalidate();return true;}if(e.getActionMasked()==MotionEvent.ACTION_CANCEL){drawing=false;invalidate();return true;}return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
