package com.mezmeraiz.vkontakteaudioplayer.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.Player;
import com.mezmeraiz.vkontakteaudioplayer.PopupMenuListener;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.db.DB;
import com.mezmeraiz.vkontakteaudioplayer.ui.MainActivity;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Адаптер для RecycleView
 */
public class RecyclerViewAdapter extends  RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> implements DraggableItemAdapter<RecyclerViewAdapter.RecyclerViewHolder> {

    public final static String POSITION = "POSITION";
    public final static String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";
    public final static String STOP_PLAYING_FROM_ADAPTER = "STOP_PLAYING_FROM_ADAPTER"; // В PlayService
    public final static String CHANGE_POSITION_FROM_ADAPTER = "CHANGE_POSITION_FROM_ADAPTER"; // В PlayService
    private LinkedList<Map<String,String>> mAudioList;
    private MainActivity mActivity;
    private int mPressedPosition = -1;
    private PopupMenuListener mPopupMenuListener;
    private Context mContext;
    private final int mCurrentFragment; // Фрагмент, для которого ставится данный адаптер
    private final Drawable mGrayDotsDrawable, mBlackDotsDrawable;
    private final int mGreyColor = Color.parseColor("#cfd8dc");
    private final int mWhiteColor = Color.parseColor("#FFFAFAFA");

    public RecyclerViewAdapter(List<Map<String, String>> itemList, Activity activity, PopupMenuListener popupMenuListener, int currentFragment) {
        setHasStableIds(true);
        mAudioList = (LinkedList<Map<String, String>>) itemList;
        mCurrentFragment = currentFragment;
        mActivity = (MainActivity) activity;
        mGrayDotsDrawable = mActivity.getResources().getDrawable(R.drawable.dots_vertical_gray);
        mBlackDotsDrawable = mActivity.getResources().getDrawable(R.drawable.dots_vertical);
        mContext = mActivity.getApplicationContext();
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
        ImageView pressMenuImageView = viewHolder.mPressMenuImageView;
        pressPlayFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// Отправка broadcast в PlayService о выборе композиции и старте проигрывания
                Intent intent = new Intent(MainActivity.NEW_TASK_SERVICE_ACTION);
                intent.putExtra(POSITION, i);
                if (mActivity != null) {
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
        // Если аудиозапись есть в сохраненных - ставим серую иконку на загрузку. Если нет - черную
        if (mCurrentFragment != AudioHolder.SAVED_FRAGMENT && AudioHolder.getInstance().getSavedMap() != null && AudioHolder.getInstance().getSavedMap().containsKey(mAudioList.get(i).get(AudioHolder.ID))){
            pressMenuImageView.setImageDrawable(mGrayDotsDrawable);
        }else{
            pressMenuImageView.setImageDrawable(mBlackDotsDrawable);
        }
        String band = mAudioList.get(i).get(AudioHolder.ARTIST);
        String song = mAudioList.get(i).get(AudioHolder.TITLE);
        if (i == mPressedPosition){
            viewHolder.mCardView.setCardBackgroundColor(mGreyColor);
        }else {
            viewHolder.mCardView.setCardBackgroundColor(mWhiteColor);
        }
        viewHolder.mSongTextView.setText(song);
        viewHolder.mBandTextView.setText(band);
    }

    public void setPressedPositon(int position){
        // После нажатия на item из фрагмента меняется цвет CardView. У ранее нажатого на белый, у нового на ...
        mPressedPosition = position;
    }

    public void addItem(Map<String, String> map){
        // Добавление нового итема в начало списка(Для SaveFragment)
        mAudioList.addFirst(map);
        if(mPressedPosition > -1)
            mPressedPosition++;
        notifyDataSetChanged();
    }

    public void incrementPressedPosition(){
        // Увеличение нажатой позиции на 1 по сигналу из AudioFragment(После длбавления из SearchFragment)
        if(mPressedPosition > -1)
            mPressedPosition++;
    }

    public void onDataChanged(){
        mAudioList = (LinkedList<Map<String, String>>) AudioHolder.getInstance().getList(mCurrentFragment);
        notifyDataSetChanged();
    }

    public void removePressedPosition(){
        // Снятие выделения с итема, после нажатия на итем в другом фрагменте
        mPressedPosition = -1;
    }

    public void removeItem(int position){
        // Удаление элемента из списка.
        // Удаление из idSet id удаленной аудиозаписи
        // Если удаленная позиция меньше позиции, которая в данный момент проигрывается -
        // - опускаем выделение на 1 итем ниже, перезаписываем список в AudioHolder и отправляем
        // broadcast в PlayService, чтобы там в объекте Player уменьшить позицию на 1
        // Если удаленная позиция равна позиции проигрывания - снимаем выделение итема и отправляем
        // broadcast на остановку MediaPlayer
        AudioHolder.getInstance().getSavedMap().remove(mAudioList.get(position).get(AudioHolder.ID));
        mAudioList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, mAudioList.size());
        if(position < mPressedPosition){
            mPressedPosition--;
            mContext.sendBroadcast(new Intent(CHANGE_POSITION_FROM_ADAPTER).putExtra(POSITION, mPressedPosition).putExtra(Player.CURRENT_FRAGMENT_KEY, mCurrentFragment));
        }else if(position == mPressedPosition){
            removePressedPosition();
            mContext.sendBroadcast(new Intent(STOP_PLAYING_FROM_ADAPTER));
        }
    }

