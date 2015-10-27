package com.mezmeraiz.vkontakteaudioplayer.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.PopupMenuListener;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.ui.MainActivity;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Адаптер для RecycleView
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>{

    public final static String POSITION = "POSITION";
    public final static String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";
    private List<Map<String,String>> mAudioList;
    private int[] mColorList;
    private MainActivity mActivity;
    private int mPressedPosition;
    private PopupMenuListener mPopupMenuListener;

    public RecyclerViewAdapter(List<Map<String, String>> itemList, Activity activity, PopupMenuListener popupMenuListener) {
        mAudioList = itemList;
        mColorList = new int[mAudioList.size()];
        Arrays.fill(mColorList, Color.parseColor("#FFFAFAFA"));
        mActivity = (MainActivity) activity;
        mPopupMenuListener = popupMenuListener;
    }

    @Override
    public RecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder viewHolder, final int i) {

        FrameLayout pressPlayFrameLayout = viewHolder.mPressPlayFrameLayout;
        FrameLayout pressMenuFrameLayout = viewHolder.mPressMenuFrameLayout;
        pressPlayFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// Отправка broadcast в PlayService о выборе композиции и старте проигрывания
                Intent intent = new Intent(MainActivity.NEW_TASK_SERVICE_ACTION);
                intent.putExtra(POSITION, i);
                if(mActivity != null){
                    intent.putExtra(CURRENT_FRAGMENT, mActivity.getCurrentFragment());
                    mActivity.sendBroadcast(intent);
                }
            }
        });
        pressMenuFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag(i);
                mPopupMenuListener.onClickPopupMenu(v);
            }
        });

        String band = mAudioList.get(i).get(AudioHolder.ARTIST);
        String song = mAudioList.get(i).get(AudioHolder.TITLE);
        viewHolder.mCardView.setCardBackgroundColor(mColorList[i]);
        viewHolder.mSongTextView.setText(song);
        viewHolder.mBandTextView.setText(band);
    }

    public void setPressedPositon( int position){
        // После нажатия на item из фрагмента меняется цвет CardView. У ранее нажатого на белый, у нового на ...
        mColorList[mPressedPosition] = Color.parseColor("#FFFAFAFA");
        mPressedPosition = position;
        mColorList[mPressedPosition] = Color.parseColor("#cfd8dc");
    }

    public void removePressedPosition(){
        // Снятие выделения с итема, после нажатия на итем в другом фрагменте
        mColorList[mPressedPosition] = Color.parseColor("#FFFAFAFA");
    }


    @Override
    public int getItemCount() {
        return mAudioList.size();
    }


    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView mBandTextView;
        private TextView mSongTextView;
        private FrameLayout mPressPlayFrameLayout, mPressMenuFrameLayout;
        private CardView mCardView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.recycler_card_view);
            mPressPlayFrameLayout = (FrameLayout) itemView.findViewById(R.id.pressPlayFrameLayout);
            mPressMenuFrameLayout = (FrameLayout) itemView.findViewById(R.id.pressMenuframeLayout);
            mBandTextView = (TextView) itemView.findViewById(R.id.band_name);
            mSongTextView = (TextView) itemView.findViewById(R.id.song_name);

        }

    }
}
