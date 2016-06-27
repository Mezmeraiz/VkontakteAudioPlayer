package com.mezmeraiz.vkontakteaudioplayer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.db.DB;
import com.mezmeraiz.vkontakteaudioplayer.db.DBHelper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by pc on 22.06.2016.
 */
public class DownloadService extends Service{

    private String mId, mTitle, mArtist, mUrl, mDuration;
    public static final String UPDATE_PROGRESS_ACTION = "com.mezmeraiz.vkontakteaudioplayer.UPDATE_PROGRESS_ACTION";
    public static final String END_DOWNLOAD_ACTION = "com.mezmeraiz.vkontakteaudioplayer.END_DOWNLOAD_ACTION";
    public static final String CANCEL_DOWNLOAD_ACTION = "com.mezmeraiz.vkontakteaudioplayer.CANCEL_DOWNLOAD_ACTION";
    public static final int DOWNLOAD_NOTIFICATION_ID = 100501;
    BroadcastReceiver mBroadcastReceiver;
    Notification mNotification;
    RemoteViews mRemoteViews;
    private boolean mDownloadState = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(CANCEL_DOWNLOAD_ACTION)){
                    mDownloadState = false;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CANCEL_DOWNLOAD_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);
        checkDatabase();
    }

    private void checkDatabase(){
        // Проверяем, если в DownloadTable записи. Если да - ставим нотификацию и грузим
        // нет - закрываем сервис
        DB db = DB.getInstance().open(getApplicationContext());
        Cursor cursor = db.getCursor(DBHelper.DOWNLOAD_TABLE_NAME, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            mId = cursor.getString(cursor.getColumnIndex(AudioHolder.ID));
            mTitle = cursor.getString(cursor.getColumnIndex(AudioHolder.TITLE));
            mArtist = cursor.getString(cursor.getColumnIndex(AudioHolder.ARTIST));
            mUrl = cursor.getString(cursor.getColumnIndex(AudioHolder.URL));
            mDuration = cursor.getString(cursor.getColumnIndex(AudioHolder.DURATION));
            if(mNotification == null){
                createNotification();
                startForeground(DOWNLOAD_NOTIFICATION_ID, mNotification);
            }else{
                mRemoteViews.setProgressBar(R.id.progressBarDownloadNotif, 100, 0, false);
                mRemoteViews.setTextViewText(R.id.textViewTitleDownloadNotif, mTitle);
                mRemoteViews.setTextViewText(R.id.textViewArtistDownloadNotif, mArtist);
                startForeground(DOWNLOAD_NOTIFICATION_ID, mNotification);
            }
            new AsyncLoader().execute();
        }else{
            stopSelf();
        }
    }

    private void createNotification(){
        mRemoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.download_notification);
        mRemoteViews.setTextViewText(R.id.textViewTitleDownloadNotif, mTitle);
        mRemoteViews.setTextViewText(R.id.textViewArtistDownloadNotif, mArtist);
        mRemoteViews.setProgressBar(R.id.progressBarDownloadNotif, 100, 0, false);
        Intent intent = new Intent(CANCEL_DOWNLOAD_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.imageViewDownloadNotifClose, pendingIntent);
        mNotification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.vd_download_white)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContent(mRemoteViews)
                .setOngoing(true)
                .build();
    }

    class AsyncLoader extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            return loadRequest();
        }



        @Override
        protected void onPostExecute(String songPath) {
            // Если mDownloadState true - отправляем сигнал об успешной загрузке
            // Если false - загрузка прервана - извещаем фрагменты
            super.onPostExecute(songPath);
            DB.getInstance().open(getApplicationContext()).deleteDownloadRow(mId);
            if(mDownloadState){
                Intent intent = new Intent(END_DOWNLOAD_ACTION);
                intent.putExtra(AudioHolder.ID, mId);
                intent.putExtra(AudioHolder.ARTIST, mArtist);
                intent.putExtra(AudioHolder.TITLE, mTitle);
                intent.putExtra(AudioHolder.PATH, songPath);
                intent.putExtra(AudioHolder.DURATION, mDuration);
                sendBroadcast(intent);
            }else{
                sendBroadcast(new Intent(UPDATE_PROGRESS_ACTION));
            }
            checkDatabase();
        }
    }

    private  String loadRequest(){
        // Загрузка аудиозаписи
        File newSong;
        if(Build.MODEL.equals("GT-I9190")){
            getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            newSong = new File("/storage/extSdCard/Android/data/com.mezmeraiz.vkontakteaudioplayer/files/Music", mArtist + " " + mTitle + ".mp3");
        }else{
            newSong = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), mArtist + " " + mTitle + ".mp3");
        }
        try{
            URL url=new URL(mUrl);
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            int length = connection.getContentLength() ;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                DB db = DB.getInstance().open(getApplicationContext());
                int order = AudioHolder.getInstance().getList(AudioHolder.SAVED_FRAGMENT).size();
                db.addNewSong(mId, mTitle, mArtist, newSong.getAbsolutePath(), mDuration, order);
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newSong));
                sendBroadcast(new Intent(UPDATE_PROGRESS_ACTION));
                int count = 0;
                int read;
                while((read=inputStream.read()) !=-1){
                    if(!mDownloadState){
                        // Отмена загрузки
                        newSong.delete();
                        DB.getInstance().open(getApplicationContext()).deleteSong(Long.valueOf(mId), order);
                        DB.getInstance().open(getApplicationContext()).deleteAll();
                        break;
                    }
                    count++;
                    if(count % (length / 10) == 0){
                        // Отправка примерно каждые 10% загрузки данных во фрагменты на обновление адаптера
                        int percent = (int) (((double)count / length) * 100);
                        if(mNotification != null && mRemoteViews != null){
                            mRemoteViews.setProgressBar(R.id.progressBarDownloadNotif, 100, percent, false);
                            startForeground(DOWNLOAD_NOTIFICATION_ID, mNotification);
                        }
                        DB.getInstance().open(getApplicationContext()).updateNewDownload(mId, percent);
                        Intent intent = new Intent(UPDATE_PROGRESS_ACTION);
                        intent.putExtra(AudioHolder.PROGRESS, percent);
                        sendBroadcast(intent);
                    }
                    outputStream.write(read);
                }
                outputStream.close();
                inputStream.close();
                connection.disconnect();
            }else{
                Snackbar
                        .make(null, "Нет подключения к интернету", Snackbar.LENGTH_LONG)
                        .show();
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return newSong.getAbsolutePath();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
