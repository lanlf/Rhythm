package com.lan.rhythm.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.animation.AnimationUtils;

import com.lan.rhythm.R;
import com.lan.rhythm.atys.PlayActivity;
import com.lan.rhythm.model.Lrc;
import com.lan.rhythm.model.Music;
import com.lan.rhythm.util.AppConstant;
import com.lan.rhythm.util.LrcProcess;
import com.lan.rhythm.util.MusicUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lan on 2016/7/19.
 */
public class PlayerService extends Service {
    private MediaPlayer mediaPlayer = new MediaPlayer();       //媒体播放器对象
    private String path;                        //音乐文件路径
    private boolean isPause;                    //暂停状态
    private boolean isChange = false;                   //是否在手动更改进度
    private int current = 0;

    private LrcProcess mLrcProcess; //歌词处理
    private List<Lrc> lrcList = new ArrayList<Lrc>(); //存放歌词列表对象
    private int index = 0;          //歌词检索值

    //播放模式
    public static final int MODE_ORDER = 0;
    public static final int MODE_ONE = 1;
    public static final int MODE_CYCLE = 2;
    public static final int MODE_RANDOM = 3;
    private int tag_mode = 0;

    //服务要发送的一些Action
    public static final String UPDATE_ACTION = "com.lan.action.UPDATE_ACTION";  //更新动作
    public static final String CTL_ACTION = "com.lan.action.CTL_ACTION";        //选择模式
    public static final String MUSIC_CURRENT = "com.lan.action.MUSIC_CURRENT";  //当前音乐播放时间更新动作
    public static final String MUSIC_DURATION = "com.lan.action.MUSIC_DURATION";//新音乐长度更新动作
    public static final String CAHNGE_ACTION = "com.lan.action.CHANGE_ACTION";  //手动动作
    public static final String UPLRC_ACTION = "com.lan.action.UPLRC_ACTION";   //更新歌词
    private MyReceiver mbr;
    private List<Music> musicList;
    private int currentTime;
    private int duration;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       /* if (mediaPlayer.isPlaying()) {
            stop();
        }*/
        path = intent.getStringExtra("url");
        current = intent.getIntExtra("listPosition", 0);
        System.out.println("position------" + current + "*******" + path);
        int msg = intent.getIntExtra("MSG", 0);
        if (msg == AppConstant.PlayerMsg.PLAY_MSG) {
            System.out.println(mediaPlayer.getCurrentPosition());
            play(mediaPlayer.getCurrentPosition());
        } else if (msg == AppConstant.PlayerMsg.PAUSE_MSG) {
            System.out.println("--------");
            //mediaPlayer.pause();
            pause();
        } else if (msg == AppConstant.PlayerMsg.STOP_MSG) {
            stop();
            System.out.println("sopppppppp");
        } else if (msg == AppConstant.PlayerMsg.REPLAY_MSG) {
            replay();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mbr = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_ACTION);
        intentFilter.addAction(CTL_ACTION);
        intentFilter.addAction(MUSIC_CURRENT);
        intentFilter.addAction(MUSIC_DURATION);
        intentFilter.addAction(CAHNGE_ACTION);
        registerReceiver(mbr, intentFilter);
        musicList = MusicUtil.getMusicInfo(getApplicationContext());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        tag_mode = sp.getInt("mode", MODE_ORDER);
        /*mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                *//*switch (tag_mode) {
                    case MODE_ORDER:
                        current = current + 1;
                        if (current < musicList.size()) {
                            path = musicList.get(current).getUrl();
                            play(0);
                        } else {
                            current = current - 1;
                        }
                        System.out.println("顺序："+ current +"-----" +musicList.get(current).getTitle());
                        break;
                    case MODE_ONE:
                        play(0);
                        System.out.println("单曲："+current +"-----" +musicList.get(current).getTitle());
                        break;
                    case MODE_CYCLE:
                        current = current + 1;
                        if (current < musicList.size()) {
                            path = musicList.get(current).getUrl();
                            play(0);
                        } else {
                            current = 0;
                            path = musicList.get(current).getUrl();
                            play(0);
                        }
                        System.out.println("循环："+ current +"-----" +musicList.get(current).getTitle());
                        break;
                    case MODE_RANDOM:
                        current = new Random().nextInt(musicList.size()-1);
                        path = musicList.get(current).getUrl();
                        play(0);
                        System.out.println("随机："+ current +"-----" +musicList.get(current).getTitle());
                        break;
                }*//*
            }
        });*/
    }

    /**
     * 重新播放音乐
     */
    private void replay() {
        play(0);
    }


    /**
     * 播放音乐
     *
     * @param position
     */
    private void play(int position) {

        try {
            isChange = false;
            isPause = false;
            mediaPlayer.reset();//把各项参数恢复到初始状态
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();  //进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(position));//注册一个监听器
            //更新seekbar
            updatePb();
            initLrc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停音乐
     */
    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    /**
     * 停止音乐
     */
    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新seekBar
     */
    private void updatePb() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null && !isChange) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    Intent intent = new Intent(MUSIC_CURRENT);
                    intent.putExtra("current", currentPosition);
                    sendBroadcast(intent);
                    SystemClock.sleep(100);
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (mbr != null) {
            unregisterReceiver(mbr);
        }
    }

    /*
    * 初始化歌词配置
    */
    public void initLrc() {
        // mLrcProcess = new LrcProcess();
        //读取歌词文件
        // mLrcProcess.readLRC(musicList.get(current).getUrl());
        //传回处理后的歌词文件
        // lrcList = mLrcProcess.getLrcList();

        /*//切换带动画显示歌词
        PlayerActivity.lrcView.setAnimation(AnimationUtils.loadAnimation(PlayerService.this,R.anim.alpha_z));*/
        //handler.post(mRunnable);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null) {
                    Intent intent = new Intent(UPLRC_ACTION);
                    intent.putExtra("path", musicList.get(current).getUrl());
                    intent.putExtra("index", lrcIndex());
                    sendBroadcast(intent);
                    SystemClock.sleep(100);
                }
            }
        }).start();
    }

    /**
     * 根据时间获取歌词显示的索引值
     *
     * @return
     */
    public int lrcIndex() {
        if (mediaPlayer.isPlaying()) {
            currentTime = mediaPlayer.getCurrentPosition();
            duration = mediaPlayer.getDuration();
        }
        if (currentTime < duration) {
            for (int i = 0; i < lrcList.size(); i++) {
                if (i < lrcList.size() - 1) {
                    if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
                        index = i;
                    }
                    if (currentTime > lrcList.get(i).getLrcTime()
                            && currentTime < lrcList.get(i + 1).getLrcTime()) {
                        index = i;
                    }
                }
                if (i == lrcList.size() - 1
                        && currentTime > lrcList.get(i).getLrcTime()) {
                    index = i;
                }
            }
        }
        return index;
    }

    /**
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     */
    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int positon;

        public PreparedListener(int positon) {
            this.positon = positon;
        }


        /**
         * 在准备期间拿到歌曲的长度，传给activity
         *
         * @param mp
         */
        @Override
        public void onPrepared(MediaPlayer mp) {
            Intent intent = new Intent(MUSIC_DURATION);
            intent.putExtra("max", mediaPlayer.getDuration());
            sendBroadcast(intent);
            mediaPlayer.start();
            if (positon > 0) {    //如果音乐不是从头播放
                mediaPlayer.seekTo(positon);
            }
        }
    }


    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UPDATE_ACTION:
                    int progress = intent.getIntExtra("progress", 0);
                    play(progress);
                    break;
                case CAHNGE_ACTION:
                    isChange = true;
                    break;
                case CTL_ACTION:
                    tag_mode = intent.getIntExtra("mode", 0);
                    break;
            }
        }
    }
}
