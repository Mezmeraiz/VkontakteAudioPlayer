package com.mezmeraiz.vkontakteaudioplayer.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.Downloader;
import com.mezmeraiz.vkontakteaudioplayer.DownloaderListener;
import com.mezmeraiz.vkontakteaudioplayer.OnRestartActivityListener;
import com.mezmeraiz.vkontakteaudioplayer.Player;
import com.mezmeraiz.vkontakteaudioplayer.PopupMenuListener;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.adapters.RecyclerViewAdapter;
import com.vk.sdk.api.VKError;
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
    private PopupMenuListener mPopupMenuListener;
    private DownloaderListener mDownloaderListener;
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
                if(intent.getAction().equals(Player.START_PLAYING_ACTION))
                    onReceiveStartPlaying(intent);
            }
        };
        mDownloaderListener = new DownloaderListener() {
            @Override
            public void onDownloadFinished(String id, String songPath, int position) {
                AudioHolder.getInstance().getSavedMap().put(id, songPath);
                mRecyclerViewAdapter.notifyItemChanged(position);
            }
        };
        mPopupMenuListener = new PopupMenuListener() {
            @Override
            public void onClickPopupMenu(final View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.inflate(R.menu.menu_audio_fragment_popup);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Проверяем, сохранена ли уже аудиозапись.
                        // Если нет - сохраняем
                        if (AudioHolder.getInstance().getSavedMap().containsKey(mAudioList.get((Integer) v.getTag()).get(AudioHolder.ID))){
                            Toast.makeText(mContext, "Уже загружено", Toast.LENGTH_SHORT).show();
                        }else{
                            new Downloader(mContext).download(AudioHolder.SEARCH_FRAGMENT, (Integer) v.getTag(), mMainActivity.getOnDataChangedListener(), mDownloaderListener);
                        }
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
                        mAudioList.add(map);
                    }
                    AudioHolder.getInstance().setList(mAudioList, AudioHolder.SEARCH_FRAGMENT);
                    mRecyclerViewAdapter.onDataChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
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
