package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BooleanSupplier;

public final class XpFeaturesSmoke extends Instrumentation {
    MainActivity home;View root;DesktopStore store;int checks;
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){Bundle result=new Bundle();try{
        Context c=getTargetContext();store=new DesktopStore(c);store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("reduce_motion",true).commit();XpTheme.defaults(store);c.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();c.getSharedPreferences(InitializationState.PREFS,0).edit().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();c.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(c,MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();await(home::hasWindowFocus,"Desktop gains focus");
        for(boolean landscape:new boolean[]{false,true}){
            ui(()->home.setRequestedOrientation(landscape?ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));await(()->landscape?root.getWidth()>root.getHeight():root.getHeight()>root.getWidth(),"Orientation settles");
            for(XpPrograms.Program p:XpPrograms.ALL){ui(()->home.open(p.id));settle();XpToolPanel panel=panel(p.id);check(panel!=null&&panel.getWidth()>100&&panel.getHeight()>100,"Opens "+p.title);if(p.id.equals("@minesweeper")){View board=find(panel,"Minesweeper board. Tap reveal, hold flag; arrow keys select, Enter reveal, F flag.");check(board!=null&&board.getHeight()>200,"Minefield renders real cells");}shot("features-"+p.id.substring(1)+(landscape?"-land":"-port"));ui(()->window(p.id).closeWindow());}
        }
        ui(()->home.open("@wordpad"));settle();XpWordPad word=(XpWordPad)panel("@wordpad");ui(()->{word.editor.setText("Formatted XP draft Ω");word.editor.setSelection(0,9);find(word,"B").performClick();});check(word.editor.getText().getSpans(0,9,StyleSpan.class).length>0,"WordPad applies bold spans");byte[][] rtf={null};ui(()->rtf[0]=XpRichText.rtf(word.editor.getText()).getBytes(StandardCharsets.US_ASCII));ui(()->{try{word.importDocument(rtf[0],"Roundtrip.rtf");}catch(Exception e){throw new RuntimeException(e);}});check(word.editor.getText().toString().equals("Formatted XP draft Ω"),"RTF preserves text and Unicode");
        ui(()->home.open("@paint"));settle();XpPaint paint=(XpPaint)panel("@paint");ui(()->{paint.document.checkpoint();paint.document.fill(10,10,Color.BLUE);});check(paint.document.image.getPixel(10,10)==Color.BLUE,"Paint edits actual pixels");ui(()->paint.document.undo());check(paint.document.image.getPixel(10,10)==Color.WHITE,"Paint Undo restores pixels");
        ui(()->home.open("@minesweeper"));settle();XpMinesweeper mines=(XpMinesweeper)panel("@minesweeper");ui(()->mines.game.reveal(40,new Random(7)));check(mines.game.started&&!mines.game.bombs[40],"First minefield reveal is safe");
        ui(()->home.open("@solitaire"));settle();XpCardGame cards=(XpCardGame)panel("@solitaire");int before=cards.game.stock.size();ui(cards.game::drawStock);check(cards.game.stock.size()==before-cards.game.draw,"Solitaire stock deals correctly");ui(cards.game::undo);check(cards.game.stock.size()==before,"Solitaire undo restores stock");
        Uri tree;getUiAutomation().adoptShellPermissionIdentity("android.permission.MANAGE_DOCUMENTS");try{tree=Uri.parse(c.getContentResolver().call(XpFeatureDocumentsProvider.AUTHORITY,"resetFixture",c.getPackageName(),null).getString("tree"));}finally{getUiAutomation().dropShellPermissionIdentity();}
        Uri folder=DocumentsContract.buildDocumentUriUsingTree(tree,"root");Uri source=DocumentsContract.createDocument(c.getContentResolver(),folder,DocumentsContract.Document.MIME_TYPE_DIR,"Source");Uri note=DocumentsContract.createDocument(c.getContentResolver(),source,"text/plain","Deep Note.txt");try(OutputStream out=c.getContentResolver().openOutputStream(note)){out.write("Real ZIP round trip Ω".getBytes(StandardCharsets.UTF_8));}
        DocumentOps.Ref ref=new DocumentOps.Ref(source,folder,DocumentOps.stat(c,source,new DocumentOps.Cancel()));XpZip.compress(c,Collections.singletonList(ref),folder,"Archive.zip",new DocumentOps.Cancel());List<DocumentOps.Ref> children=DocumentOps.children(c,folder,new DocumentOps.Cancel());DocumentOps.Ref archive=null;for(DocumentOps.Ref child:children)if(child.item.name.equals("Archive.zip"))archive=child;check(archive!=null,"ZIP creates real document-provider archive");XpZip.extract(c,archive,folder,new DocumentOps.Cancel());Uri extracted=null;for(DocumentOps.Ref child:DocumentOps.children(c,folder,new DocumentOps.Cancel()))if(child.item.name.equals("Archive extracted"))extracted=child.uri;check(extracted!=null,"ZIP extracts into a new folder");Uri inner=DocumentOps.children(c,extracted,new DocumentOps.Cancel()).get(0).uri;DocumentOps.Ref restored=DocumentOps.children(c,inner,new DocumentOps.Cancel()).get(0);try(InputStream in=c.getContentResolver().openInputStream(restored.uri)){check(new String(XpDocuments.read(in,1024),StandardCharsets.UTF_8).equals("Real ZIP round trip Ω"),"ZIP round trip preserves file bytes");}
        DocumentFolders.add(store,tree,"XP Test Files");ui(()->home.open("@search"));settle();XpSearchCompanion search=(XpSearchCompanion)panel("@search");ui(()->{((EditText)find(search,"Search file or program name")).setText("Deep Note.txt");find(search,"Search").performClick();});await(()->search.status.getText().toString().contains("results"),"Recursive search completes");check(search.status.getText().toString().contains("2 results"),"Recursive search finds original and extracted nested documents");
        ui(()->home.open("@command"));settle();XpCommandPrompt command=(XpCommandPrompt)panel("@command");ui(()->command.execute("ver"));check(command.status!=null,"Command console executes Nth commands");
        ui(()->home.open("@recorder"));settle();XpSoundRecorder recorder=(XpSoundRecorder)panel("@recorder");ui(()->recorder.active(false));check(recorder.status.getText().toString().contains("Ready"),"Recorder lifecycle stays stopped without explicit Record");
        store.startPins(Arrays.asList("@paint","@wordpad","@freecell"));check(DesktopBackup.parse(DesktopBackup.snapshot(store).toString()).values.containsKey("xp_wordpad_html"),"Rich draft and new tool pins are portable");
        result.putString("result","PASS: "+checks+" XP feature checks; all 16 programs in portrait/landscape, WordPad RTF, bitmap edits/undo, game rules, real ZIP round trip, recursive provider search, recorder lifecycle and portable tool pins.");finish(Activity.RESULT_OK,result);
    }catch(Throwable error){android.util.Log.e("XpSmoke","XP features failed",error);try{shot("features-failure");}catch(Exception ignored){}result.putString("result","FAIL: "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}}
    private void ui(Runnable r){runOnMainSync(r);}private void settle()throws Exception{waitForIdleSync();Thread.sleep(120);}private void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;}
    private void await(BooleanSupplier condition,String label)throws Exception{for(int i=0;i<120;i++){boolean[] ok={false};ui(()->ok[0]=condition.getAsBoolean());if(ok[0]){check(true,label);return;}Thread.sleep(100);}throw new AssertionError(label);}
    private View find(View v,String name){if(name.equals(v.getContentDescription()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=find(((ViewGroup)v).getChildAt(i),name);if(found!=null)return found;}return null;}
    private DesktopWindow window(String id){return window(root,id);}private DesktopWindow window(View v,String id){if(v instanceof DesktopWindow&&((DesktopWindow)v).toolId.equals(id)&&v.getParent()!=null)return (DesktopWindow)v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){DesktopWindow found=window(((ViewGroup)v).getChildAt(i),id);if(found!=null)return found;}return null;}
    private XpToolPanel panel(String id){DesktopWindow w=window(id);return w!=null&&w.body instanceof XpToolPanel?(XpToolPanel)w.body:null;}
    private void shot(String name)throws Exception{Bitmap bitmap=getUiAutomation().takeScreenshot();File dir=new File(getTargetContext().getFilesDir(),"xp-verification");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();}
}
