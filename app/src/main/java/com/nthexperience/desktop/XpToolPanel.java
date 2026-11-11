package com.nthexperience.desktop;

import android.content.*;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Shared XP utility frame. The Activity owns permission and document-picker flows. */
abstract class XpToolPanel extends LinearLayout {
    interface Host {
        void open(String id);void openFile(android.net.Uri uri,String mime,String name);void openFolder(android.net.Uri uri,String name);void pick(String id,String mime);void export(String id,String name,String mime,byte[] bytes);
        boolean microphone();void requestMicrophone();Bitmap desktopPicture(View exclude);String windowText(View exclude);
        boolean type(String value);List<AppCatalog.Entry> apps();List<String> tasks();void restorePreview(String json);void shellChanged();
    }
    final DesktopStore store;final Host host;final String id;
    final LinearLayout toolbar;final FrameLayout content;final TextView status;
    boolean closed;
    XpToolPanel(Context c,DesktopStore store,Host host,String id){
        super(c);this.store=store;this.host=host;this.id=id;setOrientation(VERTICAL);setBackgroundColor(XpTheme.surface(c));setContentDescription("XP program: "+XpPrograms.get(id).title);
        toolbar=new LinearLayout(c);toolbar.setGravity(Gravity.CENTER_VERTICAL);HorizontalScrollView tools=new HorizontalScrollView(c);tools.setHorizontalScrollBarEnabled(false);tools.addView(toolbar);tools.setBackgroundColor(XpTheme.surface(c));addView(tools,new LayoutParams(-1,dp(36)));
        content=new FrameLayout(c);addView(content,new LayoutParams(-1,0,1));status=Aero.text(c,"",11,Aero.INK);status.setPadding(dp(6),0,dp(6),0);status.setSingleLine();status.setEllipsize(android.text.TextUtils.TruncateAt.END);status.setContentDescription("Program status");status.setBackground(Aero.panel(c,0xffece9d8,0xffece9d8,0,0xffaca899));addView(status,new LayoutParams(-1,dp(24)));
    }
    final int dp(float n){return Aero.dp(getContext(),n);}
    final TextView command(String label,Runnable action){TextView b=Aero.button(getContext(),label,action);b.setTextSize(11);b.setContentDescription(label);b.setMinHeight(0);b.setPadding(dp(8),0,dp(8),0);LinearLayout.LayoutParams p=new LayoutParams(-2,dp(30));p.setMargins(dp(2),dp(2),dp(2),dp(2));toolbar.addView(b,p);return b;}
    final LinearLayout column(){LinearLayout col=Aero.column(getContext());col.setPadding(dp(10),dp(8),dp(10),dp(8));return col;}
    final void scroll(View child){ScrollView scroll=new ScrollView(getContext());scroll.setFillViewport(true);scroll.addView(child);content.removeAllViews();content.addView(scroll,new FrameLayout.LayoutParams(-1,-1));}
    final TextView text(String value,int size){TextView t=Aero.text(getContext(),value,size,Aero.INK);t.setPadding(0,dp(3),0,dp(3));return t;}
    final void say(String message){status.setText(message);}
    final void copy(String label,String text){android.content.ClipboardManager clip=getContext().getSystemService(android.content.ClipboardManager.class);if(clip!=null)clip.setPrimaryClip(ClipData.newPlainText(label,text));say("Copied to clipboard");}
    void importDocument(byte[] bytes,String name)throws Exception{throw new java.io.IOException("This program does not open documents");}
    void active(boolean value){}
    void close(){closed=true;}
    void requestExit(){View p=this;while(p.getParent() instanceof View){p=(View)p.getParent();if(p instanceof DesktopWindow){((DesktopWindow)p).closeWindow();return;}}}
}
