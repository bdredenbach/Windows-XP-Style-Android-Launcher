package com.nthexperience.desktop;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.view.*;
import android.widget.*;

/** XP Notepad's menu, plain editor and line/column status; the draft stays local. */
final class XpNotepad extends LinearLayout {
    interface Host{void open();void save(String text,boolean saveAs);void fresh();default void closed(){}}
    private final DesktopStore store;private final Host host;private final Editor editor;private final TextView status;
    private String undo="";private boolean replacing,busy;
    XpNotepad(Context c,DesktopStore store,Host host){
        super(c);this.store=store;this.host=host;setOrientation(VERTICAL);setBackgroundColor(XpTheme.surface(c));setContentDescription("XP Notepad");
        LinearLayout menus=new LinearLayout(c);for(String name:new String[]{"File","Edit","Format","View","Help"}){TextView item=Aero.text(c,name,11,Color.BLACK);item.setPadding(dp(8),0,dp(8),0);item.setFocusable(true);item.setBackground(XpControls.menuSelection(c));item.setTextColor(XpControls.menuInk(Color.BLACK));item.setContentDescription("Notepad "+name+" menu");item.setOnClickListener(v->menu(item,name));menus.addView(item,new LayoutParams(-2,dp(27)));}addView(menus);
        editor=new Editor(c);editor.setContentDescription("Notepad text");editor.setTypeface(Typeface.MONOSPACE);editor.setTextSize(store.prefs.getInt("xp_notepad_size",13));editor.setGravity(Gravity.TOP|Gravity.START);editor.setBackgroundColor(Color.WHITE);editor.setTextColor(Color.BLACK);editor.setPadding(dp(4),dp(3),dp(4),dp(3));editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);editor.setHorizontallyScrolling(!store.prefs.getBoolean("xp_notepad_wrap",false));editor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200000)});editor.setText(store.prefs.getString("note",""));addView(editor,new LayoutParams(-1,0,1));
        status=Aero.text(c,"",11,Color.BLACK);status.setContentDescription("Notepad line and column");status.setPadding(dp(7),0,dp(5),0);status.setBackground(Aero.panel(c,0xffece9d8,0xffece9d8,0,0xffc7c5b2));status.setVisibility(store.prefs.getBoolean("xp_notepad_status",true)?VISIBLE:GONE);addView(status,new LayoutParams(-1,dp(23)));updateStatus();
        editor.addTextChangedListener(new TextWatcher(){String before;public void beforeTextChanged(CharSequence text,int start,int count,int after){before=text.toString();}public void onTextChanged(CharSequence text,int start,int before,int count){store.prefs.edit().putString("note",text.toString()).apply();updateStatus();}public void afterTextChanged(Editable text){if(!replacing)undo=before;}});
    }
    void document(String text){replacing=true;editor.setText(text);editor.setSelection(0);undo="";replacing=false;}
    void busy(boolean value){busy=value;editor.setEnabled(!value);}
    void close(){host.closed();}
    String text(){return editor.getText().toString();}
    private void updateStatus(){if(status==null||editor==null)return;int at=Math.max(0,editor.getSelectionStart()),line=1,last=-1;String text=text();for(int i=0;i<Math.min(at,text.length());i++)if(text.charAt(i)=='\n'){line++;last=i;}status.setText("Ln "+line+", Col "+(at-last));}
    private void menu(View anchor,String name){if(busy){Toast.makeText(getContext(),"Opening document…",Toast.LENGTH_SHORT).show();return;}DesktopContextMenu menu=new DesktopContextMenu(getContext(),"Notepad "+name+" menu");
        switch(name){
            case "File":menu.item("New","empty",()->{if(text().isEmpty()){host.fresh();return;}Aero.dialog(getContext()).setTitle("Notepad").setMessage("Start a new document? Save your current text to a file first if you want to keep it.").setPositiveButton("New",(d,w)->host.fresh()).setNegativeButton("Cancel",null).show();});menu.item("Open…","files",host::open);menu.item("Save","empty",()->host.save(text(),false));menu.item("Save As…","empty",()->host.save(text(),true));menu.separator();menu.item("Exit","empty",this::exit);break;
            case "Edit":menu.item("Undo","empty",!undo.equals(text()),()->{String current=text();replacing=true;editor.setText(undo);editor.setSelection(editor.length());undo=current;replacing=false;});menu.separator();menu.item("Cut","cut",()->editor.onTextContextMenuItem(android.R.id.cut));menu.item("Copy","copy",()->editor.onTextContextMenuItem(android.R.id.copy));menu.item("Paste","paste",()->editor.onTextContextMenuItem(android.R.id.paste));menu.item("Delete","delete",()->{int a=editor.getSelectionStart(),b=editor.getSelectionEnd();if(a!=b)editor.getText().delete(Math.min(a,b),Math.max(a,b));});menu.separator();menu.item("Find…","search",this::find);menu.item("Select All","empty",editor::selectAll);menu.item("Time/Date","clock",()->editor.getText().insert(Math.max(0,editor.getSelectionStart()),android.text.format.DateFormat.getTimeFormat(getContext()).format(new java.util.Date())+" "+android.text.format.DateFormat.getDateFormat(getContext()).format(new java.util.Date())));break;
            case "Format":menu.checked("Word Wrap",store.prefs.getBoolean("xp_notepad_wrap",false),()->{boolean wrap=!store.prefs.getBoolean("xp_notepad_wrap",false);store.prefs.edit().putBoolean("xp_notepad_wrap",wrap).apply();editor.setHorizontallyScrolling(!wrap);editor.requestLayout();});menu.item("Font…","empty",()->{String[] sizes={"10","12","13","14","16","18"};Aero.dialog(getContext()).setTitle("Notepad Font Size").setItems(sizes,(d,w)->{int size=Integer.parseInt(sizes[w]);store.prefs.edit().putInt("xp_notepad_size",size).apply();editor.setTextSize(size);}).show();});break;
            case "View":menu.checked("Status Bar",status.getVisibility()==VISIBLE,()->{boolean show=status.getVisibility()!=VISIBLE;status.setVisibility(show?VISIBLE:GONE);store.prefs.edit().putBoolean("xp_notepad_status",show).apply();});break;
            default:menu.item("About Notepad","notes",()->Aero.dialog(getContext()).setTitle("Notepad").setMessage("Nth Desktop XP Notepad\n\nPlain text editing with Open, Save and Save As. Your unfinished text is retained on this device between visits.").setPositiveButton("OK",null).show());
        }ContextMenus.centered(anchor,(x,y)->ContextMenus.show(menu,anchor,x,y));
    }
    private void find(){EditText query=new EditText(getContext());query.setSingleLine(true);query.setHint("Find what:");Aero.dialog(getContext()).setTitle("Find").setView(query).setPositiveButton("Find Next",(d,w)->{String needle=query.getText().toString();if(needle.isEmpty())return;int at=text().indexOf(needle,Math.max(0,editor.getSelectionEnd()));if(at<0)at=text().indexOf(needle);if(at>=0){editor.requestFocus();editor.setSelection(at,at+needle.length());}else Toast.makeText(getContext(),"Cannot find \""+needle+"\"",Toast.LENGTH_SHORT).show();}).setNegativeButton("Cancel",null).show();}
    private void exit(){View view=this;while(view.getParent() instanceof View){view=(View)view.getParent();if(view instanceof DesktopWindow){((DesktopWindow)view).closeWindow();return;}}}
    private int dp(int value){return Aero.dp(getContext(),value);}
    private final class Editor extends EditText {Editor(Context c){super(c);}@Override protected void onSelectionChanged(int start,int end){super.onSelectionChanged(start,end);updateStatus();}}
}
