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
import android.view.View;
import android.view.ViewGroup;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.DownloadListener;
import com.mezmeraiz.vkontakteaudioplayer.OnRestartActivityListener;
import com.mezmeraiz.vkontakteaudioplayer.Player;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.adapters.RecyclerViewAdapter;
import com.mezmeraiz.vkontakteaudioplayer.db.DB;
import com.mezmeraiz.vkontakteaudioplayer.loaders.SaveFragmentCursorLoader;
import com.mezmeraiz.vkontakteaudioplayer.services.DownloadService;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Фрагмент для сохраненных аудиозаписей
 */
public class SaveFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnRestartActivityListener {

    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private LinkedList<Map<String,String>> mAudioList = new LinkedList<Map<String,String>>();
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    public DownloadListener mDownloadListener;
    private final int SAVE_FRAGMENT_CURSOR_LOADER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity().getApplicationContext();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Player.START_PLAYING_ACTION)){
                    onReceiveStartPlaying(intent);
                }else if(intent.getAction().equals(DownloadService.END_DOWNLOAD_ACTION)){
                    onReceiveEndDownload(intent);
                }else if(intent.getAction().equals(MainActivity.HIDE_LAYOUT_FROM_SERVICE_ACTION)){
                    onReceiveRemovePressedPosition();
                }
            }
        };
        mDownloadListener = new DownloadListener() {
            @Override
            public void onClickDownload(final View v) {
                        // Удаление файла, строки из базы и итема из RecyclerView:
                        // Удаление файла с диска, записи из базы и элемента из списка в адаптере
                        mAudioList = (LinkedList<Map<String, String>>) AudioHolder.getInstance().getList(AudioHolder.SAVED_FRAGMENT);
                        final int position = (int) v.getTag();
                        final int order = mAudioList.size() - position - 1;
                        final long id = Long.parseLong(mAudioList.get(position).get(AudioHolder.ID));
                        if (mAudioList != null){
                            File file = new File(mAudioList.get(position).get(AudioHolder.URL));
                            file.delete();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    DB.getInstance().deleteSong(id, order);
                                }
                            }).start();
                            mRecyclerViewAdapter.removeItem(position);
                        }
                        mContext.sendBroadcast(new Intent(DownloadService.UPDATE_PROGRESS_ACTION));

            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Player.START_PLAYING_ACTION);
        intentFilter.addAction(DownloadService.END_DOWNLOAD_ACTION);
        intentFilter.addAction(MainActivity.HIDE_LAYOUT_FROM_SERVICE_ACTION);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, null);
        initRecyclerView(view);
        setAdapter();
        mAudioList = (LinkedList<Map<String, String>>) AudioHolder.getInstance().getList(AudioHolder.SAVED_FRAGMENT);
        if(mAudioList != null && getActivity().getSupportLoaderManager().getLoader(SAVE_FRAGMENT_CURSOR_LOADER) != null){ // Если список уже есть в AudioHolder - ставим адаптер. Если нет - грузим новый
            mRecyclerViewAdapter.onDataChanged();
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

    private void initRecyclerView(View view){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
    }

    private void setAdapter(){
        mRecyclerViewAdapter = new RecyclerViewAdapter(mAudioList, getActivity(), mDownloadListener, AudioHolder.SAVED_FRAGMENT);
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mRecyclerViewAdapter);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SaveFragmentCursorLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Переносим данные из Cursor в List, записываем в AudioHolder
        // и извещаем адаптер о новом списке
        mAudioList = (LinkedList<Map<String, String>>) cursorToList(data);
        AudioHolder.getInstance().setList(mAudioList, AudioHolder.SAVED_FRAGMENT);
        mRecyclerViewAdapter.onDataChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private List<Map<String, String>> cursorToList(Cursor cursor){
        LinkedList<Map<String, String>> audioList = new LinkedList<Map<String, String>>();
        Map<String, String> savedMap = new HashMap<String, String>();
        if(cursor.moveToFirst()){
            do{
                Map<String, String> map = new HashMap<String, String>();
                map.put(AudioHolder.TITLE, cursor.getString(cursor.getColumnIndex(AudioHolder.TITLE)));
                map.put(AudioHolder.URL, cursor.getString(cursor.getColumnIndex(AudioHolder.PATH)));
                map.put(AudioHolder.ARTIST, cursor.getString(cursor.getColumnIndex(AudioHolder.ARTIST)));
                map.put(AudioHolder.DURATION, cursor.getString(cursor.getColumnIndex(AudioHolder.DURATION)));
                map.put(AudioHolder.ID, cursor.getString(cursor.getColumnIndex(AudioHolder.ID)));
                audioList.addFirst(map);
                savedMap.put(cursor.getString(cursor.getColumnIndex(AudioHolder.ID)), cursor.getString(cursor.getColumnIndex(AudioHolder.PATH)));
            }while (cursor.moveToNext());
        }
        AudioHolder.getInstance().setSavedMap(savedMap);
        return audioList;
    }

    private void onReceiveEndDownload(Intent intent){
        // Пришел сигнал об окончании загрузки - извещаем адаптер
        Map<String, String> map = new HashMap<String, String>();
        map.put(AudioHolder.TITLE, intent.getStringExtra(AudioHolder.TITLE));
        map.put(AudioHolder.URL, intent.getStringExtra(AudioHolder.PATH));
        map.put(AudioHolder.ARTIST, intent.getStringExtra(AudioHolder.ARTIST));
        map.put(AudioHolder.DURATION, intent.getStringExtra(AudioHolder.DURATION));
        map.put(AudioHolder.ID, intent.getStringExtra(AudioHolder.ID));
        mRecyclerViewAdapter.addItem(map);
    }

    private void onReceiveRemovePressedPosition(){
        // В нотификации нажата кнопка отмены - снимаем выделение с итема
        if(mRecyclerViewAdapter != null){
            mRecyclerViewAdapter.removePressedPosition();
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void scrollToPosition(int position) {
        if(mRecyclerView != null && mRecyclerViewAdapter != null)
            mRecyclerView.scrollToPosition(position);
    }
}
