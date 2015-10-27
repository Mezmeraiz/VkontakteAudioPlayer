package com.mezmeraiz.vkontakteaudioplayer;

import android.database.Cursor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Класс для хранения текущих списков аудиозаписей
 */
public class AudioHolder {


    public final static String ID = "id";
    public final static String TITLE = "title";
    public final static String ARTIST = "artist";
    public final static String URL = "url";
    public final static String DURATION = "duration";
    public final static String PATH = "path";
    public final static int AUDIO_FRAGMENT = 0;
    public final static int SAVED_FRAGMENT = 1;
    public final static int SEARCH_FRAGMENT = 2;

    private List<Map<String,String>> mAudioList;
    private List<Map<String,String>> mSavedList;
    private List<Map<String,String>> mSearchList;
    private Set<String> mIdSet;

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

    public void setList(List list, int currentFragment){
        switch (currentFragment){
            case AUDIO_FRAGMENT:
                mAudioList = list;
                break;
            case SAVED_FRAGMENT:
                mSavedList = list;
                break;
            case SEARCH_FRAGMENT:
                mSearchList = list;
                break;
        }
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

    public Set<String> getIdSet() {
        return mIdSet;
    }

    public void setIdSet(Set<String> idSet) {
        this.mIdSet = idSet;
    }


}
