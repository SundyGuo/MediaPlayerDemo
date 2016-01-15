package com.gxl.mediaplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gxl on 2016/1/15.
 * Description: Fragment used to show media palyer
 */
public class MediaPlayerFragment extends Fragment {

    private static final String TAG = MediaPlayerFragment.class.getSimpleName();
    private static final String URL = "http://assets.baicizhan.com/word_tv/";

    // Main View
    private View mView;

    // 记录重试状态值
    private boolean mTryAgain = false;
    private int mPlayPosition = -1;

    // 输入框
    private EditText mInputEditText;
    // 查询按钮
    private Button mSearchWord;
    // 进度条
    private SeekBar mSeekBar;
    private TextView mVideoTime;

    // 视频播放部分
    private RelativeLayout mSurfaceViewLayout;
    private KSurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mMediaPlayer;

    //UI
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.media_player_fragment, container, false);
        init();
        return  mView;
    }

    /**
     * 初始化
     */
    private void init() {

        findViewById(R.id.bbc1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewBBCClick(view);
            }
        });

        findViewById(R.id.bbc2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewBBCClick(view);
            }
        });

        findViewById(R.id.play_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewPauseClick(view);
            }
        });
        // UI部分初始化
        mInputEditText = (EditText) findViewById(R.id.input_word);
        mSearchWord = (Button) findViewById(R.id.search);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSurfaceViewLayout = (RelativeLayout) findViewById(R.id.surface_view_layout);
        mSurfaceView = (KSurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewPauseClick(findViewById(R.id.play_pause));
            }
        });
        mSearchWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTryAgain = false;
                playMedia(URL + "leng_" + mInputEditText.getText().toString().trim() + ".mp4");
            }
        });

        // 视频播放部分初始化
        mMediaPlayer = new MediaPlayer();
        // 设置surfaceHolder
        mSurfaceHolder = mSurfaceView.getHolder();
        // 设置Holder类型,该类型表示surfaceView自己不管理缓存区,虽然提示过时，但最好还是要设置
//        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置surface回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // surfaceView销毁,同时销毁mediaPlayer
                if (null != mMediaPlayer) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        });

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    if(mMediaPlayer != null) {
                        mVideoTime.setText(getTime(i) + "/" + getTime(mMediaPlayer.getDuration()));
                        mMediaPlayer.start();
                        mMediaPlayer.seekTo(i);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mVideoTime = (TextView) findViewById(R.id.video_time);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Log.e(TAG,"thread update playing time" + " *** current time is " + System.currentTimeMillis()/1000);
                        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                            mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mVideoTime.setText(getTime(mMediaPlayer.getCurrentPosition()) + "/" + getTime(mMediaPlayer.getDuration()));
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Log.e(TAG,"Thread Sleep 1000 ",e);
                            }
                        } else {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                Log.e(TAG,"Thread Sleep 2000 ",e);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG,"Update play time",e);
                    }

                }
            }
        }).start();
    }

    /**
     * View BBC click
     * @param view
     */
    private void onViewBBCClick(View view){
        mTryAgain = false;
        String text = ((Button)view).getText().toString().trim();
        if(text.endsWith("1")) {
            playMedia("http://cdn.iciba.com/news/bbc/video/bbc_lingohack_solar_power_female_commander_hospital_demolished.mp4");
        } else if(text.endsWith("2")) {
            playMedia("http://cdn.iciba.com/news/bbc/video/bbc_lingohack_syria_iraq_facebook.mp4");
        }
    }

    /**
     * View Play Pause click
     * @param view
     */
    private void onViewPauseClick(View view){
        if(mMediaPlayer == null) {
            return;
        }
        // 正在播放
        if (mMediaPlayer.isPlaying()) {
            mPlayPosition = mMediaPlayer.getCurrentPosition();
            // seekBarAutoFlag = false;
            mMediaPlayer.pause();
            ((Button)view).setText("播放");
        } else {
            if (mPlayPosition>= 0) {
                // seekBarAutoFlag = true;
//                mMediaPlayer.seekTo(mPlayPosition);
                mMediaPlayer.start();
                ((Button)view).setText("暂停");
                mPlayPosition = -1;
            }
        }
    }

    /**
     * 播放音频
     * @wordPath
     */
    private void playMedia(final String wordPath) {
        mProgressBar.setVisibility(View.VISIBLE);
        mPlayPosition = -1;
        if(mMediaPlayer == null) {
            // 初始化MediaPlayer
            mMediaPlayer = new MediaPlayer();
        } else {
            if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        }
        // 还原进度条
        mSeekBar.setProgress(0);
        mSeekBar.setSecondaryProgress(0);
        mVideoTime.setText(getTime(0) + "/" + getTime(0));
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mProgressBar.setVisibility(View.GONE);
                mMediaPlayer.start();
                // 设置显示到屏幕
                mMediaPlayer.setDisplay(mSurfaceHolder);
                mSeekBar.setMax(mMediaPlayer.getDuration());
                mVideoTime.setText(getTime(0) + "/" + getTime(mMediaPlayer.getDuration()));
                int screenWidth = mSurfaceViewLayout.getWidth();
                int screenHeight = screenWidth * mMediaPlayer.getVideoHeight()/mMediaPlayer.getVideoWidth();
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth,
                        screenHeight);
                layoutParams.topMargin = 480;
                mSurfaceViewLayout.setLayoutParams(layoutParams);
            }
        });
        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                Log.e(TAG,"onBuffering i is " + i + " max is " + mSeekBar.getMax());
                if(mSeekBar.getMax()>0) {
                    mSeekBar.setSecondaryProgress(i*mSeekBar.getMax()/100);
                }
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.e(TAG,"Video play complete");
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                if(!mTryAgain) {
                    playMedia(wordPath.replace("leng_","real_"));
                    mTryAgain = true;
                }
                return false;
            }
        });
        mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int what, int i1) {
                if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    mProgressBar.setVisibility(View.GONE);
                }
                return false;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse(wordPath);
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setDataSource(getActivity(),uri);
                    mMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    SimpleDateFormat simpleDateFormat;
    Date date;
    /**
     * 通过 int 值获取对应时间值
     * @param time
     * @return
     */
    private String getTime(int time){
        if(simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("mm:ss");
        }
        if(date == null){
            date = new Date(time);
        } else {
            date.setTime(time);
        }
        return simpleDateFormat.format(date);
    }

    /**
     * Override findViewById
     * @param id
     * @return
     */
    private View findViewById(int id){
        if(mView != null) {
            return mView.findViewById(id);
        }
        return null;

    }
}
