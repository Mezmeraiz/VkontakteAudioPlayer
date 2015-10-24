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
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.Player;
import com.mezmeraiz.vkontakteaudioplayer.PopupMenuListener;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.adapters.RecyclerViewAdapter;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by pc on 14.10.2015.
 */
public class AudioFragment extends Fragment {

    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private LinkedList<Map<String,String>> mAudioList = new LinkedList<Map<String,String>>();
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    public PopupMenuListener mPopupMenuListener; // Слушатель в адаптер для создания PopupMenu по клику на иконку "меню" в итеме из RecycleView


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Player.START_PLAYING_ACTION)
                        && intent.getIntExtra(Player.CURRENT_FRAGMENT_KEY, 0) == AudioHolder.AUDIO_FRAGMENT)
                    onReceiveStartPlaying(intent);
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
                        Toast.makeText(getActivity().getApplicationContext(), "position = " + v.getTag(),
                                Toast.LENGTH_SHORT).show();
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
        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadAudio();
    }

    private void onReceiveStartPlaying(Intent intent){
        int position = intent.getIntExtra(Player.POSITION_KEY, 0);
        mRecyclerViewAdapter.setPressedPositon(position);
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unregisterReceiver(mBroadcastReceiver);
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
                        map.put(AudioHolder.ARTIST, oneAudioObject.getString(AudioHolder.ARTIST));
                        map.put(AudioHolder.TITLE, oneAudioObject.getString(AudioHolder.TITLE));
                        map.put(AudioHolder.URL, oneAudioObject.getString(AudioHolder.URL));
                        map.put(AudioHolder.DURATION, oneAudioObject.getString(AudioHolder.DURATION));
                        mAudioList.add(map);
                    }
                    AudioHolder.getInstance().setList(mAudioList, AudioHolder.AUDIO_FRAGMENT);
                    mRecyclerViewAdapter = new RecyclerViewAdapter(mAudioList, getActivity(), mPopupMenuListener);
                    mRecyclerView.setAdapter(mRecyclerViewAdapter);
                    mContext.sendBroadcast(new Intent(MainActivity.REQUEST_DATA_FROM_SERVICE_ACTION));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d("myLogs", "attemptFailed");
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d("myLogs", "onError");
            }
        });

    }

}
