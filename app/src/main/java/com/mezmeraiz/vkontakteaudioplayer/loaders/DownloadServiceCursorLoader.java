package com.mezmeraiz.vkontakteaudioplayer.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

import com.mezmeraiz.vkontakteaudioplayer.db.DB;
import com.mezmeraiz.vkontakteaudioplayer.db.DBHelper;

/**
 * Created by pc on 23.06.2016.
 */
public class DownloadServiceCursorLoader extends CursorLoader {

    Context mContext;

    public DownloadServiceCursorLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Cursor loadInBackground() {
        DB db = DB.getInstance();
        db.open(mContext);
        Cursor cursor = db.getCursor(DBHelper.DOWNLOAD_TABLE_NAME, null, null, null, null, null, null);
        return cursor;
    }
}
