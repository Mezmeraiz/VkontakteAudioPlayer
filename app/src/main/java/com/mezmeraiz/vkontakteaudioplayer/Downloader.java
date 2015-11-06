package com.mezmeraiz.vkontakteaudioplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.widget.Toast;
import com.mezmeraiz.vkontakteaudioplayer.db.DB;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Загрузчик аудиозаписей
 */
public class Downloader {

    private Context mContext;
    private String mId, mSongName, mBandName, mUrl, mDuration;
    private OnDataChangedListener mOnDataChangedListener;
    private DownloaderListener mDownloaderListener;
    private int mPosition;

    public Downloader(Context context){
        mContext = context;
    }

    public void download(int currentFragment, int position, OnDataChangedListener onDataChangedListener, DownloaderListener downloaderListener) {
        mOnDataChangedListener = onDataChangedListener;
        mDownloaderListener = downloaderListener;
        mPosition = position;
        List<Map<String, String>> audioList = AudioHolder.getInstance().getList(currentFragment);
        Map<String, String> oneSongMap =  audioList.get(mPosition);
        mId = oneSongMap.get(AudioHolder.ID);
        mSongName = oneSongMap.get(AudioHolder.TITLE);
        mBandName = oneSongMap.get(AudioHolder.ARTIST);
        mUrl = oneSongMap.get(AudioHolder.URL);
        mDuration = oneSongMap.get(AudioHolder.DURATION);
        new AsyncLoader().execute();
    }

    class AsyncLoader extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            return loadRequest();
        }

        @Override
        protected void onPostExecute(String songPath) {
            super.onPostExecute(songPath);
            Map <String ,String> map = new HashMap<String ,String >();
            map.put(AudioHolder.TITLE, mSongName);
            map.put(AudioHolder.URL, songPath);
            map.put(AudioHolder.ARTIST, mBandName);
            map.put(AudioHolder.DURATION, mDuration);
            map.put(AudioHolder.ID, mId);
            mOnDataChangedListener.onDataChanged(map);
            mDownloaderListener.onDownloadFinished(mId, songPath, mPosition);
            Toast.makeText(mContext, mSongName + " загружено", Toast.LENGTH_SHORT).show();
        }
    }


    private String loadRequest(){
        // Загрузка аудиозаписи
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "VKplayer");
        if (!directory.exists()) {
            directory.mkdir();
        }
        String songPath = directory.getAbsolutePath() + "/" + mBandName + " " + mSongName + ".mp3";;
        try{

            URL url=new URL(mUrl);
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                File newSong = new File(songPath);
                newSong.createNewFile();
                DB db = DB.getInstance().open(mContext);
                db.addNewSong(mId, mSongName, mBandName, songPath, mDuration);
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newSong));
                int read;
                while((read=inputStream.read()) !=-1){
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
        return songPath;
    }
}
