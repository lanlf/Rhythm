package com.lan.rhythm.atys;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.lan.rhythm.R;
import com.lan.rhythm.model.Lrc;
import com.lan.rhythm.service.PlayerService;
import com.lan.rhythm.util.LrcProcess;
import com.lan.rhythm.view.LrcView;

import java.util.List;

/**
 * Created by lan on 2016/7/21.
 */
public class PlayActivity extends Activity{

    LrcView lrcView;
    MyReceiver mr;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        this.setContentView(R.layout.activity_play);
        lrcView = (LrcView) findViewById(R.id.lrcShowView);
        mr = new MyReceiver();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.UPLRC_ACTION);
        registerReceiver(mr, intentFilter);
        System.out.println("start");
    }

    /**
     * 广播接收器
     * @author len
     *
     */
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case PlayerService.UPLRC_ACTION:
                    String path = intent.getStringExtra("path");
                    int index = intent.getIntExtra("index", 0);
                    LrcProcess lrcProcess = new LrcProcess();
                    lrcProcess.readLRC(path);
                    System.out.println("path-----"+path);
                    System.out.println("index-----"+index);
                    List<Lrc> lrcList = lrcProcess.getLrcList();
                    lrcView.setmLrcList(lrcList);
                    lrcView.setIndex(index);
                    lrcView.invalidate();
                    break;
            }
        }

    }
}
