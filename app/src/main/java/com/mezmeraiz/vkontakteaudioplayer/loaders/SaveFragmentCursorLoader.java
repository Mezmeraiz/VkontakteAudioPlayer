package com.mezmeraiz.vkontakteaudioplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.db.DB;
import com.mezmeraiz.vkontakteaudioplayer.db.DBHelper;

/**
 * CursorLoader для SaveFragment
 */
public class SaveFragmentCursorLoader extends CursorLoader {

    Context mContext;

    public SaveFragmentCursorLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Cursor loadInBackground() {
        DB db = DB.getInstance();
        db.open(mContext);
        Cursor cursor = db.getCursor(DBHelper.TABLE_NAME, null, null, null, null, null, AudioHolder.ORDER);
        return cursor;
    }
}
