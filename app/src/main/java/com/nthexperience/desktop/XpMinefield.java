package com.nthexperience.desktop;

import java.util.*;
import org.json.*;

/** First reveal is safe; board rules are independent of the UI. */
final class XpMinefield {
    final int rows,cols,mines;final boolean[] bombs,opened,flags;boolean started,ended,won;
    XpMinefield(int rows,int cols,int mines){if(rows<5||rows>16||cols<5||cols>30||mines<1||mines>=rows*cols-9)throw new IllegalArgumentException();this.rows=rows;this.cols=cols;this.mines=mines;bombs=new boolean[rows*cols];opened=new boolean[bombs.length];flags=new boolean[bombs.length];}
    int adjacent(int at){int n=0,r=at/cols,c=at%cols;for(int y=Math.max(0,r-1);y<=Math.min(rows-1,r+1);y++)for(int x=Math.max(0,c-1);x<=Math.min(cols-1,c+1);x++)if(bombs[y*cols+x])n++;return n;}
    void flag(int at){if(at>=0&&at<flags.length&&!opened[at]&&!ended)flags[at]=!flags[at];}
    int flagged(){int n=0;for(boolean f:flags)if(f)n++;return n;}
    void reveal(int at,Random random){if(at<0||at>=bombs.length||ended||flags[at])return;if(!started){List<Integer> choices=new ArrayList<>();for(int i=0;i<bombs.length;i++)if(Math.abs(i/cols-at/cols)>1||Math.abs(i%cols-at%cols)>1)choices.add(i);Collections.shuffle(choices,random);for(int i=0;i<mines;i++)bombs[choices.get(i)]=true;started=true;}
        if(opened[at]){int count=0;for(int n:neighbors(at))if(flags[n])count++;if(count==adjacent(at))for(int n:neighbors(at))if(!flags[n]&&!opened[n]){open(n);if(ended)break;}}else open(at);checkWin();
    }
    private void open(int at){if(flags[at]||opened[at]||ended)return;if(bombs[at]){opened[at]=true;ended=true;return;}ArrayDeque<Integer> queue=new ArrayDeque<>();opened[at]=true;queue.add(at);while(!queue.isEmpty()){int n=queue.remove();if(adjacent(n)!=0)continue;for(int next:neighbors(n))if(!opened[next]&&!flags[next]&&!bombs[next]){opened[next]=true;queue.add(next);}}}
    private int[] neighbors(int at){int[] values=new int[8];int count=0,r=at/cols,c=at%cols;for(int y=Math.max(0,r-1);y<=Math.min(rows-1,r+1);y++)for(int x=Math.max(0,c-1);x<=Math.min(cols-1,c+1);x++)if(y*cols+x!=at)values[count++]=y*cols+x;return Arrays.copyOf(values,count);}
    private void checkWin(){if(ended)return;int open=0;for(boolean value:opened)if(value)open++;if(open==bombs.length-mines){won=ended=true;for(int i=0;i<flags.length;i++)if(bombs[i])flags[i]=true;}}
    JSONObject state()throws JSONException{return new JSONObject().put("r",rows).put("c",cols).put("m",mines).put("bombs",bits(bombs)).put("open",bits(opened)).put("flags",bits(flags)).put("started",started).put("ended",ended).put("won",won);}
    private static String bits(boolean[] values){StringBuilder s=new StringBuilder();for(boolean v:values)s.append(v?'1':'0');return s.toString();}
    static XpMinefield restore(String json)throws JSONException{JSONObject v=new JSONObject(json);XpMinefield m;try{m=new XpMinefield(v.getInt("r"),v.getInt("c"),v.getInt("m"));}catch(IllegalArgumentException e){throw new JSONException("Invalid board");}boolean[][] arrays={m.bombs,m.opened,m.flags};String[] keys={"bombs","open","flags"};for(int n=0;n<3;n++){String s=v.getString(keys[n]);if(s.length()!=m.bombs.length||!s.matches("[01]+"))throw new JSONException("Invalid board data");for(int i=0;i<s.length();i++)arrays[n][i]=s.charAt(i)=='1';}m.started=v.getBoolean("started");m.ended=v.getBoolean("ended");m.won=v.getBoolean("won");int count=0;for(boolean b:m.bombs)if(b)count++;if(count!=(m.started?m.mines:0)||m.won&&!m.ended)throw new JSONException("Invalid mine count");return m;}
}
