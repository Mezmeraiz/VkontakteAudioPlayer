package com.mezmeraiz.vkontakteaudioplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Класс с MediaPlayer
 */
public class Player implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {


    public static final String START_PLAYING_ACTION = "com.mezmeraiz.vkontakteaudioplayer.START_PLAYING_ACTION";// Информирование MainActivity и текущего фрагмента о начале проигрывания новой композиции
    public static final String FAB_PRESSED_BACK_ACTION = "com.mezmeraiz.vkontakteaudioplayer.FAB_PRESSED_BACK_ACTION";//Информирование MainActivity для смены значка fab
    public static final String SEEKBAR_PROGRESS_ACTION = "com.mezmeraiz.vkontakteaudioplayer.SEEKBAR_PROGRESS_ACTION";//Информирование MainActivity для смены прогресса SeekBar
    public static final String SEEKBAR_BUFFERING_ACTION = "com.mezmeraiz.vkontakteaudioplayer.SEEKBAR_BUFFERING_ACTION";//Информирование MainActivity для смены значка fab
    public static final String SEEKBAR_PROGRESS_KEY = "SEEKBAR_PROGRESS_KEY";
    public static final String CURRENT_FRAGMENT_KEY = "CURRENT_FRAGMENT_KEY";
    public static final String POSITION_KEY = "POSITION_KEY";
    public static final String DURATION_KEY = "DURATION_KEY";
    public static final String PROGRESS_KEY = "PROGRESS_KEY";
    public static final String PLAY_STATE_KEY = "PLAY_STATE_KEY";
    public static final String SEEKBAR_BUFFERING_KEY = "SEEKBAR_BUFFERING_KEY";
    public static final String FAB_STATE_KEY = "FAB_STATE_KEY";

    private List<Map<String, String>> mAudioList;
    private Context mContext;
    private int mPosition;
    private MediaPlayer mMediaPlayer;
    private int mCurrentFragment;
    private boolean isPlaying;
    private boolean isPrepared;
    private Timer mTimer;
    private TimerTask mTimerTask;


    public Player(Context context){
        mContext = context;
    }

    public void newTask(int position, int currentFragment){
        // Нажат новый итем во фрагменте - ставим новую композицию
        release();
        mAudioList = AudioHolder.getInstance().getList(currentFragment);
        mPosition = position;
        mCurrentFragment = currentFragment;
        startMediaPlayer();
        isPlaying = true;
        sendBroadcastStartPlaying();
    }

    public boolean isPlaying(){
        return isPlaying;
    }


    public void onFabPressed(){
        // Из активности пришел сигнал о том, что нажата fab
        // Если переключение возможно (isPrepared == true), отсылаем назад сигнал на переключение иконки
        if(!isPrepared)
            return;
        if(isPlaying){
            mMediaPlayer.pause();
            isPlaying = false;
        }else{
            mMediaPlayer.start();
            isPlaying = true;
        }
        sendBroadcastPressedBack();
    }

    public void onSeekBarPressed(int progress){
        // Из активности пришел сигнал о том, что нажата SeekBar
        // Переключаем прогресс mMediaPlayer
        mMediaPlayer.seekTo(progress * 1000);
    }


    public void release(){
        if (mTimerTask !=null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer !=null){
            mTimer.cancel();
            mTimer = null;
        }
        if (mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        isPlaying = false;
        isPrepared = false;
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        sendBroadcastSeekBarBuffering(percent);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mTimer.schedule(mTimerTask, 1000, 1000);
        isPrepared = true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(++mPosition < mAudioList.size() && mCurrentFragment != AudioHolder.SEARCH_FRAGMENT){
            newTask(mPosition, mCurrentFragment);
        }else {
            isPlaying = false;
            isPrepared = false;
        }



    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }


    public void sendBroadcastStartPlaying(){
        // Отправка в MainActivity данных о начале проигрывания новой композиции
        if (mMediaPlayer == null)
            return;
        Intent intent = new Intent(START_PLAYING_ACTION);
        intent.putExtra(CURRENT_FRAGMENT_KEY, mCurrentFragment);
        intent.putExtra(POSITION_KEY, mPosition);
        intent.putExtra(DURATION_KEY, Integer.valueOf(mAudioList.get(mPosition).get(AudioHolder.DURATION)));
        intent.putExtra(PLAY_STATE_KEY, isPlaying);
        intent.putExtra(PROGRESS_KEY, mMediaPlayer.getCurrentPosition()/1000);
        mContext.sendBroadcast(intent);
    }


    private void startMediaPlayer(){
        mTimer = new Timer();
        mTimerTask = new TimerTask(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if(mMediaPlayer!=null){
                    sendBroadcastSeekBarProgress(mMediaPlayer.getCurrentPosition()/1000);
                }
            }

        };
        String source = mAudioList.get(mPosition).get(AudioHolder.URL);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.prepareAsync();
    }


    private void sendBroadcastPressedBack(){
        // Отправка сигнала в MainActivity на переключение иконки
        Intent intent = new Intent(FAB_PRESSED_BACK_ACTION);
        intent.putExtra(FAB_STATE_KEY, isPlaying);
        mContext.sendBroadcast(intent);
    }

    private void sendBroadcastSeekBarProgress(int progress){
        // Отправка в MainActivity прогресс SeekBar
        Intent intent = new Intent(SEEKBAR_PROGRESS_ACTION);
        intent.putExtra(SEEKBAR_PROGRESS_KEY, progress);
        mContext.sendBroadcast(intent);
    }

    private void sendBroadcastSeekBarBuffering(int percent){
        Intent intent = new Intent(SEEKBAR_BUFFERING_ACTION);
        intent.putExtra(SEEKBAR_BUFFERING_KEY, percent);
        mContext.sendBroadcast(intent);
    }
}
