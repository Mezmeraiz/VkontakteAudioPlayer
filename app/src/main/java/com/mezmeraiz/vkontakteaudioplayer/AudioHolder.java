package com.mezmeraiz.vkontakteaudioplayer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 14.10.2015.
 */
public class AudioHolder {


    public final static String TITLE = "title";
    public final static String ARTIST = "artist";
    public final static String URL = "url";
    public final static int AUDIO_FRAGMENT = 0;
    public final static int SAVED_FRAGMENT = 1;
    public final static int SEARCH_FRAGMENT = 2;

    private LinkedList<Map<String,String>> mAudioList = new LinkedList<Map<String,String>>();
    private LinkedList<Map<String,String>> mSavedList = new LinkedList<Map<String,String>>();
    private LinkedList<Map<String,String>> mSearchList = new LinkedList<Map<String,String>>();

    private static AudioHolder instance;

    private AudioHolder(){};

    public static AudioHolder getInstance(){
        if(instance == null){
            synchronized (AudioHolder.class){
                if(instance == null){
                    instance = new AudioHolder();
                }
            }
        }
        return instance;
    }



    public void setAudioList(LinkedList<Map<String, String>> audioList) {
        audioList = mAudioList;
    }


    public void setSavedList(LinkedList<Map<String, String>> savedList) {
        mSavedList = savedList;
    }


    public void setSearchList(LinkedList<Map<String, String>> searchList) {
        mSearchList = searchList;
    }

    public List getList(int currentFragment){
        switch (currentFragment){
            case AUDIO_FRAGMENT:
                return mAudioList;
            case SAVED_FRAGMENT:
                return mSavedList;
            case SEARCH_FRAGMENT:
                return mSearchList;
            default:
                return null;
        }
    }

}
