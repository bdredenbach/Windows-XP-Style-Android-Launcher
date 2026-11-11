package com.nthexperience.desktop;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.json.*;

final class XpMinesweeper extends XpToolPanel {
    XpMinefield game;private Board board;private final TextView counter,clock;private boolean flagMode,active=true;private long startedAt,elapsed;
    private final Handler main=new Handler(Looper.getMainLooper());private final Runnable tick=new Runnable(){public void run(){if(closed||!active)return;refresh();main.postDelayed(this,1000);}};
    XpMinesweeper(Context c,DesktopStore store,Host host){super(c,store,host,"@minesweeper");command("New",()->fresh(9,9,10));command("Difficulty",()->Aero.dialog(c).setTitle("Minesweeper").setItems(new String[]{"Beginner — 9 × 9 / 10 mines","Intermediate — 16 × 16 / 40 mines","Expert — 30 × 16 / 99 mines"},(d,n)->{int[][] modes={{9,9,10},{16,16,40},{16,30,99}};fresh(modes[n][0],modes[n][1],modes[n][2]);}).show());command("Flag mode",()->{flagMode=!flagMode;say(flagMode?"Flag mode on":"Reveal mode • hold or right-click to flag");});
        counter=text("010",24);clock=text("000",24);for(TextView t:new TextView[]{counter,clock}){t.setTypeface(Typeface.MONOSPACE);t.setTextColor(0xffff3333);t.setBackgroundColor(Color.BLACK);t.setGravity(Gravity.CENTER);t.setPadding(dp(4),0,dp(4),0);}LinearLayout header=new LinearLayout(c);header.setGravity(Gravity.CENTER_VERTICAL);header.addView(counter,new LayoutParams(dp(70),dp(36)));TextView smile=Aero.button(c,"☺",()->fresh(game.rows,game.cols,game.mines));smile.setTextSize(22);smile.setContentDescription("New Minesweeper game");header.addView(smile,new LayoutParams(0,dp(36),1));header.addView(clock,new LayoutParams(dp(70),dp(36)));addView(header,indexOfChild(content),new LayoutParams(-1,dp(40)));
        try{JSONObject state=new JSONObject(c.getSharedPreferences("xp_games",0).getString("mines","{}"));game=XpMinefield.restore(state.getJSONObject("game").toString());elapsed=state.optLong("elapsed");}catch(Exception e){game=new XpMinefield(9,9,10);}startedAt=SystemClock.elapsedRealtime();build();main.post(tick);
    }
    private void fresh(int rows,int cols,int mines){game=new XpMinefield(rows,cols,mines);elapsed=0;startedAt=SystemClock.elapsedRealtime();build();save();}
    private void build(){content.removeAllViews();board=new Board(getContext());HorizontalScrollView horizontal=new HorizontalScrollView(getContext());horizontal.addView(board,new HorizontalScrollView.LayoutParams(dp(game.cols*28),dp(game.rows*28)));ScrollView vertical=new ScrollView(getContext());vertical.addView(horizontal,new ScrollView.LayoutParams(-1,dp(game.rows*28)));content.addView(vertical,new FrameLayout.LayoutParams(-1,-1));refresh();}
    private long seconds(){return Math.min(999,(elapsed+(game.started&&!game.ended?SystemClock.elapsedRealtime()-startedAt:0))/1000);}
    private void refresh(){counter.setText(String.format(Locale.ROOT,"%03d",game.mines-game.flagged()));clock.setText(String.format(Locale.ROOT,"%03d",seconds()));if(board!=null)board.invalidate();say(game.ended?game.won?"You cleared the minefield!":"Game over • New starts again":flagMode?"Flag mode • tap a covered cell":"Tap to reveal • hold to flag • tap a number to chord");}
    private void action(int at,boolean flag){boolean before=game.started,ended=game.ended;long current=seconds()*1000;if(flag)game.flag(at);else game.reveal(at,new Random());if(!before&&game.started)startedAt=SystemClock.elapsedRealtime();if(!ended&&game.ended)elapsed=current;save();refresh();}
    private void save(){try{getContext().getSharedPreferences("xp_games",0).edit().putString("mines",new JSONObject().put("game",game.state()).put("elapsed",seconds()*1000).toString()).apply();}catch(Exception ignored){}}
    @Override void active(boolean value){active=value;main.removeCallbacks(tick);save();if(value)main.post(tick);}
    @Override void close(){if(closed)return;save();closed=true;main.removeCallbacksAndMessages(null);}
    private final class Board extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final GestureDetector gestures;int selected;
        Board(Context c){super(c);setContentDescription("Minesweeper board. Tap reveal, hold flag; arrow keys select, Enter reveal, F flag.");setFocusable(true);gestures=new GestureDetector(c,new GestureDetector.SimpleOnGestureListener(){public boolean onDown(MotionEvent e){return true;}public boolean onSingleTapUp(MotionEvent e){action(index(e),flagMode);performClick();return true;}public void onLongPress(MotionEvent e){action(index(e),true);}});}
        @Override protected void onMeasure(int widthSpec,int heightSpec){setMeasuredDimension(dp(game.cols*28),dp(game.rows*28));}
        private int index(MotionEvent e){int x=(int)(e.getX()/dp(28)),y=(int)(e.getY()/dp(28));return x>=0&&x<game.cols&&y>=0&&y<game.rows?y*game.cols+x:-1;}
        @Override protected void onDraw(Canvas c){int size=dp(28);p.setTypeface(Aero.strong(getContext()));p.setTextSize(dp(19));p.setTextAlign(Paint.Align.CENTER);int[] colors={Color.BLACK,0xff0000ff,0xff008000,0xffff0000,0xff000080,0xff800000,0xff008080,Color.BLACK,0xff808080};for(int i=0;i<game.bombs.length;i++){int x=i%game.cols*size,y=i/game.cols*size;p.setColor(game.opened[i]?0xffc0c0c0:0xffbdbdbd);c.drawRect(x,y,x+size,y+size,p);p.setStrokeWidth(dp(1));p.setColor(game.opened[i]?0xff888888:Color.WHITE);c.drawLine(x,y,x+size,y,p);c.drawLine(x,y,x,y+size,p);p.setColor(0xff808080);c.drawLine(x+size-1,y,x+size-1,y+size,p);c.drawLine(x,y+size-1,x+size,y+size-1,p);if(game.flags[i]&&(!game.ended||game.won)){p.setColor(0xffd00000);c.drawText("⚑",x+size/2f,y+size*.76f,p);}else if(game.bombs[i]&&(game.ended||game.opened[i])){p.setColor(Color.BLACK);c.drawCircle(x+size/2f,y+size/2f,size*.24f,p);c.drawLine(x+size*.17f,y+size/2f,x+size*.83f,y+size/2f,p);c.drawLine(x+size/2f,y+size*.17f,x+size/2f,y+size*.83f,p);}else if(game.opened[i]&&game.adjacent(i)>0){int n=game.adjacent(i);p.setColor(colors[n]);c.drawText(String.valueOf(n),x+size/2f,y+size*.77f,p);}}
            if(isFocused()){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));p.setColor(0xffffa500);int x=selected%game.cols*size,y=selected/game.cols*size;c.drawRect(x+2,y+2,x+size-2,y+size-2,p);p.setStyle(Paint.Style.FILL);}}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_DOWN&&e.isButtonPressed(MotionEvent.BUTTON_SECONDARY)){action(index(e),true);return true;}return gestures.onTouchEvent(e);}
        @Override public boolean onKeyDown(int key,KeyEvent e){if(key==KeyEvent.KEYCODE_DPAD_LEFT)selected=Math.max(0,selected-1);else if(key==KeyEvent.KEYCODE_DPAD_RIGHT)selected=Math.min(game.bombs.length-1,selected+1);else if(key==KeyEvent.KEYCODE_DPAD_UP)selected=Math.max(0,selected-game.cols);else if(key==KeyEvent.KEYCODE_DPAD_DOWN)selected=Math.min(game.bombs.length-1,selected+game.cols);else if(key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_SPACE)action(selected,false);else if(key==KeyEvent.KEYCODE_F)action(selected,true);else return super.onKeyDown(key,e);invalidate();return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
