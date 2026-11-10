package com.nthexperience.desktop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.*;

/** Bounded plain-text decoding for files produced by both XP and modern editors. */
final class XpTextFiles {
    static String decode(byte[] bytes)throws IOException {
        if(bytes.length>1000000)throw new IOException("Document too large");String text;
        if(bytes.length>=2&&(bytes[0]&255)==255&&(bytes[1]&255)==254)text=new String(bytes,2,bytes.length-2,StandardCharsets.UTF_16LE);
        else if(bytes.length>=2&&(bytes[0]&255)==254&&(bytes[1]&255)==255)text=new String(bytes,2,bytes.length-2,StandardCharsets.UTF_16BE);
        else{try{text=StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();}catch(CharacterCodingException ansi){text=new String(bytes,Charset.forName("windows-1252"));}if(text.startsWith("\ufeff"))text=text.substring(1);}
        if(text.length()>200000)throw new IOException("Document too large");return text.replace("\r\n","\n");
    }
    private XpTextFiles(){}
}
