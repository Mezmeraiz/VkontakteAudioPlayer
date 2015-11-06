package com.mezmeraiz.vkontakteaudioplayer;

import java.util.Map;

/**
 * Интерфейс вешается на SavedFragment для отслеживания изменений в базе данных
 */
public interface OnDataChangedListener {
    void onDataChanged(Map<String ,String> map);
}
