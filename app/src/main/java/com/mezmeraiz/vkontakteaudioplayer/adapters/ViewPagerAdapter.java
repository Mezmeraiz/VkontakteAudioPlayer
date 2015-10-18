package com.mezmeraiz.vkontakteaudioplayer.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.mezmeraiz.vkontakteaudioplayer.R;
import java.util.ArrayList;

/**
 * Адаптер для ViewPager
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> mFragmentList;
    private ArrayList<Integer> mIconList;
    private Context mContext;

    public ViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    public void setFragmentList(ArrayList<Fragment> fragmentList, ArrayList<Integer> iconList){
        mFragmentList = fragmentList;
        mIconList = iconList;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public View getTabView(int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_tab, null);
        ImageView tabImageView = (ImageView) view.findViewById(R.id.tabImageView);
        tabImageView.setImageResource(mIconList.get(position));
        return view;
    }

}
