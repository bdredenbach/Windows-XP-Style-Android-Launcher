package com.nthexperience.desktop;

import android.content.*;
import android.graphics.*;
import android.text.Html;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.json.*;

final class XpCommandPrompt extends XpToolPanel {
    private TextView output;private EditText input;private ScrollView terminal;private String location="Tools",last="";
    XpCommandPrompt(Context c,DesktopStore store,Host host){super(c,store,host,"@command");command("Run",this::run);command("History",()->input.setText(last));command("Copy",()->copy("Command output",output.getText().toString()));command("Clear",()->output.setText(""));
        LinearLayout area=Aero.column(c);area.setBackgroundColor(Color.BLACK);output=Aero.text(c,"Nth Desktop XP command console\nType HELP for commands. This console controls Nth tools and drafts.\n",12,Color.WHITE);output.setTypeface(Typeface.MONOSPACE);output.setTextIsSelectable(true);output.setPadding(dp(6),dp(6),dp(6),dp(6));terminal=new ScrollView(c);terminal.addView(output);area.addView(terminal,new LayoutParams(-1,0,1));input=new EditText(c);input.setSingleLine();input.setTypeface(Typeface.MONOSPACE);input.setTextColor(Color.WHITE);input.setHintTextColor(0xffbbbbbb);input.setHint("C:\\NthDesktop\\Tools>");input.setContentDescription("Command line");input.setBackgroundColor(0xff151515);input.setTextSize(13);input.setOnEditorActionListener((v,a,e)->{run();return true;});area.addView(input,new LayoutParams(-1,dp(42)));content.addView(area,new FrameLayout.LayoutParams(-1,-1));say("Nth command environment • type HELP");}
    void execute(String line){input.setText(line);run();}
    private void append(String text){String value=output.getText()+text+"\n";if(value.length()>40000)value=value.substring(value.length()-40000);output.setText(value);terminal.post(()->terminal.fullScroll(View.FOCUS_DOWN));}
    private void run(){String line=input.getText().toString().trim();if(line.isEmpty())return;last=line;input.setText("");append("C:\\NthDesktop\\"+location+">"+line);String[] parts=line.split("\\s+",2);String cmd=parts[0].toLowerCase(Locale.ROOT),arg=parts.length>1?parts[1].trim():"";if(arg.length()>1&&arg.startsWith("\"")&&arg.endsWith("\""))arg=arg.substring(1,arg.length()-1);
        switch(cmd){
            case "help":append("HELP CLS DIR CD TYPE ECHO VER DATE TIME SYSTEMINFO TASKLIST START COLOR EXIT\n\nDIR lists Tools, Apps or Documents. CD changes those Nth folders.\nTYPE Notepad.txt or WordPad.txt reads the current local draft.\nSTART launches a tool command (mspaint, write, winmine…) or installed app.\nWindows .exe files and Android shell commands are not executed.");break;
            case "cls":output.setText("");break;case "echo":append(arg);break;case "ver":append("Nth Desktop XP "+AppInfo.version(getContext())+" on Android "+android.os.Build.VERSION.RELEASE);break;
            case "date":append(java.text.DateFormat.getDateInstance().format(new Date()));break;case "time":append(java.text.DateFormat.getTimeInstance().format(new Date()));break;
            case "cd":String next=arg.replace("/","\\");if(next.equals("..")||next.equals("\\"))location="";else{int slash=next.lastIndexOf('\\');if(slash>=0)next=next.substring(slash+1);String found=null;for(String folder:new String[]{"Tools","Apps","Documents"})if(folder.equalsIgnoreCase(next))found=folder;if(found==null){append("Path not found. Choose Tools, Apps or Documents.");break;}location=found;}input.setHint("C:\\NthDesktop\\"+location+">");break;
            case "dir":if(location.isEmpty())append("<DIR> Tools\n<DIR> Apps\n<DIR> Documents");else if(location.equals("Tools")){StringBuilder s=new StringBuilder();for(XpPrograms.Program p:XpPrograms.ALL)s.append(p.commands[0]).append(".exe  ").append(p.title).append('\n');append(s.toString());}else if(location.equals("Apps")){StringBuilder s=new StringBuilder();for(AppCatalog.Entry e:host.apps())s.append(e.label).append('\n');append(s.toString());}else{StringBuilder s=new StringBuilder("Notepad.txt\nWordPad.txt\n");JSONArray files=LinkedFiles.list(store);for(int i=0;i<files.length();i++)s.append(files.optJSONObject(i).optString("name")).append(" (linked file)\n");append(s.toString());}break;
            case "type":if(arg.equalsIgnoreCase("Notepad.txt"))append(store.prefs.getString("note",""));else if(arg.equalsIgnoreCase("WordPad.txt"))append(Html.fromHtml(store.prefs.getString("xp_wordpad_html",""),Html.FROM_HTML_MODE_LEGACY).toString());else append("TYPE reads Notepad.txt and WordPad.txt. Open linked documents in Explorer.");break;
            case "systeminfo":append(XpSystemInformation.report(getContext()));break;case "tasklist":append(String.join("\n",host.tasks()));break;
            case "start":String id=XpPrograms.command(arg);if(id==null){for(AppCatalog.Entry e:host.apps())if(e.label.equalsIgnoreCase(arg))id=e.id;}if(id==null){Map<String,String> names=new HashMap<>();names.put("notepad","@notes");names.put("explorer","@files");names.put("control","@control");names.put("calc","@gadget:calculator");names.put("taskmgr","@taskmanager");String value=arg.toLowerCase(Locale.ROOT).replaceFirst("\\.exe$","");id=names.get(value);}if(id==null)append("Program not found.");else{host.open(id);append("Opened "+arg);}break;
            case "color":if(arg.matches("[0-9a-fA-F]{2}")){int[] colors={Color.BLACK,0xff000080,0xff008000,0xff008080,0xff800000,0xff800080,0xff808000,0xffc0c0c0,0xff808080,0xff0000ff,0xff00ff00,0xff00ffff,0xffff0000,0xffff00ff,0xffffff00,Color.WHITE};output.setBackgroundColor(colors[Character.digit(arg.charAt(0),16)]);output.setTextColor(colors[Character.digit(arg.charAt(1),16)]);}else append("Use COLOR bg/fg hex pair, for example COLOR 0A.");break;
            case "exit":requestExit();break;default:append("Unrecognized Nth command. Type HELP.");
        }
    }
}
