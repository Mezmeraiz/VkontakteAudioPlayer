package com.mezmeraiz.vkontakteaudioplayer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;

/**
 * Created by pc on 26.10.2015.
 */
public class DBHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "VKplayer_Database";
    public static final String TABLE_NAME = "SongTable";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
            AudioHolder.ID + " text," +
            AudioHolder.TITLE + " text," +
            AudioHolder.ARTIST + " text," +
            AudioHolder.PATH + " text," +
            AudioHolder.DURATION + " text," +
            AudioHolder.ORDER + " integer);";

    //private static final String c = "CREATE TABLE SongTable(id text,title text,artist text,path text,duration text,the_order integer);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Log.d("myLogs", CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
