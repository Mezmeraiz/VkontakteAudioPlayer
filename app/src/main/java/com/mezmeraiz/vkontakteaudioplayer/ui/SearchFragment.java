package com.mezmeraiz.vkontakteaudioplayer.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.mezmeraiz.vkontakteaudioplayer.services.DownloadService;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Фрагмент для поиска
 */
public class SearchFragment extends Fragment implements OnRestartActivityListener{


    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private List<Map<String,String>> mAudioList = new LinkedList<Map<String,String>>();
    private BroadcastReceiver mBroadcastReceiver;
    private VKRequest mVKRequest;
    private Context mContext;
    private SearchView mSearchView;
    private DownloadListener mPopupMenuListener;
    private MainActivity mMainActivity;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMainActivity = (MainActivity) getActivity();
        mContext = mMainActivity.getApplicationContext();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Player.START_PLAYING_ACTION)) {
                    onReceiveStartPlaying(intent);
                }else if(intent.getAction().equals(DownloadService.UPDATE_PROGRESS_ACTION)){
                    mRecyclerViewAdapter.notifyDataSetChanged();
                }else if(intent.getAction().equals(DownloadService.END_DOWNLOAD_ACTION)){
                    onReceiveEndDownload(intent);
                }else if(intent.getAction().equals(MainActivity.HIDE_LAYOUT_FROM_SERVICE_ACTION)){
                    onReceiveRemovePressedPosition();
                }
            }
        };
        mPopupMenuListener = new DownloadListener() {
            @Override
            public void onClickDownload(final View v) {
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (!AudioHolder.getInstance().getSavedMap().containsKey(mAudioList.get((Integer) v.getTag()).get(AudioHolder.ID))) {
                        DB db = DB.getInstance().open(mContext);
                        Map<String, String> map = mAudioList.get((Integer) v.getTag());
                        db.addNewDownload(map.get(AudioHolder.ID),
                                map.get(AudioHolder.URL),
                                map.get(AudioHolder.TITLE),
                                map.get(AudioHolder.ARTIST),
                                map.get(AudioHolder.DURATION));
                        mRecyclerViewAdapter.notifyDataSetChanged();
                        getActivity().startService(new Intent(getActivity(), DownloadService.class));
                    }
                }else{
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AudioFragment.PERMISSION_REQUEST_CODE);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Player.START_PLAYING_ACTION);
        intentFilter.addAction(DownloadService.UPDATE_PROGRESS_ACTION);
        intentFilter.addAction(DownloadService.END_DOWNLOAD_ACTION);
        intentFilter.addAction(MainActivity.HIDE_LAYOUT_FROM_SERVICE_ACTION);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem searchItem = menu.findItem(R.id.toolbar_search);
        searchItem.setVisible(true);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setIconified(false);
        mSearchView.clearFocus();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                load(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                load(newText);
                return true;
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, null);
        initRecyclerView(view);
        if(mAudioList == null){
            mAudioList = new LinkedList<Map<String, String>>();
        }
        setAdapter();
        mAudioList = AudioHolder.getInstance().getList(AudioHolder.SEARCH_FRAGMENT);
        if(mAudioList != null){ // Если список уже есть в AudioHolder - ставим адаптер.
            mRecyclerViewAdapter.onDataChanged();
            mContext.sendBroadcast(new Intent(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION));
        }
        return view;
    }

    private void load(String query){
        if(mVKRequest != null)
            mVKRequest.cancel();
        mVKRequest = new VKRequest("audio.search", VKParameters.from("q", query, "auto_complete", "1", "sort", "2", "search_own", "0", "count", "50"));
        mVKRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    mAudioList = new LinkedList<Map<String, String>>();
                    JSONObject jsonObjectResponse = response.json.getJSONObject("response");
                    JSONArray jsonItemArray = jsonObjectResponse.getJSONArray("items");
                    for (int i = 0; i < jsonItemArray.length(); i++) {
                        Map map = new HashMap<String, String>();
                        JSONObject oneAudioObject = jsonItemArray.getJSONObject(i);
                        map.put(AudioHolder.ID, oneAudioObject.getString(AudioHolder.ID));
                        map.put(AudioHolder.ARTIST, oneAudioObject.getString(AudioHolder.ARTIST));
                        map.put(AudioHolder.TITLE, oneAudioObject.getString(AudioHolder.TITLE));
                        map.put(AudioHolder.URL, oneAudioObject.getString(AudioHolder.URL));
                        map.put(AudioHolder.DURATION, oneAudioObject.getString(AudioHolder.DURATION));
                        map.put(AudioHolder.OWNER_ID, oneAudioObject.getString(AudioHolder.OWNER_ID));
                        mAudioList.add(map);
                    }
                    AudioHolder.getInstance().setList(mAudioList, AudioHolder.SEARCH_FRAGMENT);
                    mRecyclerViewAdapter.onDataChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        mSearchView.clearFocus();
    }

    private void onReceiveStartPlaying(Intent intent){
        // Получен broadcast о начале проигрывания новой композиции
        // Если для данного фрагмента - меняем нажатую позицию
        // Если для другого - стираем нажатую позицию
        if(mRecyclerViewAdapter == null)
            return;
        if(intent.getIntExtra(Player.CURRENT_FRAGMENT_KEY, 0) == AudioHolder.SEARCH_FRAGMENT){
            int position = intent.getIntExtra(Player.POSITION_KEY, 0);
            mRecyclerViewAdapter.setPressedPositon(position);
        }else{
            mRecyclerViewAdapter.removePressedPosition();
        }
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void onReceiveEndDownload(Intent intent){
        // Получен broadcast из DownloadService об окончании загрузки
        // Добавляем данные в SavedMap и обновляем адаптер
        String id = intent.getStringExtra(AudioHolder.ID);
        String path = intent.getStringExtra(AudioHolder.PATH);
        AudioHolder.getInstance().getSavedMap().put(id, path);
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void onReceiveRemovePressedPosition(){
        // В нотификации нажата кнопка отмены - снимаем выделение с итема
        if(mRecyclerViewAdapter != null){
            mRecyclerViewAdapter.removePressedPosition();
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void scrollToPosition(int position) {
        if(mRecyclerView != null && mRecyclerViewAdapter != null)
            mRecyclerView.scrollToPosition(position);
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
        mRecyclerViewAdapter = new RecyclerViewAdapter(mAudioList, getActivity(), mPopupMenuListener, AudioHolder.SEARCH_FRAGMENT);
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mRecyclerViewAdapter);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
    }


}
