package com.nthexperience.desktop;

import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import java.io.*;
import java.util.*;

/** Isolated synthetic document tree used only by the instrumentation APK. */
public final class XpFeatureDocumentsProvider extends DocumentsProvider {
    public static final String AUTHORITY="com.nthexperience.desktop.xp.featuredocs";
    private static final String[] DOC={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_FLAGS,DocumentsContract.Document.COLUMN_SIZE,DocumentsContract.Document.COLUMN_LAST_MODIFIED};
    private File root(){File file=new File(getContext().getCacheDir(),"xp-feature-fixtures");file.mkdirs();return file;}
    private File file(String id)throws FileNotFoundException{try{if(!id.equals("root")&&!id.startsWith("root/"))throw new IOException();File file=new File(root(),id.equals("root")?"":id.substring(5));String base=root().getCanonicalPath(),path=file.getCanonicalPath();if(!path.equals(base)&&!path.startsWith(base+File.separator))throw new IOException();return file;}catch(IOException e){throw new FileNotFoundException();}}
    @Override public boolean onCreate(){return true;}
    @Override public Cursor queryRoots(String[] projection){String[] cols=projection==null?new String[]{DocumentsContract.Root.COLUMN_ROOT_ID,DocumentsContract.Root.COLUMN_DOCUMENT_ID,DocumentsContract.Root.COLUMN_TITLE,DocumentsContract.Root.COLUMN_FLAGS}:projection;MatrixCursor out=new MatrixCursor(cols);MatrixCursor.RowBuilder row=out.newRow();for(String key:cols)row.add(key,key.equals(DocumentsContract.Root.COLUMN_ROOT_ID)||key.equals(DocumentsContract.Root.COLUMN_DOCUMENT_ID)?"root":key.equals(DocumentsContract.Root.COLUMN_TITLE)?"XP Test Files":key.equals(DocumentsContract.Root.COLUMN_FLAGS)?DocumentsContract.Root.FLAG_SUPPORTS_CREATE:0);return out;}
    private void add(MatrixCursor out,File file){String id="root"+(file.equals(root())?"":"/"+root().toPath().relativize(file.toPath()).toString());int flags=DocumentsContract.Document.FLAG_SUPPORTS_DELETE|DocumentsContract.Document.FLAG_SUPPORTS_WRITE|DocumentsContract.Document.FLAG_SUPPORTS_RENAME;if(file.isDirectory())flags|=DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE;MatrixCursor.RowBuilder row=out.newRow();for(String key:out.getColumnNames()){Object value=null;if(key.equals(DocumentsContract.Document.COLUMN_DOCUMENT_ID))value=id;else if(key.equals(DocumentsContract.Document.COLUMN_DISPLAY_NAME))value=file.getName();else if(key.equals(DocumentsContract.Document.COLUMN_MIME_TYPE))value=file.isDirectory()?DocumentsContract.Document.MIME_TYPE_DIR:file.getName().endsWith(".txt")?"text/plain":file.getName().endsWith(".zip")?"application/zip":"application/octet-stream";else if(key.equals(DocumentsContract.Document.COLUMN_FLAGS))value=flags;else if(key.equals(DocumentsContract.Document.COLUMN_SIZE))value=file.length();else if(key.equals(DocumentsContract.Document.COLUMN_LAST_MODIFIED))value=file.lastModified();row.add(key,value);}}
    @Override public Cursor queryDocument(String id,String[] projection)throws FileNotFoundException{File f=file(id);if(!f.exists())throw new FileNotFoundException();MatrixCursor out=new MatrixCursor(projection==null?DOC:projection);add(out,f);return out;}
    @Override public Cursor queryChildDocuments(String id,String[] projection,String sort)throws FileNotFoundException{MatrixCursor out=new MatrixCursor(projection==null?DOC:projection);File[] files=file(id).listFiles();if(files!=null)for(File f:files)add(out,f);return out;}
    @Override public String createDocument(String parent,String mime,String name)throws FileNotFoundException{if(name==null||name.isEmpty()||name.contains("/")||name.contains("\\")||name.equals(".")||name.equals(".."))throw new FileNotFoundException();File f=new File(file(parent),name);try{if(f.exists())throw new IOException("Exists");if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)){if(!f.mkdir())throw new IOException();}else if(!f.createNewFile())throw new IOException();return parent+"/"+name;}catch(IOException e){throw new FileNotFoundException(e.getMessage());}}
    @Override public ParcelFileDescriptor openDocument(String id,String mode,CancellationSignal signal)throws FileNotFoundException{return ParcelFileDescriptor.open(file(id),ParcelFileDescriptor.parseMode(mode));}
    @Override public void deleteDocument(String id)throws FileNotFoundException{if(id.equals("root"))throw new FileNotFoundException();remove(file(id));}
    private void remove(File f){File[] children=f.listFiles();if(children!=null)for(File c:children)remove(c);f.delete();}
    @Override public Bundle call(String method,String arg,Bundle extras){if(method.equals("resetFixture")){remove(root());root();Uri tree=DocumentsContract.buildTreeDocumentUri(AUTHORITY,"root");getContext().grantUriPermission(arg,tree,Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);Bundle out=new Bundle();out.putString("tree",tree.toString());return out;}return super.call(method,arg,extras);}
}
