package com.mezmeraiz.vkontakteaudioplayer.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.mezmeraiz.vkontakteaudioplayer.AudioHolder;
import com.mezmeraiz.vkontakteaudioplayer.OnDataChangedListener;
import com.mezmeraiz.vkontakteaudioplayer.Player;
import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.adapters.ViewPagerAdapter;
import com.mezmeraiz.vkontakteaudioplayer.db.DB;
import com.mezmeraiz.vkontakteaudioplayer.services.PlayService;
import com.vk.sdk.VKSdk;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO После окончания загрузки список прыгает к проигрываемой композиции



public class MainActivity extends AppCompatActivity {

    public static final String START_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.START_SERVICE_ACTION";//Action для запуска сервиса
    public static final String DESTROY_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.DESTROY_SERVICE_ACTION";//Broadcast на уничтожение сервиса
    public static final String NEW_TASK_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.NEW_TASK_SERVICE_ACTION";//Сигнал из фрагмента о нажатии на новую композицию
    public static final String FAB_PRESSED_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.FAB_PRESSED_SERVICE_ACTION";//Сигнал в сервис о нажатии на fab
    public static final String SEEKBAR_PRESSED_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.SEEKBAR_PRESSED_SERVICE_ACTION";//Сигнал в сервис о нажатии на SeekBar
    public static final String REQUEST_DATA_FROM_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.REQUEST_DATA_FROM_SERVICE_ACTION";//Сигнал в сервис на запрос данных(отсылается после повторного открытия MainActivity)

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private TabLayout mTabLayout;
    private AudioFragment mAudioFragment;
    private SaveFragment mSaveFragment;
    private SearchFragment mSearchFragment;
    private BroadcastReceiver mBroadcastReceiver;
    private TextView mSongTextView, mBandTextView;
    private SeekBar mSeekBar;
    private Toolbar mToolbar;
    private MenuItem mSearchItem;
    private FrameLayout mTopFrameLayout, mBottomFrameLayout;
    private FloatingActionButton mFloatingActionButton;
    private boolean isStarted;// Становится true при первом запуске, чтобы не двигать fab после нажатия на новую композицию во фрагменте


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendBroadcastRequestData();
        mSongTextView = (TextView) findViewById(R.id.songTextView);
        mBandTextView = (TextView) findViewById(R.id.bandTextView);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mTopFrameLayout = (FrameLayout) findViewById(R.id.top_frame_layout);
        mBottomFrameLayout = (FrameLayout) findViewById(R.id.bottom_frame_layout);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setTranslationY(300);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Мои аудиозаписи");
        setSupportActionBar(mToolbar);
        setViewPager();
        setTabLayout();

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcastFabPressed();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    sendBroadcastSeekBarPressed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Player.START_PLAYING_ACTION))
                    onReceiveStartPlaying(intent);
                else if(intent.getAction().equals(Player.FAB_PRESSED_BACK_ACTION))
                    onReceivePressedBack(intent);
                else if(intent.getAction().equals(Player.SEEKBAR_PROGRESS_ACTION))
                    onReceiveSeekBarProgress(intent);
                else if(intent.getAction().equals(Player.SEEKBAR_BUFFERING_ACTION))
                    onReceiveSeekBarBuffering(intent);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Player.START_PLAYING_ACTION);
        intentFilter.addAction(Player.FAB_PRESSED_BACK_ACTION);
        intentFilter.addAction(Player.SEEKBAR_PROGRESS_ACTION);
        intentFilter.addAction(Player.SEEKBAR_BUFFERING_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);
        startService(new Intent(START_SERVICE_ACTION));
    }

    private void setViewPager(){
        ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
        mAudioFragment = new AudioFragment();
        mSaveFragment = new SaveFragment();
        mSearchFragment = new SearchFragment();
        fragmentList.add(mAudioFragment);
        fragmentList.add(mSaveFragment);
        fragmentList.add(mSearchFragment);
        ArrayList<Integer> iconList = new ArrayList<Integer>();
        iconList.add(R.drawable.music_note);
        iconList.add(R.drawable.download_icon);
        iconList.add(R.drawable.magnify);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        mViewPagerAdapter.setFragmentList(fragmentList, iconList);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case AudioHolder.AUDIO_FRAGMENT:
                        mToolbar.setTitle("Мои аудиозаписи");
                        break;
                    case AudioHolder.SAVED_FRAGMENT:
                        mToolbar.setTitle("Сохраненные");
                        break;
                    case AudioHolder.SEARCH_FRAGMENT:
                        mToolbar.setTitle("");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setTabLayout(){
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(mViewPagerAdapter.getTabView(i));
        }
    }


    private void changeFabIcon(boolean fabState){
        if(fabState){
            mFloatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.pause24));
        }else{
            mFloatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.play24));
        }
    }

    private void onReceiveStartPlaying(Intent intent){
        // По сигналу из сервиса о новой композиции заполняем view
        int currentFragment = intent.getIntExtra(Player.CURRENT_FRAGMENT_KEY, 0);
        int currentPosition = intent.getIntExtra(Player.POSITION_KEY, 0);
        List<Map<String, String>> currentList = AudioHolder.getInstance().getList(currentFragment);
        switch (currentFragment){
            case AudioHolder.AUDIO_FRAGMENT:
                mAudioFragment.scrollToPosition(currentPosition);
                break;
            case AudioHolder.SAVED_FRAGMENT:
                mSaveFragment.scrollToPosition(currentPosition);
                break;
            case AudioHolder.SEARCH_FRAGMENT:
                mSearchFragment.scrollToPosition(currentPosition);
                break;
        }
        mSongTextView.setText(currentList.get(currentPosition).get(AudioHolder.TITLE));
        mBandTextView.setText(currentList.get(currentPosition).get(AudioHolder.ARTIST));
        mSeekBar.setMax(intent.getIntExtra(Player.DURATION_KEY, 0));
        mSeekBar.setProgress(intent.getIntExtra(Player.PROGRESS_KEY, 0));
        mSeekBar.setSecondaryProgress(0);
        if(intent.getBooleanExtra(Player.PLAY_STATE_KEY, true)){
            mFloatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.pause24));
        }else{
            mFloatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.play24));
        }
        if(!isStarted)
            mFloatingActionButton.setTranslationY(mTopFrameLayout.getTranslationY());
        isStarted = true;
        mFloatingActionButton.setVisibility(View.VISIBLE);
        mTopFrameLayout.setVisibility(View.VISIBLE);
        mBottomFrameLayout.setVisibility(View.VISIBLE);
    }

    private void onReceivePressedBack(Intent intent) {
        // Добро от сервиса на переключение иконки fab
        changeFabIcon(intent.getBooleanExtra(Player.FAB_STATE_KEY, false));
    }

    private void onReceiveSeekBarProgress(Intent intent) {
        // Данные от сервиса о прогрессе seekBar каждую секунду
        mSeekBar.setProgress(intent.getIntExtra(Player.SEEKBAR_PROGRESS_KEY, 0));
    }

    private void onReceiveSeekBarBuffering(Intent intent){
        // Данные от сервиса о буферизации
        int secondaryProgress = mSeekBar.getMax() * intent.getIntExtra(Player.SEEKBAR_BUFFERING_KEY, 0) / 100;
        mSeekBar.setSecondaryProgress(secondaryProgress);
    }

    private void sendBroadcastFabPressed() {
        // Сигнал в сервис о нажатии на fab
        sendBroadcast(new Intent(FAB_PRESSED_SERVICE_ACTION));
    }

    private void sendBroadcastSeekBarPressed(int progress){
        //  Сигнал в сервис о нажатии на SeekBar
        Intent intent = new Intent(SEEKBAR_PRESSED_SERVICE_ACTION);
        intent.putExtra(PlayService.SEEKBAR_PRESSED_KEY, progress);
        sendBroadcast(intent);
    }

    private void sendBroadcastRequestData(){
        // Запрос данных о текущей композиции из сервиса
        sendBroadcast(new Intent(REQUEST_DATA_FROM_SERVICE_ACTION));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearchItem = menu.findItem(R.id.toolbar_search);
        mSearchItem.setVisible(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent(DESTROY_SERVICE_ACTION));
        unregisterReceiver(mBroadcastReceiver);
    }

    private void logout() {
        VKSdk.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public int getCurrentFragment(){
        return mViewPager.getCurrentItem();
    }

    public OnDataChangedListener getOnDataChangedListener(){
        // Возвращает Listener из SaveFragment
        if (mSaveFragment instanceof OnDataChangedListener){
            return mSaveFragment;
        }else{
            throw new IllegalArgumentException("SaveFragment should implement OnDataChangedListener");
        }
    }

}
