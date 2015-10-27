package com.mezmeraiz.vkontakteaudioplayer.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.OnDataChangedListener;
import com.mezmeraiz.vkontakteaudioplayer.Player;
import com.mezmeraiz.vkontakteaudioplayer.PopupMenuListener;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.adapters.RecyclerViewAdapter;
import com.mezmeraiz.vkontakteaudioplayer.loaders.SaveFragmentCursorLoader;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Фрагмент для сохраненных аудиозаписей
 */
public class SaveFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnDataChangedListener {

    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private List<Map<String,String>> mAudioList = new LinkedList<Map<String,String>>();
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    public PopupMenuListener mPopupMenuListener;
    private final int SAVE_FRAGMENT_CURSOR_LOADER = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity().getApplicationContext();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Player.START_PLAYING_ACTION))
                    onReceiveStartPlaying(intent);
            }
        };
        mPopupMenuListener = new PopupMenuListener() {
            @Override
            public void onClickPopupMenu(final View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.inflate(R.menu.menu_saved_fragment_popup);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(mContext, "position = " + (int)v.getTag(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                popupMenu.show();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Player.START_PLAYING_ACTION);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(itemAnimator);
        mAudioList = AudioHolder.getInstance().getList(AudioHolder.SAVED_FRAGMENT);
        if(mAudioList != null){ // Если список уже есть в AudioHolder - ставим адаптер. Если нет - грузим новый
            mRecyclerViewAdapter = new RecyclerViewAdapter(mAudioList, getActivity(), mPopupMenuListener);
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            mContext.sendBroadcast(new Intent(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION));
        }else{
            loadCursor();
        }
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        if(mRecyclerViewAdapter != null)
            mRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void onReceiveStartPlaying(Intent intent){
        // Получен broadcast о начале проигрывания новой композиции
        // Если для данного фрагмента - меняем нажатую позицию
        // Если для другого - стираем нажатую позицию
        if(mRecyclerViewAdapter == null)
            return;
        if(intent.getIntExtra(Player.CURRENT_FRAGMENT_KEY, 0) == AudioHolder.SAVED_FRAGMENT){
            int position = intent.getIntExtra(Player.POSITION_KEY, 0);
            mRecyclerViewAdapter.setPressedPositon(position);
        }else{
            mRecyclerViewAdapter.removePressedPosition();
        }
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void loadCursor(){
        getActivity().getSupportLoaderManager().initLoader(SAVE_FRAGMENT_CURSOR_LOADER, null, this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SaveFragmentCursorLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Переносим данные из Cursor в List, записываем в AudioHolder, ставим адаптер
        // и отправляем запрос в сервис о нажатой позиции
        List<Map<String, String>> audioList = cursorToList(data);
        AudioHolder.getInstance().setList(audioList, AudioHolder.SAVED_FRAGMENT);
        mRecyclerViewAdapter = new RecyclerViewAdapter(audioList, getActivity(), mPopupMenuListener);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mContext.sendBroadcast(new Intent(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private List<Map<String, String>> cursorToList(Cursor cursor){
        List<Map<String, String>> audioList = new LinkedList<Map<String, String>>();
        if(cursor.moveToFirst()){
            do{
                Map<String, String> map = new HashMap<String, String>();
                map.put(AudioHolder.TITLE, cursor.getString(cursor.getColumnIndex(AudioHolder.TITLE)));
                map.put(AudioHolder.URL, cursor.getString(cursor.getColumnIndex(AudioHolder.PATH)));
                map.put(AudioHolder.ARTIST, cursor.getString(cursor.getColumnIndex(AudioHolder.ARTIST)));
                map.put(AudioHolder.DURATION, cursor.getString(cursor.getColumnIndex(AudioHolder.DURATION)));
                map.put(AudioHolder.ID, cursor.getString(cursor.getColumnIndex(AudioHolder.ID)));
                audioList.add(map);
            }while (cursor.moveToNext());
        }
        return audioList;
    }

    @Override
    public void onDataChanged() {
        // Вызывается по окончании загрузки новой аудиозаписи из DownLoader
        // для обновления списка
        getActivity().getSupportLoaderManager().getLoader(SAVE_FRAGMENT_CURSOR_LOADER).forceLoad();
    }

}
