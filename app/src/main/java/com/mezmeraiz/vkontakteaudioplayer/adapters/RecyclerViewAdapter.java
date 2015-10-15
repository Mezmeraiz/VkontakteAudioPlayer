package com.mezmeraiz.vkontakteaudioplayer.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.ui.MainActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by pc on 14.10.2015.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>{

    public final static String POSITION = "POSITION";
    public final static String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";

    private List<Map<String,String>> mAudioList;
    private MainActivity mActivity;

    public RecyclerViewAdapter(List<Map<String, String>> itemList, Activity activity) {
        mAudioList = itemList;
        mActivity = (MainActivity) activity;
    }

    @Override
    public RecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false);
        return new RecyclerViewHolder(view);
    }



    @Override
    public void onBindViewHolder(RecyclerViewHolder viewHolder, int i) {
        final int position = i;
        FrameLayout pressPlayFrameLayout = viewHolder.mPressPlayFrameLayout;
        FrameLayout pressMenuFrameLayout = viewHolder.mPressMenuFrameLayout;
        pressPlayFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.NEW_TASK_SERVICE_ACTION);
                intent.putExtra(POSITION, position);
                intent.putExtra(CURRENT_FRAGMENT, mActivity.getCurrentFragment());
                mActivity.sendBroadcast(intent);
            }
        });
        pressMenuFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar
                        .make(v, "menu", Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        String band = mAudioList.get(i).get(AudioHolder.ARTIST);
        String song = mAudioList.get(i).get(AudioHolder.TITLE);
        viewHolder.mSongTextView.setText(song);
        viewHolder.mBandTextView.setText(band);
    }

    @Override
    public int getItemCount() {
        return mAudioList.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView mBandTextView;
        private TextView mSongTextView;
        private FrameLayout mPressPlayFrameLayout, mPressMenuFrameLayout;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            mPressPlayFrameLayout = (FrameLayout) itemView.findViewById(R.id.pressPlayFrameLayout);
            mPressMenuFrameLayout = (FrameLayout) itemView.findViewById(R.id.pressMenuframeLayout);
            mBandTextView = (TextView) itemView.findViewById(R.id.band_name);
            mSongTextView = (TextView) itemView.findViewById(R.id.song_name);
        }
    }
}
