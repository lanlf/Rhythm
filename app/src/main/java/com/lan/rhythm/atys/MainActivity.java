package com.lan.rhythm.atys;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lan.rhythm.R;
import com.lan.rhythm.util.MusicUtil;
import com.lan.rhythm.model.Music;
import com.lan.rhythm.service.PlayerService;
import com.lan.rhythm.util.AppConstant;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //各种UI
    private ListView lv_musics;
    private ProgressDialog progressDialog;
    private List<Music> musicList = new ArrayList<Music>();
    private SeekBar seekBar;
    private ImageView cover;
    private Button query;
    private Button refresh;
    private Button pre;
    private Button next;
    private Button play;
    private Button order;
    SharedPreferences sp;
    /**
     * 自定义的广播接收者，用于更新UI
     */
    MsgReceiver mbr;

    /**
     * 当前播放歌曲的位置
     */
    private int listPosition = 0;
    /**
     * 是否在播放
     */
    private boolean isPlaying= false;
    //一系列动作
    public static final String UPDATE_ACTION = "com.lan.action.UPDATE_ACTION";
    public static final String CTL_ACTION = "com.lan.action.CTL_ACTION";        //选择模式
    public static final String MUSIC_CURRENT = "com.lan.action.MUSIC_CURRENT";
    public static final String MUSIC_DURATION = "com.lan.action.MUSIC_DURATION";
    public static final String REPEAT_ACTION = "com.lan.action.REPEAT_ACTION";
    public static final String SHUFFLE_ACTION = "com.lan.action.SHUFFLE_ACTION";
    public static final String CAHNGE_ACTION = "com.lan.action.CHANGE_ACTION";  //手动动作

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
        sp = getPreferences(MODE_PRIVATE);
        int mode = sp.getInt("mode",PlayerService.MODE_ORDER);
        switch (mode){
            case PlayerService.MODE_CYCLE:
                order.setText("循环");
                break;
            case PlayerService.MODE_ONE:
                order.setText("单曲");
                break;
            case PlayerService.MODE_RANDOM:
                order.setText("随机");
                break;
            case PlayerService.MODE_ORDER:
                order.setText("顺序");
                break;
        }
        //动态注册广播接收器
        mbr = new MsgReceiver();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_ACTION);
        intentFilter.addAction(CTL_ACTION);
        intentFilter.addAction(MUSIC_CURRENT);
        intentFilter.addAction(MUSIC_DURATION);
        intentFilter.addAction(REPEAT_ACTION);
        intentFilter.addAction(SHUFFLE_ACTION);
        intentFilter.addAction(CAHNGE_ACTION);
        registerReceiver(mbr, intentFilter);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            /**
             *开始移动时发一个广播，让service不要再动seekBar
             *  @param seekBar
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent(CAHNGE_ACTION);
                sendBroadcast(intent);
            }

            /**
             *手动改变进度条时，发送广播，通知service更新进度
             *  @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent(UPDATE_ACTION);
                intent.putExtra("progress",seekBar.getProgress());
                sendBroadcast(intent);
            }
        });
        play.setOnClickListener(this);
        pre.setOnClickListener(this);
        next.setOnClickListener(this);
        order.setOnClickListener(this);
        cover.setOnClickListener(this);
        // showDialog();
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                musicList = MusicUtil.getMusicInfo(getApplicationContext());
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                    showDialog();
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                closeDialog();
                lv_musics.setAdapter(new MyAdapter());
                lv_musics.setOnItemClickListener(new MusicListItemClickListener());
            }
        };
        task.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mbr);
    }

    private void findViewById(){
        lv_musics = (ListView) findViewById(R.id.lv_musics);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        cover = (ImageView) findViewById(R.id.iv_cover);
        query = (Button) findViewById(R.id.bt_query);
        refresh = (Button) findViewById(R.id.bt_refresh);
        pre = (Button) findViewById(R.id.bt_pre);
        next = (Button) findViewById(R.id.bt_next);
        play = (Button) findViewById(R.id.bt_play);
        order = (Button) findViewById(R.id.bt_order);
    }

    private void closeDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void showDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在读取...-_-");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_pre:
                previous();
                break;
            case R.id.bt_play:
                if(!isPlaying){
                    play();
                }else {
                    pause();
                } ;
                break;
            case R.id.bt_next:
                next();
                break;
            case R.id.bt_refresh:
                break;
            case R.id.bt_query:
                break;
            case R.id.iv_cover:
                System.out.println("--------------");
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),PlayActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_order:
                //设置点击按钮，弹出菜单，选择播放模式
                // popupmenu：弹出菜单
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,order);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(CTL_ACTION);
                        SharedPreferences.Editor edit = sp.edit();
                        intent.putExtra("listSize",musicList.size());
                        int id =item.getItemId();
                        if(id == R.id.order){
                            order.setText("顺序");
                            intent.putExtra("mode",PlayerService.MODE_ORDER);
                            edit.putInt("mode",PlayerService.MODE_ORDER);
                        }else if(id == R.id.one){
                            order.setText("单曲");
                            intent.putExtra("mode",PlayerService.MODE_ONE);
                            edit.putInt("mode",PlayerService.MODE_ONE);
                        }else if(id == R.id.cycle){
                            order.setText("循环");
                            intent.putExtra("mode",PlayerService.MODE_CYCLE);
                            edit.putInt("mode",PlayerService.MODE_CYCLE);
                        }else if(id == R.id.random){
                            order.setText("随机");
                            intent.putExtra("mode",PlayerService.MODE_RANDOM);
                            edit.putInt("mode",PlayerService.MODE_RANDOM);
                        }
                        sendBroadcast(intent);
                        System.out.println("***********");
                        edit.apply();
                        return true;
                    }
                });
                popupMenu.show();
                break;
        }
    }

    private void pause() {
        Music music = musicList.get(listPosition);
        Intent intent = new Intent();
        intent.setClass(this,PlayerService.class);
        intent.putExtra("listPosition", listPosition);
        intent.putExtra("url", music.getUrl());
        intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
        play.setText("播放");
        isPlaying =false;
        startService(intent);
    }

    /**
     * 下一首歌曲
     */
    public void next() {
        listPosition = listPosition + 1;
        if(listPosition <= musicList.size() - 1) {
            Music music = musicList.get(listPosition);
            replay();
        } else {
            Toast.makeText(MainActivity.this, "没有下一首了", Toast.LENGTH_SHORT).show();
            listPosition = listPosition - 1;
        }
    }

    /**
     * 上一首歌曲
     */
    public void previous() {
        listPosition = listPosition - 1;
        if(listPosition >= 0) {
            Music music = musicList.get(listPosition);
            replay();
        }else {
            Toast.makeText(MainActivity.this, "没有上一首了", Toast.LENGTH_SHORT).show();
            listPosition = listPosition +1;
        }
    }

    public void play() {
        Music music = musicList.get(listPosition);
        Intent intent = new Intent();
        intent.setClass(this,PlayerService.class);
        intent.putExtra("listPosition", listPosition);
        intent.putExtra("url", music.getUrl());
        intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
        play.setText("暂停");
        isPlaying =true;
        startService(intent);
    }

    public void replay() {
        Music music = musicList.get(listPosition);
        Intent intent = new Intent();
        intent.setClass(this,PlayerService.class);
        intent.putExtra("listPosition", listPosition);
        intent.putExtra("url", music.getUrl());
        intent.putExtra("MSG", AppConstant.PlayerMsg.REPLAY_MSG);
        play.setText("暂停");
        isPlaying =true;
        startService(intent);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return musicList.size();
        }

        @Override
        public Object getItem(int position) {
            return musicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return musicList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = View.inflate(getApplicationContext(), R.layout.music_item, null);
            TextView title = (TextView) convertView.findViewById(R.id.tv_tilte);
            TextView artist = (TextView) convertView.findViewById(R.id.tv_artist);
            title.setText(musicList.get(position).getTitle());
            artist.setText(musicList.get(position).getArtist());
            return convertView;
        }
    }
    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if(musicList != null) {
               /* Music music = musicList.get(position);
                Log.d("music-->", music.toString());
                Intent intent = new Intent();
                intent.putExtra("url", music.getUrl());
                intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
                intent.setClass(MainActivity.this, PlayerService.class);
                startService(intent);  */     //启动服务
                int lastPosition = listPosition;
                listPosition  = position;
                if(isPlaying && lastPosition == position){
                    pause();
                }else {
                    replay();
                };
            }
        }
    }

    /**
     * 广播接收器
     * @author len
     *
     */
    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                //获取歌曲长度
                case MUSIC_DURATION:
                    int max = intent.getIntExtra("max",0);
                    seekBar.setMax(max);
                    break;
                //拿到进度，更新UI
                case MUSIC_CURRENT:
                    int current = intent.getIntExtra("current",0);
                    seekBar.setProgress(current);
                    break;
            }
        }

    }


}