    @Override
    public int getItemCount() {
        return mAudioList.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(mAudioList.get(position).get(AudioHolder.ID));
    }

    @Override
    public boolean onCheckCanStartDrag(RecyclerViewHolder holder, int position, int x, int y) {
        // Расчет отступа справа при котором будет перемещение
        int offset = holder.mPressMenuFrameLayout.getRight() - 60;
        if (x > offset)
            return true;
        return false;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(RecyclerViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(final int fromPosition, final int toPosition) {
        // Перемещаенм элемент в List и меняем mPressedPosition
        // Если перемещение в AudioFragment - отправляем на сервер информацию о перемещении
        // Если в SaveFragment - меняем order записей в базе
        int beforeID = 0;
        if (mCurrentFragment == AudioHolder.AUDIO_FRAGMENT && fromPosition < toPosition && mAudioList.size() > toPosition + 1){
            beforeID = Integer.parseInt(mAudioList.get(toPosition + 1).get(AudioHolder.ID));
        }
        else if(mCurrentFragment == AudioHolder.AUDIO_FRAGMENT && fromPosition > toPosition){
            beforeID = Integer.parseInt(mAudioList.get(toPosition).get(AudioHolder.ID));
        }
        int fromOrder = mAudioList.size() - fromPosition - 1;
        int toOrder = mAudioList.size() - toPosition - 1;
        Map<String, String> map = mAudioList.remove(fromPosition);
        int id = Integer.parseInt(map.get(AudioHolder.ID));
        mAudioList.add(toPosition, map);
        if(mPressedPosition == fromPosition){
            mPressedPosition = toPosition;
        }else if(mPressedPosition > fromPosition && mPressedPosition <= toPosition){
            mPressedPosition--;
        }else if(mPressedPosition < fromPosition && mPressedPosition >= toPosition){
            mPressedPosition++;
        }
        mContext.sendBroadcast(new Intent(CHANGE_POSITION_FROM_ADAPTER).putExtra(POSITION, mPressedPosition).putExtra(Player.CURRENT_FRAGMENT_KEY, mCurrentFragment));
        notifyItemMoved(fromPosition, toPosition);
        switch (mCurrentFragment){
            case AudioHolder.AUDIO_FRAGMENT:
                onMoveAudio(id, beforeID);
                break;
            case AudioHolder.SAVED_FRAGMENT:
                onMoveSaved(fromOrder, toOrder, id);
                break;
        }

    }

    private void onMoveSaved(final int fromOrder, final int toOrder, final int id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DB.getInstance().open(mContext).moveSong(fromOrder, toOrder, id);
            }
        }).start();
    }

    private void onMoveAudio(final int id, final int beforeID){
        VKRequest vkRequest = new VKRequest("audio.reorder", VKParameters.from("audio_id", id, "before", beforeID));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {});
    }

    public static class RecyclerViewHolder extends AbstractDraggableItemViewHolder {

        private TextView mBandTextView;
        private TextView mSongTextView;
        private FrameLayout mPressPlayFrameLayout, mPressMenuFrameLayout;
        private CardView mCardView;
        private ImageView mPressMenuImageView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.recycler_card_view);
            mPressPlayFrameLayout = (FrameLayout) itemView.findViewById(R.id.pressPlayFrameLayout);
            mPressMenuFrameLayout = (FrameLayout) itemView.findViewById(R.id.pressMenuframeLayout);
            mBandTextView = (TextView) itemView.findViewById(R.id.band_name);
            mSongTextView = (TextView) itemView.findViewById(R.id.song_name);
            mPressMenuImageView = (ImageView) itemView.findViewById(R.id.pressMenuImageView);
        }

    }
}
