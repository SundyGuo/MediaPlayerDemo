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
import android.text.TextUtils;
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
    private static int mPlayPosition = -1;

    // Mark preparing state
    private boolean mIsPreparing = false;
    // Mark prepared state
    private boolean mIsPrepared = false;
    // Paused state
    private boolean mIsPaused = false;
    // Pause when preparing
    private boolean mPreparingPause = false;
    // Destroy state
    private boolean mIsDestroy = false;
    // Mark last video path
    private static String mLastVideoPath = null;

    // Input window
    private EditText mInputEditText;
    // Search Button
    private Button mSearchWord;
    // SeekBar and time show
    private SeekBar mSeekBar;
    private TextView mVideoTime;
    private View mVideoControlView;

    private static final int FLOOT_DURATION = 300;

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
                if(mProgressBar.getVisibility() == View.VISIBLE) {
                    return;
                }
                if(mIsPrepared) {
                    mHandler.removeCallbacks(runnable);
                    if(mVideoControlView.getTranslationY() == 0) {
                        changeVideoControlState(false);
                    } else{
                        changeVideoControlState(true);
                        mHandler.postDelayed(runnable,3000);
                    }
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
                if(mIsPaused) {
                    mIsPaused = false;
                    mBBCMediaPlayerService.getMediaPlayer().start();
                    mPlayPauseButton.setText("暂停");
                }
                mIsDestroy = false;
                if(mPreparingPause && !TextUtils.isEmpty(mLastVideoPath)) {
                    if(!mIsPreparing && mIsPrepared) {
                        playMedia(mLastVideoPath);
                    }
                }
                mPreparingPause = false;
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // surfaceView销毁,同时销毁mediaPlayer
                mIsDestroy = true;
            }
        });

        mVideoControlView = findViewById(R.id.video_control_layout);
        mVideoControlView.setVisibility(View.INVISIBLE);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if(mIsPreparing) {
                        SToast.show(mContext,"Video is loading, please wait...");
                        return;
                    }
                    if(!mIsPrepared) {
                        SToast.show(mContext,"Video is not prepared, please wait...");
                        return;
                    }
                    if (mBBCMediaPlayerService != null) {
                        if (mBBCMediaPlayerService.getMediaPlayer() != null) {
                            mVideoTime.setText(getTime(i) + "/" + getTime(mBBCMediaPlayerService.getDuration()));
                            mBBCMediaPlayerService.getMediaPlayer().start();
                            mBBCMediaPlayerService.getMediaPlayer().seekTo(i);
                            if(i > mSeekBar.getSecondaryProgress()) {
                                mProgressBar.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(runnable);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.postDelayed(runnable,3000);
            }
        });

        mVideoTime = (TextView) findViewById(R.id.video_time);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.e(TAG, "thread update playing time" + " *** current time is " + System.currentTimeMillis() / 1000);
                        if (mBBCMediaPlayerService != null && mBBCMediaPlayerService.getMediaPlayer() != null && mBBCMediaPlayerService.getMediaPlayer().isPlaying()) {
                            mSeekBar.setProgress(mBBCMediaPlayerService.getCurrentPosition());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mVideoTime.setText(getTime(mBBCMediaPlayerService.getCurrentPosition()) + "/" + getTime(mBBCMediaPlayerService.getDuration()));
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Thread Sleep 1000 ", e);
                            }
                        } else {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Thread Sleep 2000 ", e);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Update play time", e);
                    }

                }
            }
        }).start();
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
            playMedia("http://cdn.iciba.com/news/bbc/video/bbc_lingohack_solar_power_female_commander_hospital_demolished.mp4");
        } else if (text.endsWith("2")) {
            playMedia("http://cdn.iciba.com/news/bbc/video/bbc_lingohack_syria_iraq_facebook.mp4");
        }
    }

    /**
     * View Play Pause click
     *
     * @param view
     */
    private void onViewPauseClick(View view) {
        if(mIsPreparing) {
            SToast.show(mContext,"Video is loading, please wait...");
            return;
        }
        if(!mIsPrepared) {
            SToast.show(mContext,"Video is not prepared, please wait...");
            return;
        }
        if (mBBCMediaPlayerService != null) {
            if (mBBCMediaPlayerService.getMediaPlayer() == null) {
                return;
            }
            // 正在播放
            if (mBBCMediaPlayerService.getMediaPlayer().isPlaying()) {
                mPlayPosition = mBBCMediaPlayerService.getCurrentPosition();
                mBBCMediaPlayerService.getMediaPlayer().pause();
                ((Button) view).setText("播放");
            } else {
                if (mPlayPosition >= 0) {
                    mBBCMediaPlayerService.getMediaPlayer().start();
                    ((Button) view).setText("暂停");
                    mPlayPosition = -1;
                }
            }
        }
    }

    /**
     * Play video or voice
     *
     * @wordPath
     */
    private void playMedia(final String wordPath) {
        mLastVideoPath = wordPath;
        if (mBBCMediaPlayerService != null) {
            mIsPrepared = false;
            mIsPreparing = true;
            mProgressBar.setVisibility(View.VISIBLE);
            mPlayPosition = -1;
            // 还原进度条
            mSeekBar.setProgress(0);
            mSeekBar.setSecondaryProgress(0);
            mVideoTime.setText(getTime(0) + "/" + getTime(0));
            mBBCMediaPlayerService.addMediaPlayerListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if(mediaPlayer.getVideoHeight() == 0 || mediaPlayer.getVideoWidth() == 0) {
                        SToast.show(mContext,"Video is not normal");
                        return;
                    }
                    mIsPrepared = true;
                    mIsPreparing = false;
                    if(mIsDestroy) {
                        return;
                    }
                    mVideoControlView.setVisibility(View.VISIBLE);
                    mVideoControlView.setTranslationY(mVideoControlView.getHeight());
                    mProgressBar.setVisibility(View.GONE);
                    mediaPlayer.start();
                    // 设置显示到屏幕
                    mediaPlayer.setDisplay(mSurfaceHolder);
                    mSeekBar.setMax(mediaPlayer.getDuration());
                    mVideoTime.setText(getTime(0) + "/" + getTime(mediaPlayer.getDuration()));
                    ViewGroup.LayoutParams layoutParams = mSurfaceViewLayout.getLayoutParams();
                    layoutParams.width = mSurfaceViewLayout.getWidth();
                    layoutParams.height = layoutParams.width * mediaPlayer.getVideoHeight() / mediaPlayer.getVideoWidth();
                    mSurfaceViewLayout.setLayoutParams(layoutParams);
                }
            }, new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    Log.e(TAG, "onBuffering i is " + percent + " max is " + mSeekBar.getMax());
                    if (mSeekBar.getMax() > 0) {
                        mSeekBar.setSecondaryProgress(percent * mSeekBar.getMax() / 100);
                    }
                }
            }, new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch(what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            mProgressBar.setVisibility(View.VISIBLE);
                            break;

                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            mProgressBar.setVisibility(View.GONE);
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
                    }
                    return false;
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
        if (mBBCMediaPlayerService != null) {
            if (mBBCMediaPlayerService.getMediaPlayer() != null) {
                if(mIsPreparing && !mIsPrepared) {
                    mPreparingPause = true;
                } else if(mBBCMediaPlayerService.getMediaPlayer().isPlaying()) {
                    mPlayPosition = mBBCMediaPlayerService.getCurrentPosition();
                    mBBCMediaPlayerService.getMediaPlayer().pause();
                    mPlayPauseButton.setText("播放");
                    mIsPaused = true;
                }
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(mBBCMediaPlayerService != null) {
            mBBCMediaPlayerService.releaseResource();
        }
        mContext.unbindService(this);
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
            mVideoAnimator.setDuration(FLOOT_DURATION * Math.abs(-(int) mVideoControlView.getTranslationY()) / mVideoControlView.getHeight());
        } else {
            mVideoAnimator = ObjectAnimator.ofFloat(mVideoControlView, "translationY", mVideoControlView.getHeight());
            mVideoAnimator.setDuration(FLOOT_DURATION * Math.abs(mVideoControlView.getHeight() -(int) mVideoControlView.getTranslationY()) / mVideoControlView.getHeight());
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
}
