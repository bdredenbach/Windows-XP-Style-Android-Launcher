package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.media3.common.*;
import androidx.media3.common.util.UnstableApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;

/** Both skins are views of the same local library and playback session. */
@UnstableApi
final class MediaPanel extends LinearLayout implements Player.Listener {
    private static final int TILE_PAGE = 16;
    interface Host {
        void addFiles();
        void switchView(boolean center);
        void closeCenter();
        void libraryChanged();
    }

    private final MediaPlayback playback;
    private final MediaLibrary library;
    private final Host host;
    private final boolean center;
    private final FrameLayout body, picture;
    private final TextureView video;
    private final ImageView art;
    private final View musicIcon;
    private final XpMusicVisualizer waveform;
    private final TextView visualChoice;
    private int visualMode;
    private final LinearLayout playingArea,queueSide;
    private final ListView inlineQueue;
    private final TextView brand, title, artist, time, state, switchView;
    private final MediaTransportButton play;
    private final Map<String,TextView> navigation = new LinkedHashMap<>();
    private final SeekBar seek;
    private final LinearLayout now;
    private final int ink;
    private String page;
    private int tilePage;
    private boolean seeking, attached, visualize;
    private byte[] artwork;
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!attached || playback.released) return;
            if (isShown()) updateTime();
            postDelayed(this, 250);
        }
    };

    MediaPanel(Context context, MediaPlayback playback, MediaLibrary library, Host host, boolean center) {
        super(context);
        this.playback = playback;
        this.library = library;
        this.host = host;
        this.center = center;
        visualMode=Math.max(0,Math.min(4,context.getSharedPreferences("xp_media",0).getInt("visualization",0)));
        visualize=!center&&visualMode<4;
        ink = center ? 0xffecfaff : Aero.INK;
        setOrientation(VERTICAL);
        setPadding(dp(center ? 16 : 0), dp(center?6:0), dp(center ? 16 : 0), dp(center?6:0));
        if (center) setAccessibilityPaneTitle(MediaLibrary.CENTER_TITLE);

        LinearLayout heading = new LinearLayout(context);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        brand = label(center ? MediaLibrary.CENTER_TITLE : "Media Library", center ? 22 : 15);
        brand.setPadding(dp(6), 0, 0, 0);
        brand.setSingleLine(true);
        brand.setEllipsize(TextUtils.TruncateAt.END);
        heading.addView(brand, new LayoutParams(0, dp(center ? 45 : 36), 1));
        heading.addView(button("Add files", host::addFiles), new LayoutParams(dp(86), dp(42)));
        if (center) heading.addView(button("Close", host::closeCenter), new LayoutParams(dp(72), dp(42)));
        addView(heading);
        if(!center){heading.setVisibility(GONE);LinearLayout menuBar=new LinearLayout(context);menuBar.setBackgroundColor(XpTheme.surface(context));for(String name:new String[]{"File","View","Play","Tools","Help"}){TextView item=Aero.text(context,name,11,Color.BLACK);item.setPadding(dp(7),0,dp(7),0);item.setFocusable(true);item.setBackground(XpControls.menuSelection(context));item.setTextColor(XpControls.menuInk(Color.BLACK));item.setContentDescription("Media Player "+name+" menu");item.setOnClickListener(v->classicMenu(item,name));menuBar.addView(item,new LayoutParams(-2,dp(27)));}addView(menuBar);}

        LinearLayout tabs = new LinearLayout(context);
        if (center) {
            tab(tabs, "Start"); tab(tabs, "Music"); tab(tabs, "Videos");
        }
        tab(tabs, "Now Playing");if(!center)tab(tabs,"Library"); tab(tabs, "Queue");
        addView(tabs);

        body = new FrameLayout(context);
        if(center)addView(body, new LayoutParams(-1, 0, 1));
        else{removeView(tabs);tabs.setOrientation(VERTICAL);tabs.setPadding(dp(4),dp(12),dp(4),dp(4));tabs.setBackground(Aero.gloss(context,new int[]{0xff5c73c6,0xff233d85,0xff152b68},0,0));for(int i=0;i<tabs.getChildCount();i++)tabs.getChildAt(i).setLayoutParams(new LayoutParams(-1,dp(48)));LinearLayout area=new LinearLayout(context);area.addView(tabs,new LayoutParams(dp(104),-1));area.addView(body,new LayoutParams(0,-1,1));addView(area,new LayoutParams(-1,0,1));}
        now = Aero.column(context);now.setBackgroundColor(0xff061328);
        title = label("Nothing playing", center ? 19 : 15);
        title.setTextColor(0xffe5f6ff);
        title.setPadding(dp(8), 0, dp(8), 0);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        now.addView(title, new LayoutParams(-1, dp(28)));
        artist = label("", 11);
        artist.setSingleLine(true);
        artist.setEllipsize(TextUtils.TruncateAt.END);
        artist.setPadding(dp(8), 0, dp(8), 0);
        artist.setTextColor(0xff9db9d1);
        now.addView(artist, new LayoutParams(-1, dp(18)));
        visualChoice=label("",11);visualChoice.setTextColor(0xff17366d);visualChoice.setGravity(Gravity.CENTER_VERTICAL);visualChoice.setPadding(dp(8),0,dp(5),0);visualChoice.setBackground(Aero.gloss(context,new int[]{0xffeff3fc,0xffb6c6e6},0,0xff879cca));visualChoice.setFocusable(true);visualChoice.setOnClickListener(v->visualizationMenu(visualChoice));
        visualChoice.setVisibility(center?GONE:VISIBLE);now.addView(visualChoice,new LayoutParams(-1,dp(28)));
        picture = new FrameLayout(context);
        picture.setBackground(Aero.gloss(context,
                new int[]{0xff02050b, 0xff0e2a42, 0xff184462, 0xff050911}, 2, 0xff193952));
        musicIcon = new AeroIcon(context, "mediaplayer");
        picture.addView(musicIcon, new FrameLayout.LayoutParams(dp(92), dp(92), Gravity.CENTER));
        art = new ImageView(context);
        art.setScaleType(ImageView.ScaleType.FIT_CENTER);
        picture.addView(art, new FrameLayout.LayoutParams(-1, -1));
        waveform = new XpMusicVisualizer(context,playback.signal,()->!playback.released&&playback.player.isPlaying());
        waveform.mode(Math.min(visualMode,3));
        picture.addView(waveform, new FrameLayout.LayoutParams(-1, -1));
        video = new TextureView(context);
        video.setContentDescription("Media video");
        picture.addView(video, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        picture.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob) -> resizeVideo());
        now.addView(picture, new LayoutParams(-1, 0, 1));
        playingArea=new LinearLayout(context);playingArea.addView(now,new LayoutParams(0,-1,1));
        queueSide=Aero.column(context);queueSide.setBackgroundColor(0xfff4f6fd);TextView queueTitle=label("Now Playing List",11);queueTitle.setPadding(dp(7),0,0,0);queueTitle.setBackgroundColor(0xffd3dcef);queueSide.addView(queueTitle,new LayoutParams(-1,dp(28)));
        inlineQueue=new ListView(context);inlineQueue.setContentDescription("Now Playing side list");inlineQueue.setDividerHeight(1);inlineQueue.setOnItemClickListener((parent,row,index,id)->playback.select(index));queueSide.addView(inlineQueue,new LayoutParams(-1,0,1));queueSide.setVisibility(GONE);playingArea.addView(queueSide,new LayoutParams(dp(164),-1));
        body.addView(playingArea,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout timeline = new LinearLayout(context);
        timeline.setGravity(Gravity.CENTER_VERTICAL);
        seek = new SeekBar(context);
        seek.setContentDescription("Seek media");
        seek.setMax(1000);
        seek.setProgressTintList(ColorStateList.valueOf(0xff67b8ec));
        seek.setThumbTintList(ColorStateList.valueOf(0xffc2ecff));
        timeline.addView(seek, new LayoutParams(0, dp(33), 1));
        time = label("0:00 / 0:00", 11);
        time.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        time.setTextColor(center?0xffd9eaff:0xff10295b);
        time.setPadding(0, 0, dp(5), 0);
        timeline.addView(time, new LayoutParams(dp(102), dp(33)));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar bar) { seeking = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) {
                seeking = false;
                seekTo(bar.getProgress());
            }
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean user) {
                if (user && !seeking) seekTo(progress);
            }
        });

        LinearLayout deck = Aero.column(context);
        deck.setBackground(Aero.gloss(context, center
                ? new int[]{0xff19395b, 0xff112943, 0xff0b1e37}
                : new int[]{0xffedf1fc, 0xffb2c3e5, 0xffd8e2f5}, center?5:0, 0xff738bb5));
        deck.addView(timeline);
        LinearLayout controls = new LinearLayout(context);
        controls.setGravity(Gravity.CENTER);
        transport(controls,MediaTransportButton.Action.STOP,()->{playback.player.pause();playback.player.seekTo(0);});
        transport(controls, MediaTransportButton.Action.PREVIOUS,
                () -> playback.select(playback.player.getCurrentMediaItemIndex() - 1));
        play = transport(controls, MediaTransportButton.Action.PLAY, playback::playPause);
        transport(controls, MediaTransportButton.Action.NEXT,
                () -> playback.select(playback.player.getCurrentMediaItemIndex() + 1));
        transport(controls, MediaTransportButton.Action.VOLUME, this::volume);
        deck.addView(controls);
        addView(deck);
        LinearLayout options = new LinearLayout(context);
        switchView = control(options, center ? "Open Player" : "Media Center", () -> host.switchView(!center));
        if (center) control(options, "Visualize", () -> {
            visualize = !visualize;
            show("Now Playing");
        });
        state = label("", 11);
        state.setSingleLine();
        state.setPadding(dp(6), 0, 0, 0);
        state.setEllipsize(TextUtils.TruncateAt.END);
        options.addView(state, new LayoutParams(0, dp(center?40:28), 1));
        if(!center){switchView.setTextSize(11);switchView.getLayoutParams().height=dp(28);switchView.setPadding(dp(5),0,dp(5),0);}
        addView(options);
        show(center&&playback.current()==null?"Start":"Now Playing");
        updatePlayback();
    }

    private void classicMenu(View anchor,String name){DesktopContextMenu menu=new DesktopContextMenu(getContext(),"Media Player "+name+" menu");switch(name){
        case "File":menu.item("Open…","files",host::addFiles);menu.item("Add to Media Library…","music",host::addFiles);break;
        case "View":menu.item("Visualizations…","mediaplayer",()->visualizationMenu(visualChoice));menu.item("Now Playing","mediaplayer",this::showNow);menu.item("Media Library","music",()->show("Library"));menu.item("Now Playing List","files",()->show("Queue"));menu.item("Media Center","mediacenter",()->host.switchView(true));break;
        case "Play":menu.item("Play / Pause","mediaplayer",playback::playPause);menu.item("Stop","empty",()->{playback.player.pause();playback.player.seekTo(0);});menu.item("Previous","empty",()->playback.select(playback.player.getCurrentMediaItemIndex()-1));menu.item("Next","empty",()->playback.select(playback.player.getCurrentMediaItemIndex()+1));break;
        case "Tools":menu.item("Volume…","sound",this::volume);break;
        default:menu.item("About Nth Media Player","mediaplayer",()->Aero.dialog(getContext()).setTitle("Nth Media Player").setMessage("Nth Desktop XP media library and player. Select local music or video files with File > Open.").setPositiveButton("OK",null).show());
        }ContextMenus.centered(anchor,(x,y)->ContextMenus.show(menu,anchor,x,y));}
    private int dp(int n) { return Aero.dp(getContext(), n); }
    private TextView label(String text, int size) {
        TextView view = Aero.text(getContext(), text, size, ink);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }
    private TextView button(String text, Runnable action) {
        TextView button = Aero.button(getContext(), text, action);
        if (center) {
            button.setTextColor(0xffe5f7ff);
            button.setBackground(navigationSurface(true, false));
        }
        return button;
    }
    private TextView control(LinearLayout row, String text, Runnable action) {
        TextView button = button(text, action);
        button.setPadding(dp(4), 0, dp(4), 0);
        row.addView(button, new LayoutParams(0, dp(42), 1));
        return button;
    }
    MediaPlayback playback(){return playback;}

    private MediaTransportButton transport(LinearLayout row, MediaTransportButton.Action action, Runnable click) {
        MediaTransportButton key = new MediaTransportButton(getContext(), action, click);
        key.xp(!center);row.addView(key, new LayoutParams(dp(44), dp(46)));
        return key;
    }
    private void tab(LinearLayout row, String name) {
        TextView tab = control(row, name, () -> show(name));
        tab.setSingleLine(true);
        tab.setEllipsize(TextUtils.TruncateAt.END);
        if (center && name.equals("Now Playing")) {
            ((LayoutParams) tab.getLayoutParams()).weight = 1.7f;
        }
        if(!center){tab.setText(name.equals("Library")?"Media Library":name.equals("Queue")?"Now Playing List":name);tab.setTextSize(11);tab.setSingleLine(false);tab.setMaxLines(2);}
        navigation.put(name, tab);
    }
    private void appearance() {
        Context context = getContext();
        boolean playing = page.equals("Now Playing");
        boolean dark = center;
        setBackground(Aero.gloss(context, center
                ? new int[]{0xff06152f, 0xff12396a, 0xff236d9d, 0xff10264e}
                : new int[]{0xffd8def1,0xffa0b1db,0xffbac7e6}, 0, 0));
        brand.setText(center ? MediaLibrary.CENTER_TITLE : playing ? "Now Playing" : "Media Library");
        brand.setTextColor(dark ? 0xffeff9ff : 0xff264768);
        state.setTextColor(dark ? 0xffb7dbf2 : 0xff355874);
        switchView.setTextColor(dark ? 0xffedf9ff : 0xff284763);
        switchView.setBackground(navigationSurface(dark, false));
        for (Map.Entry<String,TextView> item : navigation.entrySet()) {
            boolean selected = item.getKey().equals(page);
            TextView tab = item.getValue();
            tab.setSelected(selected);
            tab.setTextColor(0xffedf9ff);
            tab.setBackground(navigationSurface(true, selected));
        }
    }
    private StateListDrawable navigationSurface(boolean dark, boolean selected) {
        Context context = getContext();
        StateListDrawable surface = new StateListDrawable();
        int start = dark ? 0xff3877ad : 0xfff7fcff;
        int end = dark ? 0xff193b65 : 0xffb5d8f0;
        int border = dark ? 0xff7cadcd : 0xff8ab5d3;
        surface.addState(new int[]{android.R.attr.state_pressed}, Aero.panel(context, end, start, 4, border));
        surface.addState(new int[]{android.R.attr.state_focused}, Aero.panel(context, start, end, 4, 0xff88d5ff));
        surface.addState(new int[]{}, selected ? Aero.panel(context, start, end, 4, border)
                : Aero.panel(context, 0x00ffffff, 0x00ffffff, 4, 0));
        return surface;
    }
    void showNow() { show("Now Playing"); }
    TextureView videoSurface() { return video; }
    void libraryChanged() {
        if (!page.equals("Now Playing") && !page.equals("Queue")) populate();
    }

    private void show(String name) {
        if (!name.equals(page)) tilePage = 0;
        page = name;
        appearance();
        populate();
        updatePlayback();
    }
    private List<MediaLibrary.Entry> visibleItems() {
        if (page.equals("Queue")) return playback.queue();
        List<MediaLibrary.Entry> result = new ArrayList<>(library.items());
        if (center) result.removeIf(item -> item.video() != page.equals("Videos"));
        return result;
    }
    private void populate() {
        while (body.getChildCount() > 1) body.removeViewAt(1);
        boolean playing = page.equals("Now Playing");
        playingArea.setVisibility(playing ? VISIBLE : GONE);
        if (playing) return;
        if (page.equals("Start")) { centerStart(); return; }
        List<MediaLibrary.Entry> items = visibleItems();
        if (items.isEmpty()) {
            TextView empty = label(page.equals("Queue") ? "The queue is empty" : center?"Choose Add files to build your library":"Use File > Open to build your library", 15);
            empty.setGravity(Gravity.CENTER);
            body.addView(empty, new FrameLayout.LayoutParams(-1, -1));
        } else if (center && !page.equals("Queue")) {
            tiles(items);
        } else {
            ListView list = new ListView(getContext());
            list.setContentDescription(page.equals("Queue") ? "Media queue" : "Media library");
            list.setDrawSelectorOnTop(true);
            list.setSelector(navigationSurface(center, false));
            list.setAdapter(new BaseAdapter() {
                @Override public int getCount() { return items.size(); }
                @Override public Object getItem(int index) { return items.get(index); }
                @Override public long getItemId(int index) { return index; }
                @Override public View getView(int index, View old, ViewGroup parent) {
                    MediaLibrary.Entry item = items.get(index);
                    LinearLayout row = new LinearLayout(getContext());
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(7), 0, dp(8), 0);
                    row.setMinimumHeight(dp(46));
                    row.setBackgroundColor(center ? (index % 2 == 0 ? 0x202e83c0 : 0x10112c55)
                            : (index % 2 == 0 ? 0xfffafdff : 0xffeaf4fc));
                    row.addView(new AeroIcon(getContext(), item.video() ? "videofile" : "musicfile"),
                            new LayoutParams(dp(29), dp(29)));
                    TextView name = label(item.name, 14);
                    name.setPadding(dp(7), 0, dp(3), 0);
                    name.setSingleLine(true);
                    name.setEllipsize(TextUtils.TruncateAt.END);
                    row.addView(name, new LayoutParams(0, dp(46), 1));
                    TextView kind = label(item.video() ? "Video" : "Music", 11);
                    kind.setTextColor(center ? 0xffa9cce8 : 0xff537595);
                    row.addView(kind, new LayoutParams(-2, dp(46)));
                    return row;
                }
            });
            list.setOnItemClickListener((parent, row, index, id) -> play(items, index));
            if (!page.equals("Queue")) list.setOnItemLongClickListener((parent, row, index, id) -> {
                libraryMenu(row, items.get(index));
                return true;
            });
            body.addView(list, new FrameLayout.LayoutParams(-1, -1));
        }
    }
    private void centerStart() {
        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout home = Aero.column(getContext());
        home.setGravity(Gravity.CENTER_VERTICAL);
        home.setPadding(dp(6), dp(5), dp(6), dp(6));
        TextView heading = label("start", 31);
        heading.setPadding(dp(8), 0, 0, 0);
        home.addView(heading, new LayoutParams(-1, dp(42)));
        String[] names = {"Music", "Videos", "Now Playing"};
        String[] descriptions = {"Browse your music library", "Browse your video library", "Return to playback"};
        String[] icons = {"musicfile", "videofile", "mediaplayer"};
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            LinearLayout tile = tile(name, icons[i], () -> show(name));
            tile.setOrientation(HORIZONTAL);
            tile.setGravity(Gravity.CENTER_VERTICAL);
            tile.setPadding(dp(10), dp(4), dp(8), dp(4));
            tile.removeViewAt(1);
            LinearLayout copy = Aero.column(getContext());
            copy.setPadding(dp(12), 0, 0, 0);
            TextView caption = label(name, 20);
            caption.setSingleLine(true);
            caption.setEllipsize(TextUtils.TruncateAt.END);
            copy.addView(caption);
            TextView detail = label(descriptions[i], 11);
            detail.setTextColor(0xffa8d1ee);
            detail.setSingleLine(true);
            detail.setEllipsize(TextUtils.TruncateAt.END);
            copy.addView(detail);
            tile.addView(copy, new LayoutParams(0, -2, 1));
            LayoutParams bounds = new LayoutParams(-1, dp(66));
            bounds.bottomMargin = dp(5);
            home.addView(tile, bounds);
        }
        scroll.addView(home);
        body.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
    }
    private LinearLayout tile(String name, String icon, Runnable activate) {
        LinearLayout tile = Aero.column(getContext());
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(6), dp(4), dp(6), dp(4));
        tile.setBackground(Aero.panel(getContext(), 0xff2f5b83, 0xff172e53, 5, 0xff6594b5));
        tile.setFocusable(true);
        tile.setOnFocusChangeListener((view, focused) -> tile.setBackground(Aero.panel(getContext(),
                focused ? 0xff5088b3 : 0xff2f5b83, 0xff172e53, 5, focused ? 0xffd1edff : 0xff6594b5)));
        tile.setOnClickListener(view -> activate.run());
        tile.setContentDescription(name);
        tile.addView(new AeroIcon(getContext(), icon), new LayoutParams(dp(44), dp(44)));
        TextView caption = label(name, 13);
        caption.setGravity(Gravity.CENTER);
        caption.setMaxLines(2);
        caption.setEllipsize(TextUtils.TruncateAt.END);
        tile.addView(caption, new LayoutParams(-1, -2));
        return tile;
    }
    private void tiles(List<MediaLibrary.Entry> items) {
        LinearLayout shelf = Aero.column(getContext());
        TextView category = label(page.toLowerCase(Locale.ROOT), 26);
        shelf.addView(category, new LayoutParams(-1, dp(38)));
        HorizontalScrollView scroll = new HorizontalScrollView(getContext());
        LinearLayout row = new LinearLayout(getContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        tilePage = Math.min(tilePage, (items.size() - 1) / TILE_PAGE);
        for (int index = tilePage * TILE_PAGE; index < Math.min(items.size(), (tilePage + 1) * TILE_PAGE); index++) {
            int selected = index;
            MediaLibrary.Entry item = items.get(index);
            LinearLayout tile = tile(item.name, item.video() ? "videofile" : "musicfile", () -> play(items, selected));
            tile.setOnLongClickListener(view -> { libraryMenu(tile, item); return true; });
            tile.setContentDescription("Play " + item.name);
            LayoutParams size = new LayoutParams(dp(138), dp(105));
            size.rightMargin = dp(8);
            row.addView(tile, size);
        }
        scroll.addView(row);
        shelf.addView(scroll, new LayoutParams(-1, 0, 1));
        if (items.size() > TILE_PAGE) {
            LinearLayout pages = new LinearLayout(getContext());
            control(pages, "Previous page", () -> { if (tilePage > 0) { tilePage--; populate(); } });
            control(pages, "Next page", () -> { if ((tilePage + 1) * TILE_PAGE < items.size()) { tilePage++; populate(); } });
            shelf.addView(pages);
        }
        body.addView(shelf, new FrameLayout.LayoutParams(-1, -1));
    }
    private void play(List<MediaLibrary.Entry> items, int index) {
        if (page.equals("Queue")) playback.select(index);
        else playback.play(items, index);
        showNow();
    }
    private void libraryMenu(View anchor, MediaLibrary.Entry item) {
        DesktopContextMenu menu = new DesktopContextMenu(getContext(), "Media library commands");
        menu.item("Play this file", "musicfile", () -> {
            playback.play(Collections.singletonList(item), 0);
            showNow();
        });
        menu.item("Remove from library", "delete", () -> { library.remove(item); host.libraryChanged(); });
        ContextMenus.centered(anchor, (x, y) -> ContextMenus.show(menu, anchor, x, y));
    }
    private void volume() {
        SeekBar volume = new SeekBar(getContext());
        volume.setMax(100);
        volume.setProgress(Math.round(playback.player.getVolume() * 100));
        volume.setContentDescription("Media volume");
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
            @Override public void onProgressChanged(SeekBar bar, int value, boolean user) {
                if (user) playback.player.setVolume(value / 100f);
            }
        });
        Aero.dialog(getContext()).setTitle("Media volume").setView(volume).setPositiveButton("Done", null).show();
    }
    private void seekTo(int fraction) {
        long duration = playback.player.getDuration();
        if (duration > 0 && playback.player.isCurrentMediaItemSeekable())
            playback.player.seekTo(duration * fraction / seek.getMax());
    }
    private void updateTime() {
        long duration = Math.max(0, playback.player.getDuration());
        long position = Math.max(0, playback.player.getCurrentPosition());
        time.setText(clock(position) + " / " + clock(duration));
        seek.setEnabled(duration > 0 && playback.player.isCurrentMediaItemSeekable());
        if (!seeking) seek.setProgress(duration == 0 ? 0 : (int) (position * seek.getMax() / duration));
    }
    private static String clock(long millis) {
        long seconds = millis / 1000;
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }
    private void updatePlayback() {
        MediaLibrary.Entry item = playback.current();
        MediaMetadata metadata = playback.player.getMediaMetadata();
        title.setText(item == null ? "Nothing playing" : metadata.title == null ? item.name : metadata.title);
        artist.setText(metadata.artist);
        artist.setVisibility(item != null && !TextUtils.isEmpty(metadata.artist) ? VISIBLE : GONE);
        PlaybackException error = playback.player.getPlayerError();
        boolean active = playback.player.isPlaying();
        boolean moving = playback.player.getPlayWhenReady() && playback.player.getPlaybackState() != Player.STATE_ENDED
                && error == null;
        play.action(moving ? MediaTransportButton.Action.PAUSE : MediaTransportButton.Action.PLAY);
        play.setEnabled(item != null);
        state.setText(error != null ? "File unavailable or format unsupported" : playback.player.getPlaybackState()
                == Player.STATE_BUFFERING ? "Opening media…" : active ? "Playing" : item == null ? "" : "Paused");
        boolean hasVideo = item != null && item.video();
        video.setVisibility(hasVideo ? VISIBLE : GONE);
        video.setKeepScreenOn(hasVideo && active && page.equals("Now Playing"));
        visualChoice.setText((visualMode<4?XpMusicVisualizer.NAMES[visualMode]:"Album Art")+"  ▾");visualChoice.setContentDescription("Visualization: "+(visualMode<4?XpMusicVisualizer.NAMES[visualMode]:"Album Art"));visualChoice.setEnabled(!hasVideo);
        waveform.setVisibility(!hasVideo && visualize ? VISIBLE : GONE);
        waveform.invalidate();
        if (artwork != metadata.artworkData) {
            artwork = metadata.artworkData;
            art.setImageBitmap(decodeArtwork(artwork));
        }
        boolean showArt = !hasVideo && !visualize && art.getDrawable() != null;
        art.setVisibility(showArt ? VISIBLE : GONE);
        musicIcon.setVisibility(!hasVideo && !visualize && !showArt ? VISIBLE : GONE);
        resizeVideo();
        updateTime();
    }
    private static Bitmap decodeArtwork(byte[] data) {
        if (data == null || data.length > 12 * 1024 * 1024) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = 1;
        while (Math.max(options.outHeight, options.outWidth) / options.inSampleSize > 512) options.inSampleSize *= 2;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }
    private void resizeVideo() {
        VideoSize size = playback.player.getVideoSize();
        if (size.width <= 0 || size.height <= 0 || picture.getWidth() == 0 || picture.getHeight() == 0) return;
        float aspect = size.width * size.pixelWidthHeightRatio / size.height;
        int width = Math.max(1, Math.min(picture.getWidth(), Math.round(picture.getHeight() * aspect)));
        int height = Math.max(1, Math.min(picture.getHeight(), Math.round(picture.getWidth() / aspect)));
        ViewGroup.LayoutParams bounds = video.getLayoutParams();
        if (bounds.width != width || bounds.height != height) {
            bounds.width = width;
            bounds.height = height;
            video.setLayoutParams(bounds);
        }
    }
    @Override public void onEvents(Player player, Player.Events events) {
        if (events.contains(Player.EVENT_TIMELINE_CHANGED) && page.equals("Queue")) populate();
        updateInlineQueue();updatePlayback();
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        playback.player.addListener(this);
        updateInlineQueue();updatePlayback();
        post(tick);
    }
    @Override protected void onDetachedFromWindow() {
        attached = false;
        removeCallbacks(tick);
        if (!playback.released) {
            playback.player.removeListener(this);
            if (playback.clearSurface(video)) playback.player.pause();
        }
        super.onDetachedFromWindow();
    }
    private void visualizationMenu(View anchor){
        DesktopContextMenu menu=new DesktopContextMenu(getContext(),"Music visualizations");
        for(int i=0;i<5;i++){int selected=i;String name=i<4?XpMusicVisualizer.NAMES[i]:"Album Art";menu.item((i==visualMode?"✓ ":"")+name,"mediaplayer",()->visualization(selected));}
        ContextMenus.centered(anchor,(x,y)->ContextMenus.show(menu,anchor,x,y));
    }
    void visualization(int mode){
        visualMode=Math.max(0,Math.min(4,mode));visualize=visualMode<4;waveform.mode(Math.min(3,visualMode));getContext().getSharedPreferences("xp_media",0).edit().putInt("visualization",visualMode).apply();updatePlayback();
    }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);queueSide.setVisibility(!center&&w>=dp(620)?VISIBLE:GONE);}
    private void updateInlineQueue(){
        List<MediaLibrary.Entry> items=playback.queue();
        inlineQueue.setAdapter(new BaseAdapter(){public int getCount(){return items.size();}public Object getItem(int p){return items.get(p);}public long getItemId(int p){return p;}
            public View getView(int p,View old,ViewGroup parent){TextView row=label(items.get(p).name,11);row.setTextColor(p==playback.player.getCurrentMediaItemIndex()?Color.WHITE:0xff183260);row.setBackgroundColor(p==playback.player.getCurrentMediaItemIndex()?0xff316ac5:0xfff4f6fd);row.setSingleLine();row.setEllipsize(TextUtils.TruncateAt.END);row.setPadding(dp(7),0,dp(5),0);row.setMinHeight(dp(32));return row;}});
    }
}
