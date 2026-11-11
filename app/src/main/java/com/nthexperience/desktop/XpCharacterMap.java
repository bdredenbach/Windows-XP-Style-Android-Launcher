package com.nthexperience.desktop;

import android.content.*;
import android.graphics.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

final class XpCharacterMap extends XpToolPanel {
    private final GridLayout grid;private EditText query,chosen;private TextView preview;private int block;
    private static final String[] BLOCKS={"Basic Latin","Latin-1 Supplement","Greek","Cyrillic","Punctuation","Symbols"};
    private static final int[][] RANGES={{32,127},{160,255},{0x370,0x3ff},{0x400,0x4ff},{0x2000,0x206f},{0x2600,0x26ff}};
    XpCharacterMap(Context c,DesktopStore store,Host host){super(c,store,host,"@charmap");command("Copy",()->copy("Characters",chosen.getText().toString()));command("Clear",()->chosen.setText(""));
        LinearLayout body=Aero.column(c);Spinner ranges=new Spinner(c);ranges.setAdapter(new ArrayAdapter<>(c,android.R.layout.simple_spinner_dropdown_item,BLOCKS));ranges.setContentDescription("Character set");body.addView(ranges,new LayoutParams(-1,dp(36)));
        query=new EditText(c);query.setTextSize(12);query.setSingleLine();query.setHint("Search name or U+code in this set");query.setContentDescription("Find character");body.addView(query,new LayoutParams(-1,dp(38)));
        grid=new GridLayout(c);grid.setColumnCount(8);ScrollView scroll=new ScrollView(c);scroll.addView(grid);body.addView(scroll,new LayoutParams(-1,0,1));preview=text("Select a character",14);preview.setContentDescription("Selected character");body.addView(preview,new LayoutParams(-1,dp(36)));chosen=new EditText(c);chosen.setTextSize(19);chosen.setSingleLine();chosen.setHint("Characters to copy");chosen.setContentDescription("Characters to copy");body.addView(chosen,new LayoutParams(-1,dp(40)));content.addView(body,new FrameLayout.LayoutParams(-1,-1));
        ranges.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int n,long id){block=n;render();}});query.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int n,int f){}public void onTextChanged(CharSequence s,int a,int b,int n){render();}public void afterTextChanged(Editable e){}});render();
    }
    private void render(){grid.removeAllViews();String needle=query.getText().toString().trim().toLowerCase(Locale.ROOT);int count=0;for(int cp=RANGES[block][0];cp<=RANGES[block][1];cp++){
        if(Character.isISOControl(cp)||!Character.isDefined(cp))continue;String name=Character.getName(cp),code=String.format(Locale.ROOT,"U+%04X",cp),symbol=new String(Character.toChars(cp));if(!needle.isEmpty()&&!(String.valueOf(name)+" "+code+" "+symbol).toLowerCase(Locale.ROOT).contains(needle))continue;
        TextView cell=Aero.text(getContext(),symbol,20,Color.BLACK);cell.setGravity(Gravity.CENTER);cell.setFocusable(true);cell.setBackground(Aero.buttonBackground(getContext()));cell.setContentDescription(code+" "+name);cell.setOnClickListener(v->{preview.setText(symbol+"  "+code+"  "+name);chosen.append(symbol);});GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=dp(36);lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);grid.addView(cell,lp);count++;
    }say(count+" characters • tap to append, then Copy");}
}
