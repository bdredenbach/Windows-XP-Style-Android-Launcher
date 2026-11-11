package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w393dp-h800dp-mdpi",shadows=XpVisualTest.DeniedStorage.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
public class XpFeaturesTest {
    Context c;DesktopStore store;
    @Before public void setup(){c=RuntimeEnvironment.getApplication();store=new DesktopStore(c);store.prefs.edit().clear().putBoolean("reduce_motion",true).putInt("startup_mode",2).commit();XpTheme.defaults(store);c.getSharedPreferences("xp_games",0).edit().clear().commit();}
    @Test public void registryResolvesRunSearchPinsAndBackup()throws Exception{
        Set<String> ids=new HashSet<>(),aliases=new HashSet<>();for(XpPrograms.Program p:XpPrograms.ALL){assertTrue(ids.add(p.id));for(String alias:p.commands){assertTrue(aliases.add(alias));assertEquals(p.id,XpPrograms.command(alias.toUpperCase(Locale.ROOT)+".exe"));}assertTrue(StartSearch.find(store,Collections.emptyList(),p.title).stream().anyMatch(r->r.id.equals(p.id)));}
        store.desktop(new ArrayList<>(ids));store.startPins(new ArrayList<>(ids));DesktopBackup.Plan plan=DesktopBackup.parse(DesktopBackup.snapshot(store).toString());assertEquals(ids.size(),plan.count("shortcuts"));assertEquals(ids.size(),new JSONArray(plan.string("start_pins","[]")).length());
        for(String group:new String[]{"Accessories","Games","Accessibility","Entertainment","System Tools"})assertTrue(XpPrograms.ALL.stream().anyMatch(p->p.group.equals(group)));
    }
    @Test public void paintFillUndoRedoAndImportAreRealBitmapEdits()throws Exception{
        XpPaint.Document doc=new XpPaint.Document();Canvas canvas=new Canvas(doc.image);Paint line=new Paint();line.setColor(Color.BLACK);canvas.drawRect(20,0,21,600,line);doc.checkpoint();doc.fill(0,0,Color.RED);assertEquals(Color.RED,doc.image.getPixel(5,5));assertEquals(Color.WHITE,doc.image.getPixel(30,5));doc.undo();assertEquals(Color.WHITE,doc.image.getPixel(5,5));doc.redo();assertEquals(Color.RED,doc.image.getPixel(5,5));for(int i=0;i<12;i++)doc.checkpoint();assertTrue(doc.undo.size()<=6);doc.close();
    }
    @Test public void richTextRoundTripsUnicodeFormattingAndSkipsObjects()throws Exception{
        SpannableString text=new SpannableString("Bold café Ω 😀\nNext\tline");text.setSpan(new TypefaceSpan("monospace"),0,4,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),0,4,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);text.setSpan(new StyleSpan(Typeface.BOLD),0,4,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);text.setSpan(new UnderlineSpan(),5,9,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);text.setSpan(new AbsoluteSizeSpan(24,true),10,11,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);String rtf=XpRichText.rtf(text);SpannableStringBuilder parsed=XpRichText.parseRtf(rtf);assertEquals(text.toString(),parsed.toString());assertTrue(parsed.getSpans(0,4,StyleSpan.class).length>0);assertEquals("monospace",parsed.getSpans(0,4,TypefaceSpan.class)[0].getFamily());assertEquals(Layout.Alignment.ALIGN_CENTER,parsed.getSpans(0,4,AlignmentSpan.class)[0].getAlignment());assertTrue(parsed.getSpans(5,9,UnderlineSpan.class).length>0);assertEquals(24,parsed.getSpans(10,11,AbsoluteSizeSpan.class)[0].getSize());
        assertEquals("Before After",XpRichText.parseRtf("{\\rtf1 Before {\\object hidden executable data}After}").toString());String bold="x".repeat(20000);SpannableStringBuilder longText=XpRichText.parseRtf("{\\rtf1\\b "+bold+"}");assertEquals(20000,longText.length());assertEquals(1,longText.getSpans(0,longText.length(),StyleSpan.class).length);
    }
    @Test public void firstMinesweeperClickIsSafeAndBoardsPersist()throws Exception{
        for(int at=0;at<81;at++){XpMinefield game=new XpMinefield(9,9,10);game.reveal(at,new Random(at));assertFalse(game.bombs[at]);assertFalse(game.ended&&!game.won);int count=0;for(boolean value:game.bombs)if(value)count++;assertEquals(10,count);assertEquals(game.state().toString(),XpMinefield.restore(game.state().toString()).state().toString());}
        XpMinefield game=new XpMinefield(9,9,10);game.flag(1);game.reveal(1,new Random(2));assertFalse(game.started);game.flag(1);game.reveal(40,new Random(2));for(int i=0;i<81;i++)if(!game.bombs[i])game.reveal(i,new Random(2));assertTrue(game.won);assertTrue(game.ended);assertEquals(10,game.flagged());
    }
    @Test public void solitaireDealStockRecyclingAndUndoKeepAllCards()throws Exception{
        XpCards cards=new XpCards(false,12);assertEquals(24,cards.stock.size());for(int i=0;i<7;i++){assertEquals(i+1,cards.tableau.get(i).size());assertTrue(cards.face[XpCards.last(cards.tableau.get(i))]);}cards.draw=3;String before=cards.state().toString();cards.drawStock();assertEquals(3,cards.waste.size());assertEquals(21,cards.stock.size());assertTrue(cards.undo());assertEquals(before,cards.state().toString());cards.draw=1;for(int i=0;i<24;i++)cards.drawStock();assertEquals(0,cards.stock.size());cards.drawStock();assertEquals(24,cards.stock.size());assertEquals(0,cards.waste.size());assertEquals(cards.state().toString(),XpCards.restore(cards.state().toString()).state().toString());
    }
    @Test public void freecellAllowsCellsAndEnforcesAlternatingSequences()throws Exception{
        XpCards game=new XpCards(true,8);assertEquals(7,game.tableau.get(0).size());assertEquals(6,game.tableau.get(7).size());int card=XpCards.last(game.tableau.get(0));assertTrue(game.move(0,game.tableau.get(0).size()-1,XpCards.CELL));assertEquals(card,XpCards.last(game.cells.get(0)));assertFalse(game.move(1,0,XpCards.CELL));
        int destination=-1;for(int i=0;i<8;i++)if(game.valid(XpCards.CELL,0,i))destination=i;if(destination>=0)assertTrue(game.move(XpCards.CELL,0,destination));assertEquals(52,count(game));XpCards restored=XpCards.restore(game.state().toString());assertEquals(52,count(restored));assertTrue(restored.freecell);
    }
    @Test public void illegalCardMoveLeavesDealUnchangedAndAceCanEnterFoundation()throws Exception{
        XpCards game=new XpCards(false,0);String before=game.state().toString();assertFalse(game.move(0,0,0));assertEquals(before,game.state().toString());
        for(long seed=0;seed<200;seed++){game=new XpCards(false,seed);for(int i=0;i<7;i++){int at=game.tableau.get(i).size()-1,card=game.tableau.get(i).get(at);if(XpCards.rank(card)==1){assertTrue(game.autoFoundation(i,at));assertEquals(1,game.foundations.get(XpCards.suit(card)).size());assertTrue(game.undo());assertEquals(52,count(game));return;}}}fail("No dealt Ace found");
    }
    @Test public void zipInspectionRejectsTraversalDuplicatesAndConflictingPaths()throws Exception{
        byte[] valid=zip(new String[]{"folder/","folder/note.txt"},new String[]{"","hello"});List<XpZip.Entry> entries=XpZip.inspect(()->new ByteArrayInputStream(valid),new DocumentOps.Cancel());assertEquals(2,entries.size());assertEquals("folder/note.txt",entries.get(1).path);
        for(String unsafe:new String[]{"../escape.txt","/absolute","C:/drive","folder/../../x","folder//name","folder/ name","a/./b"})try{XpZip.path(unsafe);fail(unsafe);}catch(IOException expected){}
        byte[] conflict=zip(new String[]{"a","a/b.txt"},new String[]{"one","two"});try{XpZip.inspect(()->new ByteArrayInputStream(conflict),new DocumentOps.Cancel());fail();}catch(IOException expected){}
        byte[] caseDuplicate=zip(new String[]{"A.txt","a.txt"},new String[]{"one","two"});try{XpZip.inspect(()->new ByteArrayInputStream(caseDuplicate),new DocumentOps.Cancel());fail();}catch(IOException expected){}
    }
    @Test public void wildcardMatchingAndRestorePointsPreserveCurrentDrafts()throws Exception{
        assertTrue(XpSearchCompanion.matches("Holiday.JPG","*.jpg"));assertTrue(XpSearchCompanion.matches("note1.txt","note?.txt"));assertFalse(XpSearchCompanion.matches("notes.txt","note?.pdf"));store.prefs.edit().putString("note","old").putString("xp_wordpad_html","<p>old</p>").commit();JSONObject point=DesktopBackup.snapshot(store);store.prefs.edit().putString("note","current").putString("xp_wordpad_html","<p>current</p>").commit();DesktopBackup.Plan plan=DesktopBackup.parse(XpRestorePoints.preservingDrafts(point,store));assertEquals("current",plan.values.get("note"));assertEquals("<p>current</p>",plan.values.get("xp_wordpad_html"));
    }
    @Test public void restoredDraftIsNotOverwrittenAndPreparedExportFollowsRecreatedActivity()throws Exception{
        XpWordPad pad=new XpWordPad(c,store,null);pad.editor.setText("Outgoing document");String restored="{\\rtf1 Restored document}";store.prefs.edit().putString("xp_wordpad_rtf",restored).commit();pad.active(false);assertEquals(restored,store.prefs.getString("xp_wordpad_rtf",""));
        c.getSharedPreferences("xp_document_picker",0).edit().clear().commit();XpDocuments.Host host=new XpDocuments.Host(){public void opened(String id,byte[] data,String name){}public void message(String id,String text){}};
        try(org.robolectric.android.controller.ActivityController<Activity> first=Robolectric.buildActivity(Activity.class).setup();org.robolectric.android.controller.ActivityController<Activity> second=Robolectric.buildActivity(Activity.class).setup()){
            XpDocuments old=new XpDocuments(first.get(),host);old.export("@wordpad","Kept.rtf","application/rtf",restored.getBytes(StandardCharsets.US_ASCII));old.close();XpDocuments next=new XpDocuments(second.get(),host);
            for(int i=0;i<100;i++){Shadows.shadowOf(Looper.getMainLooper()).idle();if("picker".equals(c.getSharedPreferences("xp_document_picker",0).getString("phase","")))break;Thread.sleep(20);}
            org.robolectric.shadows.ShadowActivity.IntentForResult launched=Shadows.shadowOf(second.get()).getNextStartedActivityForResult();assertNotNull(launched);assertEquals(Intent.ACTION_CREATE_DOCUMENT,launched.intent.getAction());assertEquals("Kept.rtf",launched.intent.getStringExtra(Intent.EXTRA_TITLE));assertNull(Shadows.shadowOf(first.get()).getNextStartedActivityForResult());assertTrue(next.result(XpDocuments.REQUEST,Activity.RESULT_CANCELED,null));next.close();
        }
    }
    @Test public void allNewProgramsRenderFromTheActualLauncherInBothOrientations()throws Exception{
        c.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();c.getSharedPreferences(InitializationState.PREFS,0).edit().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();c.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();File output=new File("build/xp-features-rendered");output.mkdirs();
        for(boolean landscape:new boolean[]{false,true}){
            RuntimeEnvironment.setQualifiers(landscape?"w800dp-h393dp-land-mdpi":"w393dp-h800dp-port-mdpi");int w=landscape?800:393,h=landscape?393:800;
            try(org.robolectric.android.controller.ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).create().start().resume().visible()){
                MainActivity activity=controller.get();View root=activity.getWindow().getDecorView();XpShellUpgradeTest.layout(root,w,h);
                for(XpPrograms.Program p:XpPrograms.ALL){activity.open(p.id);XpShellUpgradeTest.layout(root,w,h);XpToolPanel panel=(XpToolPanel)XpExperiencePolishTest.find(root,"XP program: "+p.title);assertNotNull(p.id,panel);assertTrue(p.id,panel.getWidth()>100&&panel.getHeight()>100);if(panel instanceof XpMinesweeper){View board=XpExperiencePolishTest.find(root,"Minesweeper board. Tap reveal, hold flag; arrow keys select, Enter reveal, F flag.");assertTrue("Minefield "+board.getWidth()+" x "+board.getHeight(),board.getWidth()>200&&board.getHeight()>200);}Bitmap image=XpExperiencePolishTest.draw(root);XpExperiencePolishTest.save(image,new File(output,p.id.substring(1)+(landscape?"-land":"-port")+".png"));image.recycle();View window=(View)panel.getParent();while(!(window instanceof DesktopWindow))window=(View)window.getParent();((DesktopWindow)window).closeWindow();}
            }
        }
    }
    static int count(XpCards game){int n=game.stock.size()+game.waste.size();for(List<Integer> p:game.tableau)n+=p.size();for(List<Integer> p:game.foundations)n+=p.size();for(List<Integer> p:game.cells)n+=p.size();return n;}
    static byte[] zip(String[] names,String[] texts)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ZipOutputStream zip=new ZipOutputStream(bytes)){for(int i=0;i<names.length;i++){zip.putNextEntry(new ZipEntry(names[i]));zip.write(texts[i].getBytes(StandardCharsets.UTF_8));zip.closeEntry();}}return bytes.toByteArray();}
}
