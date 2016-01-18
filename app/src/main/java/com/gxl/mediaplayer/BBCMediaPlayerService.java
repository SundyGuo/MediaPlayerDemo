package com.gxl.mediaplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by gxl on 2016/1/15.
 * Description: Create Service to play Media avoid ANR
 */
public class BBCMediaPlayerService extends Service{

    private static final String TAG = BBCMediaPlayerService.class.getSimpleName();

    // MediaPlayer to play online video
    protected MediaPlayer mMediaPlayer = null;

    // Local IBinder
    private final IBinder mBinder = new LocalBinder();

    /**
     * Create Local Binder
     */
    public class LocalBinder extends Binder {
        public BBCMediaPlayerService getService() {
            return BBCMediaPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * Start Play
     */
    public void startPlayMedia(String mediaPath){
        if(mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        Uri uri = Uri.parse(mediaPath);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(this,uri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG,"play media failed",e);
        }
    }

    /**
     * Set window to show media player content
     * @param surfaceHolder
     */
    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        if(mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setDisplay(surfaceHolder);
    }

    /**
     * Release media player source
     */
    public void releaseResource() {
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * get service media player
     * @return
     */
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    /**
     * get media player play position
     * @return
     */
    public int getCurrentPosition(){
        if(mMediaPlayer != null) {
            return  mMediaPlayer.getCurrentPosition();
        } else {
            return -1;
        }
    }

    /**
     * get media player duration
     * @return
     */
    public int getDuration() {
        if(mMediaPlayer != null) {
            return  mMediaPlayer.getDuration();
        } else {
            return -1;
        }
    }

    // 添加MediaPlayer加载数据及播放视频进度监听

    /**
     * Add some media player listener
     * @param onPreparedListener
     * @param onBufferingUpdateListener
     * @param onInfoListener
     * @param onErrorListener
     */
    public void addMediaPlayerListener(MediaPlayer.OnPreparedListener onPreparedListener, MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener,
                                       MediaPlayer.OnInfoListener onInfoListener, MediaPlayer.OnErrorListener onErrorListener) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        if (onPreparedListener != null) {
            mMediaPlayer.setOnPreparedListener(onPreparedListener);

        }
        if (onBufferingUpdateListener != null) {
            mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        }
        if (onInfoListener != null) {
            mMediaPlayer.setOnInfoListener(onInfoListener);
        }
        if(onErrorListener != null) {
            mMediaPlayer.setOnErrorListener(onErrorListener);
        }
    }
}
