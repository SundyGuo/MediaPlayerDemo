package com.gxl.mediaplayer;

import android.media.MediaPlayer;

/**
 * Created by gxl on 2016/1/19.
 * Description: override MediaPlayer
 */
public class SMediaPlayer extends MediaPlayer{

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
}
