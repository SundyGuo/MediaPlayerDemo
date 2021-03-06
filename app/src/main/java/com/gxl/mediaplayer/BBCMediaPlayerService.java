package com.gxl.mediaplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
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
    protected SMediaPlayer mMediaPlayer = null;

    // Local IBinder
    private final IBinder mBinder = new LocalBinder();

    public static boolean mIsBusy = false;

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
            mMediaPlayer = new SMediaPlayer();
        } else {
            mMediaPlayer.stop();
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
        mMediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK);
    }

    /**
     * Set window to show media player content
     * @param surfaceHolder
     */
    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        if(mMediaPlayer == null) {
            mMediaPlayer = new SMediaPlayer();
        }
        if(surfaceHolder != null) {
            mMediaPlayer.setDisplay(surfaceHolder);
        }
    }

    /**
     * Release media player source
     */
    public void releaseResource() {
        if(mMediaPlayer != null) {
            if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        try {
            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Judge whether media is playing
     * @return
     */
    public boolean isPlaying() {
        if(mMediaPlayer == null) {
            return  false;
        }
        return mMediaPlayer.isPlaying();
    }

    /**
     * get service media player
     * @return
     */
    public SMediaPlayer getMediaPlayer() {
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

    /**
     * Create new media player when play another video
     */
    public void createNewMediaPlayer(){
        if(mMediaPlayer != null) {
            mMediaPlayer = new SMediaPlayer();
        }

    }

    /**
     * Add some media player listener
     * @param onPreparedListener
     * @param onBufferingUpdateListener
     * @param onInfoListener
     * @param onErrorListener
     */
    public void addMediaPlayerListener(SMediaPlayer.OnPreparedListener onPreparedListener, SMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener,
                                       SMediaPlayer.OnInfoListener onInfoListener, SMediaPlayer.OnErrorListener onErrorListener) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new SMediaPlayer();
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

    /**
     * Add media player player interface of KMediaPlayer
     * @param iMediaPlayerInterface
     */
    public void addMediaPlayerPausedListener(SMediaPlayer.IMediaPlayerInterface iMediaPlayerInterface) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new SMediaPlayer();
        }
        if (iMediaPlayerInterface != null) {
            mMediaPlayer.setMediaPlayerInterface(iMediaPlayerInterface);
        }
    }

    /**
     * Add media player Completion listener
     * @param onCompletionListener
     */
    public void addMediaPlayerCompletionListener(SMediaPlayer.OnCompletionListener onCompletionListener) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new SMediaPlayer();
        }
        if (onCompletionListener != null) {
            mMediaPlayer.setOnCompletionListener(onCompletionListener);

        }
    }
}
