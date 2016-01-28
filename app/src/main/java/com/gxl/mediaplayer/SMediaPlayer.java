package com.gxl.mediaplayer;

import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by gxl on 2016/1/19.
 * Description: override MediaPlayer
 */
public class SMediaPlayer extends MediaPlayer{

    private static final String TAG = SMediaPlayer.class.getSimpleName();
    private IMediaPlayerInterface iMediaPlayerInterface;

    public SMediaPlayer() {
        super();
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        super.seekTo(msec);
    }

    @Override
    public void setOnSeekCompleteListener(final OnSeekCompleteListener listener) {
        super.setOnSeekCompleteListener(listener);
    }

    /**
     * MediaPlayer interface listener
     */
    public interface IMediaPlayerInterface {
        void onPauseMedia();
    }

    /**
     * override MediaPlayer play interface
     * @param mediaPlayerInterface
     */
    public void setMediaPlayerInterface (IMediaPlayerInterface mediaPlayerInterface){
        this.iMediaPlayerInterface = mediaPlayerInterface;
    }

    /**
     *  Paused media player
     */
    public void pauseMediaPlayer() {
        if(iMediaPlayerInterface != null) {
            try {
                iMediaPlayerInterface.onPauseMedia();
            } catch (Exception e) {
                Log.e(TAG,"Pause Media Player failed",e);
            }
        } else {
            try {
                if (isPlaying()){
                    pause();
                    stop();
                    release();
                } else {
                    reset();
                    release();
                }

            } catch (Exception e) {
                Log.e(TAG,"Pause Media Player failed",e);
            }
        }
    }
}
