package com.mezmeraiz.vkontakteaudioplayer;

import java.util.List;
import java.util.Map;

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
    public final static String ORDER = "song_order";
    public final static String OWNER_ID = "owner_id";
    public final static String PROGRESS = "progress";
    public final static int AUDIO_FRAGMENT = 0;
    public final static int SAVED_FRAGMENT = 1;
    public final static int SEARCH_FRAGMENT = 2;

    // Список с аудиозаписями из AudioFragment
    private List<Map<String,String>> mAudioList;
    // Список с аудиозаписями из SaveFragment
    private List<Map<String,String>> mSavedList;
    // Список с аудиозаписями из AudioFragment
    private List<Map<String,String>> mSearchList;
    // Map с сохраненными аудиозаписями.
    // Ключ - id, значение - путь к аудиозаписи на диске
    private Map<String, String> mSavedMap;

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

    public Map<String, String> getSavedMap() {
        return mSavedMap;
    }

    public void setSavedMap(Map<String, String> map) {
        mSavedMap = map;
    }


}
