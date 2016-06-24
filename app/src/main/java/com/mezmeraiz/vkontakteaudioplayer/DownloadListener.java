package com.mezmeraiz.vkontakteaudioplayer;

import android.view.View;

/**
 * Интерфейс передает сигнал о нажатии на кнопку загрузки
 * из RecyclerViewAdapter во фрагменты
 */
public interface DownloadListener {
    void onClickDownload(View v);
}
