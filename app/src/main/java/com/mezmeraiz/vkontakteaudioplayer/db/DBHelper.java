package com.mezmeraiz.vkontakteaudioplayer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;

/**
 * Created by pc on 26.10.2015.
 */
public class DBHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "VKplayer_Database";
    public static final String SONG_TABLE_NAME = "SongTable";
    public static final String DOWNLOAD_TABLE_NAME = "DownloadTable";
    private static final int DATABASE_VERSION = 2;
    private static final String CREATE_SONG_TABLE = "CREATE TABLE " + SONG_TABLE_NAME + "(" +
            AudioHolder.ID + " text," +
            AudioHolder.TITLE + " text," +
            AudioHolder.ARTIST + " text," +
            AudioHolder.PATH + " text," +
            AudioHolder.DURATION + " text," +
            AudioHolder.ORDER + " integer);";
    private static final String CREATE_DOWNLOAD_TABLE = "CREATE TABLE " + DOWNLOAD_TABLE_NAME + "(" +
            AudioHolder.ID + " text," +
            AudioHolder.TITLE + " text," +
            AudioHolder.ARTIST + " text," +
            AudioHolder.DURATION + " text," +
            AudioHolder.URL + " text," +
            AudioHolder.PROGRESS + " integer);";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SONG_TABLE);
        db.execSQL(CREATE_DOWNLOAD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2){
            db.execSQL(CREATE_DOWNLOAD_TABLE);
        }
    }

}
