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
import android.util.Log;
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
import com.mezmeraiz.vkontakteaudioplayer.services.DownloadService;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
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
 * Created by pc on 14.10.2015.
 */
public class AudioFragment extends Fragment implements OnRestartActivityListener{

    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private List<Map<String,String>> mAudioList = new LinkedList<Map<String,String>>();
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private MainActivity mMainActivity;
    private DownloadListener mDownloadListener;
    public static final int PERMISSION_REQUEST_CODE = 7;


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
        mDownloadListener = new DownloadListener() {
            @Override
            public void onClickDownload(final View v) {
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
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
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
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



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, null);
        initRecyclerView(view);
        setAdapter();
        mAudioList = AudioHolder.getInstance().getList(AudioHolder.AUDIO_FRAGMENT);
        if(mAudioList != null){ // Если список уже есть в AudioHolder - ставим адаптер. Если нет - грузим новый
            mRecyclerViewAdapter.onDataChanged();
            mContext.sendBroadcast(new Intent(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION));
        }else{
            loadAudio();
        }
        return view;
    }

    private void onReceiveStartPlaying(Intent intent){
        // Получен broadcast о начале проигрывания новой композиции
        // Если для данного фрагмента - меняем нажатую позицию
        // Если для другого - стираем нажатую позицию
        if(mRecyclerViewAdapter == null)
            return;
        if(intent.getIntExtra(Player.CURRENT_FRAGMENT_KEY, 0) == AudioHolder.AUDIO_FRAGMENT){
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
            Log.d("myLogs", "hide");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_search).setVisible(false);
    }

    private void loadAudio(){
        // Загрузка данных с сервера VK, запись данных в AudioHolder и установка адаптера
        VKRequest vkRequest = new VKRequest("audio.get", VKParameters.from(VKApiConst.OWNER_ID, VKSdk.getAccessToken().userId));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
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
                    AudioHolder.getInstance().setList(mAudioList, AudioHolder.AUDIO_FRAGMENT);
                    mRecyclerViewAdapter.onDataChanged();
                    mContext.sendBroadcast(new Intent(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION));
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
        mRecyclerViewAdapter = new RecyclerViewAdapter(mAudioList, getActivity(), mDownloadListener, AudioHolder.AUDIO_FRAGMENT);
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mRecyclerViewAdapter);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
    }

}
