package com.nthexperience.desktop;

import android.content.Context;
import android.view.TextureView;
import androidx.media3.common.*;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.audio.DefaultAudioSink;
import androidx.media3.exoplayer.audio.TeeAudioProcessor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@UnstableApi
final class MediaPlayback {
    final ExoPlayer player;
    final Signal signal = new Signal();
    private List<MediaLibrary.Entry> queue = new ArrayList<>();
    private TextureView surface;
    boolean released;

    MediaPlayback(Context context) {
        DefaultRenderersFactory renderers = new DefaultRenderersFactory(context) {
            @Override protected AudioSink buildAudioSink(Context context, boolean floatOutput, boolean playbackParams) {
                return new DefaultAudioSink.Builder(context)
                        .setAudioProcessors(new AudioProcessor[]{new TeeAudioProcessor(signal)}).build();
            }
        };
        player = new ExoPlayer.Builder(context, renderers)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .setHandleAudioBecomingNoisy(true)
                .build();
    }
    void play(List<MediaLibrary.Entry> entries, int index) {
        if (index < 0 || index >= entries.size()) return;
        List<MediaItem> items = new ArrayList<>(entries.size());
        for (MediaLibrary.Entry item : entries) {
            items.add(new MediaItem.Builder().setUri(item.uri).setMediaId(item.uri.toString())
                    .setMimeType(item.mime)
                    .setMediaMetadata(new MediaMetadata.Builder().setTitle(item.name).build()).build());
        }
        queue = new ArrayList<>(entries);
        player.setMediaItems(items, index, 0);
        player.prepare();
        player.play();
    }
    List<MediaLibrary.Entry> queue() { return new ArrayList<>(queue); }
    MediaLibrary.Entry current() {
        int index = player.getCurrentMediaItemIndex();
        return index >= 0 && index < queue.size() ? queue.get(index) : null;
    }
    void playPause() {
        if (current() == null) return;
        if (player.getPlayWhenReady() && player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlayerError() == null) {
            player.pause();
            return;
        }
        if (player.getPlayerError() != null) player.prepare();
        if (player.getPlaybackState() == Player.STATE_ENDED) player.seekToDefaultPosition();
        player.play();
    }
    void select(int index) {
        if (index < 0 || index >= player.getMediaItemCount()) return;
        player.seekToDefaultPosition(index);
        if (player.getPlaybackState() == Player.STATE_IDLE) player.prepare();
        player.play();
    }
    void surface(TextureView next) {
        if (surface == next) return;
        if (next != null) player.setVideoTextureView(next);
        else if (surface != null) player.clearVideoTextureView(surface);
        surface = next;
    }
    boolean clearSurface(TextureView view) {
        if (surface != view) return false;
        surface(null);
        return true;
    }
    void release() { released = true; player.release(); }

    /** PCM envelope from the player itself. The microphone and device audio are never used. */
    static final class Signal implements TeeAudioProcessor.AudioBufferSink {
        private static final int SAMPLES = 256;
        private static final float[] WINDOW=new float[SAMPLES];
        private static final float[][] COS=new float[24][SAMPLES],SIN=new float[24][SAMPLES];
        static {
            for(int i=0;i<SAMPLES;i++)WINDOW[i]=(float)(.5-.5*Math.cos(2*Math.PI*i/(SAMPLES-1)));
            for(int band=0;band<24;band++){
                int bin=Math.max(1,Math.round((float)Math.pow(SAMPLES*.42,band/23f)));
                for(int i=0;i<SAMPLES;i++){double angle=2*Math.PI*bin*i/SAMPLES;COS[band][i]=(float)Math.cos(angle);SIN[band][i]=(float)Math.sin(angle);}
            }
        }
        private float[] analyzed;
        private float[] bands=new float[24];
        private volatile float[] envelope = new float[SAMPLES];
        private int stride = 2, encoding = C.ENCODING_PCM_16BIT;
        @Override public void flush(int sampleRate, int channels, int encoding) {
            this.encoding = encoding;
            stride = Math.max(1, channels) * 2;
            envelope = new float[SAMPLES];
        }
        @Override public void handleBuffer(ByteBuffer buffer) {
            if (encoding != C.ENCODING_PCM_16BIT) return;
            int frames = buffer.remaining() / stride;
            if (frames == 0) return;
            ByteBuffer pcm = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN);
            float[] next = new float[SAMPLES];
            for (int i = 0; i < next.length; i++) {
                int at = pcm.position() + Math.min(frames-1,Math.max(0,frames-next.length)+i) * stride;
                float sum=0;for(int channel=0;channel<stride/2;channel++)sum+=pcm.getShort(at+channel*2)/32768f;
                next[i] = sum/(stride/2);
            }
            envelope = next;
        }
        float[] samples() { return envelope; }
        // Log-spaced DFT bins over one PCM snapshot. Rendering never mutates the audio buffer.
        float[] spectrum(){
            float[] values=envelope;if(values==analyzed)return bands;
            float[] next=new float[24];int n=values.length;
            for(int band=0;band<next.length;band++){
                double re=0,im=0;
                for(int i=0;i<n;i++){float value=values[i]*WINDOW[i];re+=value*COS[band][i];im-=value*SIN[band][i];}
                next[band]=(float)(Math.hypot(re,im)*4/n);
            }
            analyzed=values;bands=next;return next;
        }
    }
}
