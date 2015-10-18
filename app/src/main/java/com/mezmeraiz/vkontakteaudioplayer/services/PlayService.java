package com.mezmeraiz.vkontakteaudioplayer.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.mezmeraiz.vkontakteaudioplayer.Player;
import com.mezmeraiz.vkontakteaudioplayer.adapters.RecyclerViewAdapter;
import com.mezmeraiz.vkontakteaudioplayer.ui.MainActivity;


/**
 * Сервис с MediaPlayer
 */
public class PlayService extends Service {

    public static final String SEEKBAR_PRESSED_KEY = "SEEKBAR_PRESSED_KEY";

    private BroadcastReceiver mBroadcastReceiver;
    private Player mPlayer;


    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new Player(getApplicationContext());
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(MainActivity.DESTROY_SERVICE_ACTION))
                    onReceiveDestroy();
                else if(intent.getAction().equals(MainActivity.NEW_TASK_SERVICE_ACTION))
                    onReceiveNewTask(intent);
                else if(intent.getAction().equals(MainActivity.FAB_PRESSED_SERVICE_ACTION))
                    onReceiveFabPressed(intent);
                else if(intent.getAction().equals(MainActivity.SEEKBAR_PRESSED_SERVICE_ACTION))
                    onReceiveSeekBarPressed(intent);
                else if(intent.getAction().equals(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION))
                    onReceiveRequestDataFromActivity();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.DESTROY_SERVICE_ACTION);
        intentFilter.addAction(MainActivity.NEW_TASK_SERVICE_ACTION);
        intentFilter.addAction(MainActivity.FAB_PRESSED_SERVICE_ACTION);
        intentFilter.addAction(MainActivity.SEEKBAR_PRESSED_SERVICE_ACTION);
        intentFilter.addAction(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION);
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
        // Сигнал о закрытии MainActivity. Если плеер остановлен(isPlaying == false),
        // закрываем плеер и уничтожаем сервис
        if(!mPlayer.isPlaying()){
            mPlayer.release();
            mPlayer = null;
            stopSelf();
        }
    }

    private void onReceiveNewTask(Intent intent){
        //Запуск новой композиции по сигналу из фрагмента после нажатия на item
        int position = intent.getIntExtra(RecyclerViewAdapter.POSITION, 0);
        int currentFragment = intent.getIntExtra(RecyclerViewAdapter.CURRENT_FRAGMENT, 0);
        mPlayer.newTask(position, currentFragment);
    }

    private void onReceiveFabPressed(Intent intent){
        // В MainAcyivity нажата fab - включаем/выключаем MediaPlayer
        mPlayer.onFabPressed();
    }


    private void onReceiveSeekBarPressed(Intent intent){
        // В MainAcyivity нажат SeekBar - меняем прогресс MediaPlayer
        mPlayer.onSeekBarPressed(intent.getIntExtra(SEEKBAR_PRESSED_KEY, 0));
    }

    private void onReceiveRequestDataFromActivity(){
        // MainActivity перезапустилась - просит данные о текущей композиции
        mPlayer.sendBroadcastStartPlaying();
    }


}
