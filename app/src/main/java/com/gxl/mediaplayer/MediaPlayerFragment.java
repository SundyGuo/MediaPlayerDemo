package com.gxl.mediaplayer;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gxl on 2016/1/15.
 * Description: Fragment used to show media player
 */
public class MediaPlayerFragment extends BaseFragment implements ServiceConnection {

    private static final String TAG = MediaPlayerFragment.class.getSimpleName();
    private static final String URL = "http://assets.baicizhan.com/word_tv/";
    private static final String DEFAULT_URL = "http://assets.baicizhan.com/word_tv/leng_play.mp4";

    // Main View
    private View mView;

    /**
     * Add mark to judge whether has voice or video
     * 0: Default
     * 1: Voice
     * 2: Video
     */
    private int mVoiceAndVideoState = 2;

    private BBCMediaPlayerService mBBCMediaPlayerService;

    // Mark Retry state
    private boolean mTryAgain = false;

    // Play Time Thread control state
    private boolean mUpdatePlayTimeState = true;

    // State to mark play state
    // Mark preparing state
    private boolean mIsPreparing = false;
    private boolean mSeekBarTouching = false;
    // Mark prepared state
    private boolean mIsPrepared = false;
    // onPaused State
    private boolean mIsPausedMediaState = false;
    // Focused state
    private boolean mIsFocused = false;
    private boolean mIsDestroyed = false;
    // Playing State
    private boolean mIsPlaying = false;

    // Input window
    private EditText mInputEditText;
    // Search Button
    private Button mSearchWord;
    // SeekBar and time show
    private SeekBar mSeekBar;
    private TextView mVideoTime;
    private View mVideoControlView;

    private static final int DURATION = 300;
    private static final int SLEEP_TIME = 300;
    private static final int DELAY_TIME = 3000;

    // Animator to show and hide video control view
    private ObjectAnimator mVideoAnimator = null;


    // View Button
    private Button mBBC1;
    private Button mBBC2;
    private Button mPlayPauseButton;

