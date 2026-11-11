package com.nthexperience.desktop;

import android.content.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;

/** Bounded recursive search only in storage that the user granted to this launcher. */
final class XpSearchCompanion extends XpToolPanel {
    private final EditText query;private final Spinner kind;private final LinearLayout results;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());private DocumentOps.Cancel cancel;private int generation;
    XpSearchCompanion(Context c,DesktopStore store,Host host){super(c,store,host,"@search");command("Search",this::search);command("Stop",this::cancel);command("Connect folder",()->host.open("@computer"));
        LinearLayout area=new LinearLayout(c);LinearLayout side=column();side.setBackgroundColor(0xffd6e5fb);side.addView(text("Search Companion",16));side.addView(text("What do you want to search for?",12));query=new EditText(c);query.setSingleLine();query.setFilters(new InputFilter[]{new InputFilter.LengthFilter(128)});query.setTextSize(13);query.setHint("All or part of a name");query.setContentDescription("Search file or program name");side.addView(query,new LayoutParams(-1,dp(42)));kind=new Spinner(c);kind.setAdapter(new ArrayAdapter<>(c,android.R.layout.simple_spinner_dropdown_item,new String[]{"All files and folders","Documents","Pictures, music and video","Programs and settings"}));kind.setContentDescription("Search category");side.addView(kind,new LayoutParams(-1,dp(62)));side.addView(text("Use * or ? for file-name patterns. Search covers granted folders, to 5,000 items and 200 results.",11));area.addView(side,new LayoutParams(0,-1,.42f));results=column();ScrollView scroll=new ScrollView(c);scroll.addView(results);area.addView(scroll,new LayoutParams(0,-1,.58f));content.addView(area,new FrameLayout.LayoutParams(-1,-1));say("Enter a name, then Search");}
    private void cancel(){generation++;if(cancel!=null)cancel.cancel();say("Search stopped");}
    static boolean matches(String name,String query){String value=name.toLowerCase(Locale.ROOT),q=query.toLowerCase(Locale.ROOT);if(q.equals("*.*"))q="*";if(!q.contains("*")&&!q.contains("?"))return value.contains(q);StringBuilder regex=new StringBuilder("^");for(int i=0;i<q.length();i++){char c=q.charAt(i);regex.append(c=='*'?".*":c=='?'?".":java.util.regex.Pattern.quote(String.valueOf(c)));}return value.matches(regex.append('$').toString());}
    private void search(){cancel();results.removeAllViews();String needle=query.getText().toString().trim();if(needle.isEmpty()){say("Enter a name or a wildcard such as *.jpg");return;}int category=kind.getSelectedItemPosition(),token=++generation;List<AppCatalog.Entry> apps=host.apps();
        if(category==3){for(StartSearch.Result result:StartSearch.find(store,apps,needle))row(result.title,result.group,()->host.open(result.id));say(results.getChildCount()+" program and setting results");return;}
        cancel=new DocumentOps.Cancel();DocumentOps.Cancel request=cancel;say("Searching granted folders…");
        List<String> roots=new ArrayList<>();JSONArray links=DocumentFolders.links(store);for(int i=0;i<links.length();i++){JSONObject link=links.optJSONObject(i);if(link!=null)roots.add(link.optString("uri"));}if(StorageAccess.has(getContext()))roots.add(StorageAccess.tree().toString());
        worker.execute(()->{List<DocumentOps.Ref> hits=new ArrayList<>();boolean limited=false;int visited=0;long end=SystemClock.elapsedRealtime()+15000;Set<String> seen=new HashSet<>();ArrayDeque<Uri> folders=new ArrayDeque<>();try{for(String root:roots){Uri tree=Uri.parse(root);folders.add(android.provider.DocumentsContract.buildDocumentUriUsingTree(tree,android.provider.DocumentsContract.getTreeDocumentId(tree)));}
            while(!folders.isEmpty()){request.check();if(visited>=5000||hits.size()>=200||SystemClock.elapsedRealtime()>end){limited=true;break;}Uri parent=folders.removeFirst();if(!seen.add(parent.toString()))continue;List<DocumentOps.Ref> children;try{children=DocumentOps.children(getContext(),parent,request);}catch(Exception unavailable){if(request.signal.isCanceled())throw unavailable;continue;}
                for(DocumentOps.Ref ref:children){request.check();visited++;boolean directory=ref.item.directory();String mime=ref.item.mime;boolean allowed=category==0||category==1&&!directory&&(mime.startsWith("text/")||mime.equals("application/pdf")||mime.contains("rtf"))||category==2&&!directory&&(mime.startsWith("image/")||mime.startsWith("audio/")||mime.startsWith("video/"));if(allowed&&matches(ref.item.name,needle))hits.add(ref);if(directory)folders.add(ref.uri);if(visited>=5000||hits.size()>=200){limited=true;break;}}
            }
            boolean partial=limited;int count=visited;main.post(()->{if(closed||token!=generation)return;for(DocumentOps.Ref ref:hits)row(ref.item.name,ref.item.directory()?"Folder":ref.item.mime,()->{if(ref.item.directory())host.openFolder(ref.uri,ref.item.name);else host.openFile(ref.uri,ref.item.mime,ref.item.name);});if(hits.isEmpty())results.addView(text(roots.isEmpty()?"Connect a folder in My Computer to search its files.":"No matching files",13));say(hits.size()+" results • "+count+" items checked"+(partial?" • search limit reached":""));});
        }catch(Exception failure){main.post(()->{if(!closed&&token==generation)say(request.signal.isCanceled()?"Search stopped":"Some folders could not be searched");});}});
    }
    private void row(String title,String detail,Runnable action){TextView item=Aero.button(getContext(),title+"\n"+detail,action);item.setTextSize(12);item.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);item.setMaxLines(3);item.setEllipsize(TextUtils.TruncateAt.END);results.addView(item,new LayoutParams(-1,dp(66)));}
    @Override void close(){if(closed)return;cancel();closed=true;worker.shutdownNow();main.removeCallbacksAndMessages(null);}
}
