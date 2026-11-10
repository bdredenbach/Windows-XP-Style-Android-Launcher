package com.nthexperience.desktop;

import android.app.Activity;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.nio.charset.*;
import java.io.IOException;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class XpUiTest {
    @Test public void xpAndModernTextEncodingsKeepTheirCharacters()throws Exception{
        assertEquals("Café\nSecond",XpTextFiles.decode("\ufeffCafé\r\nSecond".getBytes(StandardCharsets.UTF_8)));
        assertEquals("Café",XpTextFiles.decode("Café".getBytes(Charset.forName("windows-1252"))));
        assertEquals("日本語",XpTextFiles.decode("日本語".getBytes(StandardCharsets.UTF_16)));
        byte[] body="日本語".getBytes(StandardCharsets.UTF_16LE),bom=new byte[body.length+2];bom[0]=(byte)255;bom[1]=(byte)254;System.arraycopy(body,0,bom,2,body.length);assertEquals("日本語",XpTextFiles.decode(bom));
    }
    @Test public void oversizedImportsAreRejectedInsteadOfTruncated()throws Exception{try{XpTextFiles.decode(new byte[1000001]);fail();}catch(IOException expected){}try{XpTextFiles.decode(new byte[200001]);fail();}catch(IOException expected){}}
    @Test public void notepadTypingUpdatesTheDraftAndCursorStatus(){
        Context c=RuntimeEnvironment.getApplication();DesktopStore store=new DesktopStore(c);store.prefs.edit().clear().commit();
        XpNotepad panel=new XpNotepad(c,store,new XpNotepad.Host(){public void open(){}public void save(String text,boolean saveAs){}public void fresh(){}});
        EditText editor=(EditText)find(panel,"Notepad text");editor.setText("first\nsecond");editor.setSelection(8);
        assertEquals("first\nsecond",store.prefs.getString("note",""));assertEquals("Ln 2, Col 3",((TextView)find(panel,"Notepad line and column")).getText().toString());
        for(String menu:new String[]{"File","Edit","Format","View","Help"})assertNotNull(find(panel,"Notepad "+menu+" menu"));
        panel.document("loaded");assertEquals("loaded",store.prefs.getString("note",""));
    }
    @Test public void captionPaintingKeepsArtworkInsideItsTouchTarget(){
        Context c=RuntimeEnvironment.getApplication();c.getSharedPreferences("desktop",0).edit().putInt(XpTheme.KEY,0).commit();WindowButton close=new WindowButton(c,"×",()->{});close.layout(0,0,32,32);Bitmap normal=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888);close.draw(new Canvas(normal));
        assertEquals(0,Color.alpha(normal.getPixel(0,0)));assertTrue(Color.alpha(normal.getPixel(16,16))>0);int middle=normal.getPixel(16,5);close.setPressed(true);Bitmap pressed=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888);close.draw(new Canvas(pressed));assertNotEquals(middle,pressed.getPixel(16,5));
    }
    @Test public void selectedTabsAndMenuLabelsHaveXpStates(){
        Context c=RuntimeEnvironment.getApplication();android.graphics.drawable.Drawable tab=XpControls.tab(c);tab.setBounds(0,0,90,32);tab.setState(new int[]{android.R.attr.state_enabled,android.R.attr.state_selected});Bitmap image=Bitmap.createBitmap(90,32,Bitmap.Config.ARGB_8888);tab.draw(new Canvas(image));assertTrue(Color.red(image.getPixel(40,2))>200);
        assertEquals(Color.WHITE,XpControls.menuInk(Color.BLACK).getColorForState(new int[]{android.R.attr.state_enabled,android.R.attr.state_hovered},0));
    }
    private View find(View view,String description){if(description.equals(view.getContentDescription()))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View result=find(((ViewGroup)view).getChildAt(i),description);if(result!=null)return result;}return null;}
}
