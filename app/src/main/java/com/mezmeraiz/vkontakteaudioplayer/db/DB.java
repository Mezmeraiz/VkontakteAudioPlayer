package com.mezmeraiz.vkontakteaudioplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.util.Log;

import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;

/**
 * Created by pc on 26.10.2015.
 */
public class DB {

    private static DB instance;
    private SQLiteDatabase mSQLiteDatabase;
    private DBHelper mDBHelper;

    private DB(){};

    public static DB getInstance(){

        if(instance == null){
            synchronized (DB.class){
                if(instance == null){
                    instance = new DB();
                }
            }
        }
        return instance;
    }

    public DB open(Context context){
        if (mDBHelper == null || mSQLiteDatabase == null){
            mDBHelper = new DBHelper(context);
            mSQLiteDatabase = mDBHelper.getWritableDatabase();
        }
        return this;
    }


    public Cursor getCursor(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy){
        if (mSQLiteDatabase != null){
            return mSQLiteDatabase.query(table,columns,selection,selectionArgs,groupBy,having,orderBy);
        }else{
            return null;
        }
    }

    public void addNewSong(String id, String songName, String bandName, String path, String duration, int order){
        ContentValues cv = new ContentValues();
        cv.put(AudioHolder.ID, id);
        cv.put(AudioHolder.TITLE, songName);
        cv.put(AudioHolder.ARTIST, bandName);
        cv.put(AudioHolder.PATH, path);
        cv.put(AudioHolder.DURATION, duration);
        cv.put(AudioHolder.ORDER, order);
        mSQLiteDatabase.insert(DBHelper.TABLE_NAME,null,cv);
    }

    public synchronized void deleteSong(long id, int deletedOrder){
        String query = "UPDATE SongTable SET song_order = song_order - 1 WHERE song_order > " + deletedOrder;
        mSQLiteDatabase.delete(DBHelper.TABLE_NAME, AudioHolder.ID + " = " + id, null);
        Cursor cursor = mSQLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();

    }

    public synchronized void moveSong(int fromPosition, int toPosition, int id){
        // Изменение поля song_order в db
        if(fromPosition > toPosition){
            String query = "UPDATE SongTable SET song_order = song_order + 1 WHERE song_order >= " + toPosition + " AND song_order < " + fromPosition;
            Cursor cursor = mSQLiteDatabase.rawQuery(query, null);
            cursor.moveToFirst();
        }else{
            String query = "UPDATE SongTable SET song_order = song_order - 1 WHERE song_order <= " + toPosition + " AND song_order > " + fromPosition;
            Cursor cursor = mSQLiteDatabase.rawQuery(query, null);
            cursor.moveToFirst();
        }
        String query = "UPDATE SongTable SET song_order = " + toPosition + " WHERE id = " + id;
        Cursor cursor = mSQLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
    }



}
