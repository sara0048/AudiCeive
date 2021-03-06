package com.bignerdranch.android.audiceive;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 4;
    RecordFragment recordFragment = RecordFragment.newInstance();
    RecentsFragment recentsFragment = RecentsFragment.newInstance();
    SearchImageFragment searchImageFragment = SearchImageFragment.newInstance();
    SearchVideoFragment searchVideoFragment = SearchVideoFragment.newInstance();
    private String tabTitles[] = new String[] { "Record", "Recents", "Images", "Videos" };
    private Context context;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return recordFragment;
            case 1:
                return recentsFragment;
            case 2:
                return searchImageFragment;
            case 3:
                return searchVideoFragment;
            default:
                return PageFragment.newInstance();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
