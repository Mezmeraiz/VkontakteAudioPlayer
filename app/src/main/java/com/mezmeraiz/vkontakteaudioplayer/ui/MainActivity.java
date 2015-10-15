package com.mezmeraiz.vkontakteaudioplayer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.mezmeraiz.vkontakteaudioplayer.R;
import com.mezmeraiz.vkontakteaudioplayer.adapters.ViewPagerAdapter;
import com.vk.sdk.VKSdk;
import com.vk.sdk.util.VKUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String START_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.START_SERVICE_ACTION";//Action для запуска сервиса
    public static final String DESTROY_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.DESTROY_SERVICE_ACTION";
    public static final String NEW_TASK_SERVICE_ACTION = "com.mezmeraiz.vkontakteaudioplayer.NEW_TASK_SERVICE_ACTION";






    ViewPager mViewPager;
    ViewPagerAdapter mViewPagerAdapter;
    TabLayout mTabLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Log.d("myLogs", fingerprints[0]);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setViewPager();
        setTabLayout();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        startService(new Intent(START_SERVICE_ACTION));

    }

    public void setViewPager(){
        ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
        fragmentList.add(new AudioFragment());
        fragmentList.add(new SaveFragment());
        fragmentList.add(new SearchFragment());
        ArrayList<Integer> iconList = new ArrayList<Integer>();
        iconList.add(R.drawable.music_note);
        iconList.add(R.drawable.download_icon);
        iconList.add(R.drawable.magnify);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        mViewPagerAdapter.setFragmentList(fragmentList, iconList);
        mViewPager.setAdapter(mViewPagerAdapter);
    }

    public void setTabLayout(){
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(mViewPagerAdapter.getTabView(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    }

    public void logout() {
        VKSdk.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public int getCurrentFragment(){
        return mViewPager.getCurrentItem();
    }


}
