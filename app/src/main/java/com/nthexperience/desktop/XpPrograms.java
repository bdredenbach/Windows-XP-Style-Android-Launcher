package com.nthexperience.desktop;

import java.util.*;

/** One registry for Start, Run, search, shortcut backup and window restoration. */
final class XpPrograms {
    static final class Program {
        final String id,title,group,icon,description;final String[] commands;
        Program(String id,String title,String group,String icon,String description,String aliases){this.id=id;this.title=title;this.group=group;this.icon=icon;this.description=description;commands=aliases.split(",");}
    }
    static final List<Program> ALL=Collections.unmodifiableList(Arrays.asList(
        p("paint","Paint","Accessories","paint","Draw, edit pictures, undo and save PNG files","mspaint,pbrush"),
        p("wordpad","WordPad","Accessories","wordpad","Rich text editing with RTF, HTML and plain text files","write,wordpad"),
        p("charmap","Character Map","System Tools","charmap","Choose Unicode characters and copy them","charmap"),
        p("command","Command Prompt","Accessories","command","Commands for Nth programs, drafts and device information","cmd,command"),
        p("recorder","Sound Recorder","Entertainment","recorder","Record microphone audio, preview and save M4A","sndrec32,soundrecorder"),
        p("magnifier","Magnifier","Accessibility","magnifier","Magnify your Nth desktop with adjustable zoom","magnify,magnifier"),
        p("narrator","Narrator","Accessibility","narrator","Read typed text or a Nth window aloud","narrator"),
        p("osk","On-Screen Keyboard","Accessibility","keyboard","Type into Nth text fields with a floating keyboard","osk"),
        p("msinfo","System Information","System Tools","computer","Real Android hardware, memory, display and network reports","msinfo32,systeminfo"),
        p("cleanup","Disk Cleanup","System Tools","drive","Review and remove Nth temporary cache files","cleanmgr"),
        p("restorepoints","System Restore","System Tools","restore","Create and preview Nth desktop configuration restore points","rstrui"),
        p("transfer","Files and Settings Transfer Wizard","System Tools","transfer","Move Nth desktop settings with portable backup files","migwiz"),
        p("search","Search Companion","Accessories","search","Search granted folders recursively and find programs","search"),
        p("minesweeper","Minesweeper","Games","minesweeper","First-click-safe mines, flags, chord and three difficulties","winmine"),
        p("solitaire","Solitaire","Games","solitaire","Klondike with draw one or three, hints and undo","sol"),
        p("freecell","FreeCell","Games","solitaire","Eight cascades, four free cells and legal multi-card moves","freecell")
    ));
    private static Program p(String id,String title,String group,String icon,String description,String aliases){return new Program("@"+id,title,group,icon,description,aliases);}
    static Program get(String id){for(Program p:ALL)if(p.id.equals(id))return p;return null;}
    static boolean valid(String id){return get(id)!=null;}
    static String command(String input){String text=input.trim().toLowerCase(Locale.ROOT);if(text.endsWith(".exe"))text=text.substring(0,text.length()-4);for(Program p:ALL)for(String alias:p.commands)if(text.equals(alias))return p.id;return null;}
    static void search(DesktopStore store,String query,List<StartSearch.Result> result){String q=query.toLowerCase(Locale.ROOT);for(Program p:ALL)if((p.title+" "+p.description+" "+String.join(" ",p.commands)).toLowerCase(Locale.ROOT).contains(q))result.add(new StartSearch.Result(p.id,store.name(p.id,p.title),p.group,p.icon,null,true));}
    static XpToolPanel create(android.content.Context c,DesktopStore store,String id,XpToolPanel.Host host){
        switch(id){case "@paint":return new XpPaint(c,store,host);case "@wordpad":return new XpWordPad(c,store,host);case "@charmap":return new XpCharacterMap(c,store,host);case "@command":return new XpCommandPrompt(c,store,host);case "@recorder":return new XpSoundRecorder(c,store,host);case "@magnifier":return new XpMagnifier(c,store,host);case "@narrator":return new XpNarrator(c,store,host);case "@osk":return new XpKeyboard(c,store,host);case "@msinfo":return new XpSystemInformation(c,store,host);case "@cleanup":return new XpDiskCleanup(c,store,host);case "@restorepoints":return new XpRestorePoints(c,store,host);case "@transfer":return new XpTransferWizard(c,store,host);case "@search":return new XpSearchCompanion(c,store,host);case "@minesweeper":return new XpMinesweeper(c,store,host);case "@solitaire":case "@freecell":return new XpCardGame(c,store,host,id.equals("@freecell"));default:throw new IllegalArgumentException("Unknown XP program");}
    }
    private XpPrograms(){}
}
