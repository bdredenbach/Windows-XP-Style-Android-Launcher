package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class XpIconAssetsTest {
    @Test public void allFiveSuppliedIconsRenderThroughSharedIconSurfaces()throws Exception{
        Context context=RuntimeEnvironment.getApplication();BuiltinIconCache.clear();
        for(String kind:new String[]{"computer","network","documents","recycle","recycle-full"}){
            Bitmap source;try(java.io.InputStream stream=context.getAssets().open(XpIconAssets.asset(kind,48))){source=BitmapFactory.decodeStream(stream);}
            AeroIcon icon=new AeroIcon(context,kind);icon.layout(0,0,48,48);Bitmap actual=Bitmap.createBitmap(48,48,Bitmap.Config.ARGB_8888);icon.draw(new Canvas(actual));
            assertEquals(48,source.getWidth());assertEquals(48,source.getHeight());assertTrue("Supplied bitmap reaches "+kind,source.sameAs(actual));source.recycle();actual.recycle();
        }
    }
    @Test public void picksEmbeddedSizesAndLeavesAdditionalNthArtworkAvailable(){
        assertEquals("xp-icons/computer-16.png",XpIconAssets.asset("computer",16));assertEquals("xp-icons/network-24.png",XpIconAssets.asset("network",19));assertEquals("xp-icons/recycle-32.png",XpIconAssets.asset("recycle",24));assertEquals("xp-icons/documents-48.png",XpIconAssets.asset("documents",96));assertNull(XpIconAssets.asset("music",32));
    }
}
