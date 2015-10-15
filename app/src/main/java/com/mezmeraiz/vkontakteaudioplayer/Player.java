package com.mezmeraiz.vkontakteaudioplayer;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.List;
import java.util.Map;

/**
 * Created by pc on 15.10.2015.
 */
public class Player  {

    List<Map<String, String>> mAudioList;
    Context mContext;
    int mPosition;


    public Player(Context context, int position, int currentFragment){
        mAudioList = AudioHolder.getInstance().getList(currentFragment);
        mPosition = position;
        mContext = context;
    }

    public void execute(){


    }



    private void releaseMediaPlayer(){

    }


}
