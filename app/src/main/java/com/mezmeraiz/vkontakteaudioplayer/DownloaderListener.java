package com.mezmeraiz.vkontakteaudioplayer;

/**
 * Вешается на AudioFragment и SearchFragment и передается в Downloader, чтобы отследить окончание загрузки
 */
public interface DownloaderListener {
    void onDownloadFinished(String id, int position);
}
