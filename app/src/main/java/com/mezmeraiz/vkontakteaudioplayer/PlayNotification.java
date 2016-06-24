package com.mezmeraiz.vkontakteaudioplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import com.mezmeraiz.vkontakteaudioplayer.adapters.RecyclerViewAdapter;
import com.mezmeraiz.vkontakteaudioplayer.services.PlayService;
import com.mezmeraiz.vkontakteaudioplayer.ui.MainActivity;

/**
 * Created by pc on 06.06.2016.
 */
public class PlayNotification extends BroadcastReceiver{

    public final static String CANCEL_NOTIFICATION_ACTION = "com.mezmeraiz.vkontakteaudioplayer.CANCEL_NOTIFICATION_ACTION";
    private static PlayNotification instance;
    private final int ID = 100500;
    private NotificationManager mNotificationManager;
    private Context mContext;
    private String mTitle, mArtist;

    private PlayNotification(Context context){
        mContext = context;
    }

    public static PlayNotification getInstance(Context context){
        if (instance == null){
            instance = new PlayNotification(context);
        }
        return instance;
    }

    public void createNotification(String title, String artist){
        mTitle = title;
        mArtist = artist;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Player.FAB_PRESSED_BACK_ACTION);
        intentFilter.addAction(CANCEL_NOTIFICATION_ACTION);
        mContext.registerReceiver(this, intentFilter);
        createNotification(true);
    }

    public void cancelNotification(){
        if (mNotificationManager != null)
            mNotificationManager.cancel(ID);
        mContext.unregisterReceiver(this);
    }

    private void createNotification(boolean playState){
        mNotificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.notification);
        view.setTextViewText(R.id.textViewTitle, mTitle);
        view.setTextViewText(R.id.textViewArtist, mArtist);
        if (playState){
            view.setImageViewResource(R.id.imageViewNotifPlay, R.drawable.widget_pause);
        }else{
            view.setImageViewResource(R.id.imageViewNotifPlay, R.drawable.widget_play);
        }
        view.setOnClickPendingIntent(R.id.imageViewNotifPrev, createPendingIntent(PlayService.PREV_SERVICE_ACTION));
        view.setOnClickPendingIntent(R.id.imageViewNotifNext, createPendingIntent(PlayService.NEXT_SERVICE_ACTION));
        view.setOnClickPendingIntent(R.id.imageViewNotifPlay, createPendingIntent(MainActivity.FAB_PRESSED_SERVICE_ACTION));
        view.setOnClickPendingIntent(R.id.imageViewNotifClose, createPendingIntent(CANCEL_NOTIFICATION_ACTION));
        Notification notification = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.play)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
        notification.bigContentView = view;
        mNotificationManager.notify(ID, notification);
    }

    private PendingIntent createPendingIntent(String action){
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        return pendingIntent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Player.FAB_PRESSED_BACK_ACTION)){
            createNotification(intent.getBooleanExtra(Player.FAB_STATE_KEY, false));
        }else if(intent.getAction().equals(CANCEL_NOTIFICATION_ACTION)){
            cancelNotification();
            mContext.sendBroadcast(new Intent(RecyclerViewAdapter.STOP_PLAYING_FROM_ADAPTER));
        }
    }
}
