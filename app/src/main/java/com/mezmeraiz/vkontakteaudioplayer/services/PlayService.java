package com.mezmeraiz.vkontakteaudioplayer.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mezmeraiz.vkontakteaudioplayer.Player;
import com.mezmeraiz.vkontakteaudioplayer.adapters.RecyclerViewAdapter;
import com.mezmeraiz.vkontakteaudioplayer.ui.MainActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by pc on 15.10.2015.
 */
public class PlayService extends Service {

    BroadcastReceiver mBroadcastReceiver;
    MediaPlayer mMediaPlayer;
    List<Map<String,String>> mAudioList;


    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(MainActivity.DESTROY_SERVICE_ACTION))
                    onReceiveDestroy();
                else if(intent.getAction().equals(MainActivity.NEW_TASK_SERVICE_ACTION))
                    onReceiveNewTask(intent);

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.DESTROY_SERVICE_ACTION);
        //intFilt.addAction(PlayActivity.NEW_TASK_SERVICE_ACTION);
        //intFilt.addAction(PlayActivity.CHANGE_STATE_SERVICE_ACTION);
        //intFilt.addAction(PlayActivity.CHECK_STATE_SERVICE_ACTION);
        //intFilt.addAction(PlayActivity.CHANGE_SEEKBAR_SERVICE_ACTION);
        //intFilt.addAction(PlayActivity.DESTROY_SERVICE_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onReceiveDestroy(){
        stopSelf();
    }

    private void onReceiveNewTask(Intent intent){
        int position = intent.getIntExtra(RecyclerViewAdapter.POSITION, 0);
        int currentFragment = intent.getIntExtra(RecyclerViewAdapter.CURRENT_FRAGMENT, 0);
        startMediaPlayer(position, currentFragment);

    }

    private void startMediaPlayer(int position, int currentFragment){

    }


}
