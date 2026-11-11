package com.nthexperience.desktop;

import android.graphics.Typeface;
import android.text.*;
import android.text.style.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

/** Text/HTML and a bounded text-only RTF subset; embedded objects are ignored. */
final class XpRichText {
    static String rtf(Spanned text){StringBuilder out=new StringBuilder("{\\rtf1\\ansi\\deff0{\\fonttbl{\\f0 Tahoma;}{\\f1 Times New Roman;}{\\f2 Courier New;}}\\uc1\n");boolean bold=false,italic=false,underline=false;int size=24,font=0,alignment=0;
        for(int i=0;i<text.length();i++){
            boolean b=false,it=false,u=text.getSpans(i,i+1,UnderlineSpan.class).length>0;int fs=24;for(StyleSpan span:text.getSpans(i,i+1,StyleSpan.class)){b|=(span.getStyle()&Typeface.BOLD)!=0;it|=(span.getStyle()&Typeface.ITALIC)!=0;}for(AbsoluteSizeSpan span:text.getSpans(i,i+1,AbsoluteSizeSpan.class))fs=span.getSize()*2;
            int family=0,align=0;for(TypefaceSpan span:text.getSpans(i,i+1,TypefaceSpan.class))family="serif".equals(span.getFamily())?1:"monospace".equals(span.getFamily())?2:0;for(AlignmentSpan span:text.getSpans(i,i+1,AlignmentSpan.class))align=span.getAlignment()==Layout.Alignment.ALIGN_CENTER?1:span.getAlignment()==Layout.Alignment.ALIGN_OPPOSITE?2:0;
            if(family!=font){out.append("\\f").append(family).append(' ');font=family;}if(align!=alignment){out.append(align==1?"\\qc ":align==2?"\\qr ":"\\ql ");alignment=align;}
            if(b!=bold){out.append(b?"\\b ":"\\b0 ");bold=b;}if(it!=italic){out.append(it?"\\i ":"\\i0 ");italic=it;}if(u!=underline){out.append(u?"\\ul ":"\\ulnone ");underline=u;}if(fs!=size){out.append("\\fs").append(fs).append(' ');size=fs;}
            char c=text.charAt(i);if(c=='\n')out.append("\\par\n");else if(c=='\t')out.append("\\tab ");else if(c=='\\'||c=='{'||c=='}')out.append('\\').append(c);else if(c<128)out.append(c);else out.append("\\u").append((int)(short)c).append('?');
        }return out.append('}').toString();
    }
    static SpannableStringBuilder read(byte[] bytes,String name)throws IOException{
        String text=XpTextFiles.decode(bytes),lower=name.toLowerCase(Locale.ROOT);if(text.startsWith("{\\rtf"))return parseRtf(text);if(lower.endsWith(".html")||lower.endsWith(".htm")||text.trim().startsWith("<!DOCTYPE html")||text.trim().startsWith("<html"))return new SpannableStringBuilder(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY));return new SpannableStringBuilder(text);
    }
    private static final class State {boolean b,i,u,skip;int size=12,uc=1,font=0,alignment=0;State copy(){State n=new State();n.b=b;n.i=i;n.u=u;n.skip=skip;n.size=size;n.uc=uc;n.font=font;n.alignment=alignment;return n;}}
    static SpannableStringBuilder parseRtf(String input)throws IOException{
        if(input.length()>1000000)throw new IOException("RTF is too large");SpannableStringBuilder out=new SpannableStringBuilder();ArrayDeque<State> stack=new ArrayDeque<>();State state=new State();int fallback=0;
        for(int at=0;at<input.length();){char c=input.charAt(at++);
            if(c=='{'){if(stack.size()>64)throw new IOException("RTF nesting limit");stack.push(state.copy());continue;}if(c=='}'){if(!stack.isEmpty())state=stack.pop();continue;}
            if(c=='\\'){
                if(at>=input.length())break;char next=input.charAt(at++);
                if(next=='\\'||next=='{'||next=='}'){if(!state.skip&&fallback==0)append(out,next,state);else if(fallback>0)fallback--;continue;}
                if(next=='*'){state.skip=true;continue;}if(next=='\''){if(at+2<=input.length()){try{char value=new String(new byte[]{(byte)Integer.parseInt(input.substring(at,at+2),16)},Charset.forName("windows-1252")).charAt(0);if(!state.skip&&fallback==0)append(out,value,state);else if(fallback>0)fallback--;}catch(RuntimeException ignored){}at+=2;}continue;}
                if(!Character.isLetter(next)){if(next=='~'&&!state.skip)append(out,'\u00a0',state);continue;}
                int start=at-1;while(at<input.length()&&Character.isLetter(input.charAt(at)))at++;String word=input.substring(start,at);int sign=1;if(at<input.length()&&input.charAt(at)=='-'){sign=-1;at++;}int nstart=at;while(at<input.length()&&Character.isDigit(input.charAt(at)))at++;int n=1;if(at>nstart)try{n=Integer.parseInt(input.substring(nstart,at))*sign;}catch(NumberFormatException ignored){}if(at<input.length()&&input.charAt(at)==' ')at++;
                if(Arrays.asList("fonttbl","colortbl","stylesheet","pict","object","info","header","footer").contains(word)){state.skip=true;continue;}if(state.skip)continue;
                switch(word){case "b":state.b=n!=0;break;case "i":state.i=n!=0;break;case "ul":state.u=n!=0;break;case "ulnone":state.u=false;break;case "fs":state.size=Math.max(8,Math.min(72,n/2));break;case "f":state.font=Math.max(0,Math.min(2,n));break;case "ql":state.alignment=0;break;case "qc":state.alignment=1;break;case "qr":state.alignment=2;break;case "pard":state.alignment=0;break;case "plain":state.b=state.i=state.u=false;state.size=12;state.font=0;break;case "uc":state.uc=Math.max(0,Math.min(8,n));break;case "u":append(out,(char)(n&0xffff),state);fallback=state.uc;break;case "par":case "line":append(out,'\n',state);break;case "tab":append(out,'\t',state);break;}
            }else if(c!='\r'&&c!='\n'&&!state.skip){if(fallback>0)fallback--;else append(out,c,state);}
            if(out.length()>200000)throw new IOException("RTF text is too large");
        }return out;
    }
    private static void append(SpannableStringBuilder out,char c,State s){int start=out.length();out.append(c);int style=(s.b?Typeface.BOLD:0)|(s.i?Typeface.ITALIC:0);
        if(style!=0){StyleSpan prior=null;if(start>0)for(StyleSpan span:out.getSpans(start-1,start,StyleSpan.class))if(out.getSpanEnd(span)==start&&span.getStyle()==style)prior=span;if(prior==null)out.setSpan(new StyleSpan(style),start,start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);else out.setSpan(prior,out.getSpanStart(prior),start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}
        if(s.u){UnderlineSpan prior=null;if(start>0)for(UnderlineSpan span:out.getSpans(start-1,start,UnderlineSpan.class))if(out.getSpanEnd(span)==start)prior=span;if(prior==null)out.setSpan(new UnderlineSpan(),start,start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);else out.setSpan(prior,out.getSpanStart(prior),start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}
        if(s.font!=0){String family=s.font==1?"serif":"monospace";TypefaceSpan prior=null;if(start>0)for(TypefaceSpan span:out.getSpans(start-1,start,TypefaceSpan.class))if(out.getSpanEnd(span)==start&&family.equals(span.getFamily()))prior=span;if(prior==null)out.setSpan(new TypefaceSpan(family),start,start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);else out.setSpan(prior,out.getSpanStart(prior),start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}
        if(s.alignment!=0){Layout.Alignment alignment=s.alignment==1?Layout.Alignment.ALIGN_CENTER:Layout.Alignment.ALIGN_OPPOSITE;AlignmentSpan prior=null;if(start>0)for(AlignmentSpan span:out.getSpans(start-1,start,AlignmentSpan.class))if(out.getSpanEnd(span)==start&&span.getAlignment()==alignment)prior=span;if(prior==null)out.setSpan(new AlignmentSpan.Standard(alignment),start,start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);else out.setSpan(prior,out.getSpanStart(prior),start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}
        if(s.size!=12){AbsoluteSizeSpan prior=null;if(start>0)for(AbsoluteSizeSpan span:out.getSpans(start-1,start,AbsoluteSizeSpan.class))if(out.getSpanEnd(span)==start&&span.getSize()==s.size)prior=span;if(prior==null)out.setSpan(new AbsoluteSizeSpan(s.size,true),start,start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);else out.setSpan(prior,out.getSpanStart(prior),start+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}
    }
    private XpRichText(){}
}
