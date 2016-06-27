package com.mezmeraiz.vkontakteaudioplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;
import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.PlayWidget;
import com.mezmeraiz.vkontakteaudioplayer.Player;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.adapters.RecyclerViewAdapter;
import com.mezmeraiz.vkontakteaudioplayer.ui.MainActivity;

import java.util.Map;


/**
 * Сервис с MediaPlayer
 */
public class PlayService extends Service {

    public static final String SEEKBAR_PRESSED_KEY = "SEEKBAR_PRESSED_KEY";
    public final static String NEXT_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.NEXT_SERVICE_ACTION";
    public final static String PREV_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.PREV_SERVICE_ACTION";
    public final static String CANCEL_NOTIFICATION_ACTION = "com.mezmeraiz.vkontakteaudioplayer.CANCEL_NOTIFICATION_ACTION";
    public static final int PLAY_NOTIFICATION_ID = 100502;
    private BroadcastReceiver mBroadcastReceiver;
    private Player mPlayer;
    private int mCurrentFragment;


    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new Player(getApplicationContext());
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(MainActivity.DESTROY_SERVICE_ACTION))
                    onReceiveDestroy();
                else if(intent.getAction().equals(MainActivity.FAB_PRESSED_SERVICE_ACTION))
                    onReceiveFabPressed(intent);
                else if(intent.getAction().equals(MainActivity.SEEKBAR_PRESSED_SERVICE_ACTION))
                    onReceiveSeekBarPressed(intent);
                else if(intent.getAction().equals(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION))
                    onReceiveRequestDataFromActivity(intent);
                else if(intent.getAction().equals(RecyclerViewAdapter.STOP_PLAYING_FROM_ADAPTER))
                    onReceiveStopPlayingFromAdapter();
                else if(intent.getAction().equals(RecyclerViewAdapter.CHANGE_POSITION_FROM_ADAPTER))
                    onReceiveChangePositionFromAdapter(intent);
                else if(intent.getAction().equals(Player.START_PLAYING_ACTION))
                    onReceiveStartPlaying();
                else if(intent.getAction().equals(CANCEL_NOTIFICATION_ACTION))
                    onReceiveCancelFromNotification();
                else if(intent.getAction().equals(NEXT_SERVICE_ACTION))
                    onReceiveChangeFromWidget(true);
                else if(intent.getAction().equals(PREV_SERVICE_ACTION))
                    onReceiveChangeFromWidget(false);

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.DESTROY_SERVICE_ACTION);
        intentFilter.addAction(MainActivity.NEW_TASK_SERVICE_ACTION);
        intentFilter.addAction(MainActivity.FAB_PRESSED_SERVICE_ACTION);
        intentFilter.addAction(MainActivity.SEEKBAR_PRESSED_SERVICE_ACTION);
        intentFilter.addAction(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION);
        intentFilter.addAction(RecyclerViewAdapter.STOP_PLAYING_FROM_ADAPTER);
        intentFilter.addAction(RecyclerViewAdapter.CHANGE_POSITION_FROM_ADAPTER);
        intentFilter.addAction(Player.START_PLAYING_ACTION);
        intentFilter.addAction(CANCEL_NOTIFICATION_ACTION);
        intentFilter.addAction(NEXT_SERVICE_ACTION);
        intentFilter.addAction(PREV_SERVICE_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onNewTask(intent);
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
        // закрываем плеер, уничтожаем сервис и отправляем broadcast в виджет
        if(!mPlayer.isPlaying()){
            mPlayer.release();
            mPlayer = null;
            stopSelf();
            sendBroadcast(new Intent(PlayWidget.DESTROY_WIDGET_ACTION));
        }
    }

    private void onReceiveCancelFromNotification(){
        // В notification нажата кнопка отмены - уничтожаем сервис, виджет и
        // отправляем broadcast на закрытие панели с seekbar и названием исполнителя в MainActivity
        // и отключение выделения нажатого итема во фрагментах
        mPlayer.release();
        mPlayer = null;
        stopSelf();
        sendBroadcast(new Intent(PlayWidget.DESTROY_WIDGET_ACTION));
        sendBroadcast(new Intent(MainActivity.HIDE_LAYOUT_FROM_SERVICE_ACTION));
    }

    private void onReceiveStartPlaying(){
        // Сигнал из плеера о запуске новой композиции - меняем notification
        startForeground(PLAY_NOTIFICATION_ID, createNotification());
    }

    private void onNewTask(Intent intent){
        //Запуск новой композиции по сигналу из фрагмента после нажатия на item
        int position = intent.getIntExtra(RecyclerViewAdapter.POSITION, 0);
        mCurrentFragment = intent.getIntExtra(RecyclerViewAdapter.CURRENT_FRAGMENT, 0);
        mPlayer.newTask(position, mCurrentFragment);
    }

    private void onReceiveFabPressed(Intent intent){
        // В MainAcyivity нажата fab - включаем/выключаем MediaPlayer
        mPlayer.onFabPressed();
        startForeground(PLAY_NOTIFICATION_ID, createNotification());
    }

    private void onReceiveSeekBarPressed(Intent intent){
        // В MainAcyivity нажат SeekBar - меняем прогресс MediaPlayer
        mPlayer.onSeekBarPressed(intent.getIntExtra(SEEKBAR_PRESSED_KEY, 0));
    }

    private void onReceiveRequestDataFromActivity(Intent intent){
        // MainActivity перезапустилась - просит данные о текущей композиции
        mPlayer.sendBroadcastStartPlaying();
    }

    private void onReceiveStopPlayingFromAdapter(){
        // В SaveFragment удалена текущая позиция проигрывания - останавливаем MediaPlayer
        mPlayer.release();
        stopSelf();
    }

    private void onReceiveChangePositionFromAdapter(Intent intent){
        // Во фрагменте изменена позиция до позиции проигрывания -
        // - меняем позицию в Player
        mPlayer.changePosition(intent.getIntExtra(RecyclerViewAdapter.POSITION, 0), intent.getIntExtra(Player.CURRENT_FRAGMENT_KEY, 0));
    }

    private void onReceiveChangeFromWidget(boolean next){
        // Увеличение/уменьшение позиции по сигналу из виджета и уведомления
        if (next)
            mPlayer.incPosition();
        else
            mPlayer.decPosition();
    }

    private Notification createNotification(){
        Map<String, String> map = (Map<String, String>) AudioHolder.getInstance().getList(mCurrentFragment).get(mPlayer.getPosition());
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.textViewPlayNotifTitle, map.get(AudioHolder.TITLE));
        remoteViews.setTextViewText(R.id.textViewPlayNotifArtist, map.get(AudioHolder.ARTIST));
        if (mPlayer.isPlaying()){
            remoteViews.setImageViewResource(R.id.imageViewPlayNotifPlay, R.drawable.widget_pause);
        }else{
            remoteViews.setImageViewResource(R.id.imageViewPlayNotifPlay, R.drawable.widget_play);
        }
        remoteViews.setOnClickPendingIntent(R.id.imageViewPlayNotifPrev, createPendingIntent(PlayService.PREV_SERVICE_ACTION));
        remoteViews.setOnClickPendingIntent(R.id.imageViewPlayNotifNext, createPendingIntent(PlayService.NEXT_SERVICE_ACTION));
        remoteViews.setOnClickPendingIntent(R.id.imageViewPlayNotifPlay, createPendingIntent(MainActivity.FAB_PRESSED_SERVICE_ACTION));
        remoteViews.setOnClickPendingIntent(R.id.imageViewPlayNotifClose, createPendingIntent(CANCEL_NOTIFICATION_ACTION));
        Notification notification = new android.support.v7.app.NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.play)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .build();
        notification.bigContentView = remoteViews;
        return notification;
    }

    private PendingIntent createPendingIntent(String action){
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        return pendingIntent;
    }


}