    // Video Play part
    private RelativeLayout mSurfaceViewLayout;
    private KSurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    //UI
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.media_player_fragment, container, false);
        init();
        if (mVoiceAndVideoState == 1 || mVoiceAndVideoState == 2) {
            startAndBindMediaService();
        }
        return mView;
    }

    /**
     * Start Service and play media
     */
    private void startAndBindMediaService() {
        BBCMediaPlayerService.mIsBusy = true;
        mContext.startService(new Intent(mContext, BBCMediaPlayerService.class));
        mContext.bindService(new Intent(mContext, BBCMediaPlayerService.class), this, Context.BIND_AUTO_CREATE);
    }

    /**
     * Init
     */
    private void init() {

        // View Click
        mBBC1 = (Button) findViewById(R.id.bbc1);
        mBBC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewBBCClick(v);
            }
        });
        mBBC2 = (Button) findViewById(R.id.bbc2);
        mBBC2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewBBCClick(v);
            }
        });
        mPlayPauseButton = (Button) findViewById(R.id.play_pause);
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewPauseClick(v);
            }
        });

        // UI
        mInputEditText = (EditText) findViewById(R.id.input_word);
        mSearchWord = (Button) findViewById(R.id.search);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSurfaceViewLayout = (RelativeLayout) findViewById(R.id.surface_view_layout);
        mSurfaceView = (KSurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mProgressBar.getVisibility() == View.VISIBLE) {
                    return;
                }
                if(mPlayPauseButton.getVisibility() == View.VISIBLE && !mIsPlaying) {
                    return;
                }
                if(mBBCMediaPlayerService != null && mBBCMediaPlayerService.isPlaying()) {
                    setPlayPauseButton(true);
                }else {
                    setPlayPauseButton(false);
                }
                mHandler.removeCallbacks(runnable);
                if(mVideoControlView.getTranslationY() == 0) {
                    changeVideoControlState(false);
                    mPlayPauseButton.setVisibility(View.GONE);
                } else{
                    changeVideoControlState(true);
                    mPlayPauseButton.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(runnable,DELAY_TIME);
                }
            }
        });
        mSearchWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTryAgain = false;
                playMedia(URL + "leng_" + mInputEditText.getText().toString().trim() + ".mp4");
            }
        });
        // 设置surfaceHolder
        mSurfaceHolder = mSurfaceView.getHolder();
        // 设置Holder类型,该类型表示surfaceView自己不管理缓存区,虽然提示过时，但最好还是要设置
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置surface回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if(mVoiceAndVideoState == 2) {
                    if (mBBCMediaPlayerService != null) {
                        mBBCMediaPlayerService.setSurfaceHolder(surfaceHolder);
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // surfaceView销毁,同时销毁mediaPlayer
            }
        });

        mVideoControlView = findViewById(R.id.video_control_layout);
        mVideoControlView.setVisibility(View.INVISIBLE);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (mBBCMediaPlayerService != null) {
                    if (mBBCMediaPlayerService.getMediaPlayer() != null) {
                        mVideoTime.setText(getTime(progress) + "/" + getTime(mBBCMediaPlayerService.getDuration()));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(runnable);
                mSeekBarTouching = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = seekBar.getProgress();
                mHandler.postDelayed(runnable, DELAY_TIME);
                if (mBBCMediaPlayerService != null) {
                    if (mBBCMediaPlayerService.getMediaPlayer() != null) {
                        mVideoTime.setText(getTime(i) + "/" + getTime(mBBCMediaPlayerService.getDuration()));
                        mBBCMediaPlayerService.getMediaPlayer().seekTo(i);
                        mIsPreparing = true;
                        mSeekBarTouching = false;
                        changeProgressBarState(true);
                        changeVideoControlState(false);
                        mPlayPauseButton.setVisibility(View.GONE);
                    }
                }
            }
        });

        mVideoTime = (TextView) findViewById(R.id.video_time);
        UpdatePlayTimeThread.start();
    }

    /**
     * View BBC click
     *
     * @param view
     */
    private void onViewBBCClick(View view) {
        mTryAgain = false;
        String text = ((Button) view).getText().toString().trim();
        if (text.endsWith("1")) {
            playMedia("http://cdn.iciba.com/news/bbc/video/bbc_lingohack_solar_power_female_commander_hospital_demolished-android.mp4");
        } else if (text.endsWith("2")) {
            playMedia("http://cdn.iciba.com/news/bbc/video/bbc_lingohack_syria_iraq_facebook-android.mp4");
        }
    }

    /**
     * View Play Pause click
     *
     * @param view
     */
    private void onViewPauseClick(View view) {
        if(mIsPrepared) {
            if(mIsPlaying) {
                mBBCMediaPlayerService.getMediaPlayer().pauseMediaPlayer();
                setPlayPauseButton(false);
                mHandler.removeCallbacks(runnable);
                changeVideoControlState(false);
            } else {
                mBBCMediaPlayerService.getMediaPlayer().start();
                mIsPausedMediaState = false;
                setPlayPauseButton(true);
                mHandler.removeCallbacks(runnable);
                mHandler.postDelayed(runnable, DELAY_TIME);
            }
        } else {
            if(mIsPreparing) {
                mPlayPauseButton.setVisibility(View.GONE);
                return;
            } else if (mIsPrepared) {
                mPlayPauseButton.setVisibility(View.GONE);
                mBBCMediaPlayerService.getMediaPlayer().start();
                mIsPausedMediaState = false;
            } else {
                mPlayPauseButton.setVisibility(View.GONE);
                playMedia(DEFAULT_URL);
            }
        }
    }

    /**
     * Play video or voice
     *
     * @wordPath
     */
    private void playMedia(final String wordPath) {
        if (mBBCMediaPlayerService != null) {
            if(mIsPreparing) {
                mBBCMediaPlayerService.createNewMediaPlayer();
            }
            mIsPrepared = false;
            mIsPreparing = true;
            changeProgressBarState(true);
            mSeekBar.setProgress(0);
            mSeekBar.setSecondaryProgress(0);
            mVideoTime.setText(getTime(0) + "/" + getTime(0));
            mBBCMediaPlayerService.addMediaPlayerListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if(mIsDestroyed) {
                        releaseMediaPlayer(mediaPlayer);
                        return;
                    }
                    mIsPrepared = true;
                    mIsPreparing = false;
                    mVideoControlView.setVisibility(View.VISIBLE);
                    mVideoControlView.setTranslationY(mVideoControlView.getHeight());
                    changeProgressBarState(false);
                    mediaPlayer.start();
                    // 设置显示到屏幕
                    mediaPlayer.setDisplay(mSurfaceHolder);
                    mSeekBar.setMax(mediaPlayer.getDuration());
                    mVideoTime.setText(getTime(0) + "/" + getTime(mediaPlayer.getDuration()));
                    ViewGroup.LayoutParams layoutParams = mSurfaceViewLayout.getLayoutParams();
                    if (mediaPlayer.getVideoHeight() == 0) {
                        layoutParams.width = mSurfaceViewLayout.getWidth();
                        layoutParams.height = mSurfaceViewLayout.getHeight();
                    } else if (mediaPlayer.getVideoHeight() * mSurfaceViewLayout.getWidth() > mediaPlayer.getVideoWidth() * mSurfaceViewLayout.getHeight()) {
                        layoutParams.height = mSurfaceViewLayout.getHeight();
                        layoutParams.width = (int) ((long) layoutParams.height * mediaPlayer.getVideoWidth() / mediaPlayer.getVideoHeight());
                    } else {
                        layoutParams.width = mSurfaceViewLayout.getWidth();
                        layoutParams.height = (int) ((long) layoutParams.width * mediaPlayer.getVideoHeight() / mediaPlayer.getVideoWidth());
                    }
                    mSurfaceViewLayout.setLayoutParams(layoutParams);
                    if (mIsPausedMediaState) {
                        ((SMediaPlayer) mediaPlayer).pauseMediaPlayer();
                        return;
                    }
                }
            }, new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    if (mSeekBar.getMax() > 0) {
                        mSeekBar.setSecondaryProgress(percent * mSeekBar.getMax() / 100);
                    }
                }
            }, new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch(what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            changeProgressBarState(true);
                            break;

                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            if (mIsDestroyed) {
                                releaseMediaPlayer(mp);
                                break;
                            }
                            mIsPreparing = false;
                            if (mIsPausedMediaState) {
                                ((SMediaPlayer) mp).pauseMediaPlayer();
                            }
                            break;

                        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            break;
                    }
                    return false;
                }
            }, new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (!mTryAgain) {
                        playMedia(wordPath.replace("leng_", "real_"));
                        mTryAgain = true;
                    } else {
                        setPlayPauseButton(false);
                        changeProgressBarState(false);
                        mIsPrepared = false;
                        mIsPreparing = false;
                        SToast.show(mContext,"what is " + what + " *** extra is " + extra);
                    }
                    return false;
                }
            });
            mBBCMediaPlayerService.addMediaPlayerPausedListener(new SMediaPlayer.IMediaPlayerInterface() {
                @Override
                public void onPauseMedia() {
                    if(!mIsPreparing) {
                        mBBCMediaPlayerService.getMediaPlayer().pause();
                        changeProgressBarState(false);
                        setPlayPauseButton(false);
                    }
                    mIsPausedMediaState = true;
                }
            });
            mBBCMediaPlayerService.addMediaPlayerCompletionListener(new SMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mIsPreparing = false;
                    ((SMediaPlayer)mp).pauseMediaPlayer();
                }
            });
            mBBCMediaPlayerService.startPlayMedia(wordPath);
        }
    }

    SimpleDateFormat simpleDateFormat;
    Date date;

    /**
     * Change time from int to format string
     *
     * @param time
     * @return
     */
    private String getTime(int time) {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("mm:ss");
        }
        if (date == null) {
            date = new Date(time);
        } else {
            date.setTime(time);
        }
        return simpleDateFormat.format(date);
    }

    /**
     * Override findViewById add default parent view
     *
     * @param id
     * @return
     */
    private View findViewById(int id) {
        if (mView != null) {
            return mView.findViewById(id);
        }
        return null;

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBBCMediaPlayerService = ((BBCMediaPlayerService.LocalBinder) service)
                .getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBBCMediaPlayerService = null;
    }

    @Override
    public void onPause() {
        mIsFocused = false;
        if (mBBCMediaPlayerService != null) {
            if(mBBCMediaPlayerService.getMediaPlayer() != null && mBBCMediaPlayerService.getMediaPlayer().isPlaying()) {
                mBBCMediaPlayerService.getMediaPlayer().pauseMediaPlayer();
            }
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        mIsFocused = true;
        super.onResume();
    }

    @Override
    public void onDestroy() {
        BBCMediaPlayerService.mIsBusy = false;
        if(mBBCMediaPlayerService != null) {
            if(!mIsPreparing) {
                mBBCMediaPlayerService.releaseResource();
            }
        }
        try {
            mContext.unbindService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUpdatePlayTimeState = false;
        mIsDestroyed = true;
        super.onDestroy();
    }

    /**
     * Change Video control view state
     * @param state true: show false: hide
     */
    private void changeVideoControlState(boolean state){
        if (mVideoAnimator != null && mVideoAnimator.isRunning()) {
            mVideoAnimator.cancel();
        }
        if(state) {
            mVideoAnimator = ObjectAnimator.ofFloat(mVideoControlView, "translationY", 0);
            mVideoAnimator.setDuration(DURATION * Math.abs(-(int) mVideoControlView.getTranslationY()) / mVideoControlView.getHeight());
        } else {
            mVideoAnimator = ObjectAnimator.ofFloat(mVideoControlView, "translationY", mVideoControlView.getHeight());
            mVideoAnimator.setDuration(DURATION * Math.abs(mVideoControlView.getHeight() -(int) mVideoControlView.getTranslationY()) / mVideoControlView.getHeight());
        }
        mVideoAnimator.start();
    }

    /**
     * Hide video control view
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            changeVideoControlState(false);
        }
    };

    /**
     * 释放media player资源
     * @param mediaPlayer
     */
    private void releaseMediaPlayer(MediaPlayer mediaPlayer) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Release media player resource failed", e);
        }
        if(mBBCMediaPlayerService != null){
            if(!BBCMediaPlayerService.mIsBusy) {
                try {
                    mBBCMediaPlayerService.stopSelf();
                } catch (Exception e) {
                    Log.e(TAG,"Stop Service failed",e);
                }
            }
        }
    }

    private Thread UpdatePlayTimeThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (mUpdatePlayTimeState) {
                try {
                    if (mIsFocused && mBBCMediaPlayerService != null && mBBCMediaPlayerService.getMediaPlayer() != null && mBBCMediaPlayerService.getMediaPlayer().isPlaying()) {
                        if(mSeekBar.getProgress() < mBBCMediaPlayerService.getCurrentPosition()) {
                            if(mProgressBar.getVisibility() == View.VISIBLE) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeProgressBarState(false);
                                    }
                                });
                            }
                        } else {
                            if(mProgressBar.getVisibility() == View.GONE && mPlayPauseButton.getVisibility() == View.GONE) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeProgressBarState(true);
                                    }
                                });
                            }
                        }
                        if(!mSeekBarTouching) {
                            mSeekBar.setProgress(mBBCMediaPlayerService.getCurrentPosition());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mVideoTime.setText(getTime(mBBCMediaPlayerService.getCurrentPosition()) + "/" + getTime(mBBCMediaPlayerService.getDuration()));
                                }
                            });
                        }
                        try {
                            Thread.sleep(SLEEP_TIME);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Thread Sleep 1000 err", e);
                        }
                    } else {
                        try {
                            Thread.sleep(SLEEP_TIME * 4);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Thread Sleep 2000 err", e);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Update play time", e);
                }

            }
        }
    });

    /**
     * Change ProgessBar state and show or hide control at same time
     *
     * @param state
     */
    private void changeProgressBarState(boolean state) {
        if (state) {
            mProgressBar.setVisibility(View.VISIBLE);
            changeVideoControlState(false);
            mPlayPauseButton.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mIsPreparing = false;
        }
    }

    /**
     * Set Play View
     * @param isPlaying
     */
    private void setPlayPauseButton(boolean isPlaying){
        if(mPlayPauseButton != null) {
            mIsPlaying = isPlaying;
            if (isPlaying) {
                mPlayPauseButton.setText("Pause");
            } else {
                mPlayPauseButton.setText("Play");
                mPlayPauseButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
