package com.bignerdranch.android.audiceive;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.PreferenceManager;

public class MainActivity extends FragmentActivity implements MyInterface {

    SharedPreferences sharedpreferences;
    ViewPagerNoSwipe viewPager;
    SampleFragmentPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPagerNoSwipe) findViewById(R.id.viewpager);
        viewPager.setPagingEnabled(false);

        pagerAdapter = new SampleFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (position != 0)
                    pagerAdapter.recordFragment.onPause();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void saveScene(final Scene scene) {
        final RecentsFragment fragment = (RecentsFragment) pagerAdapter.getItem(1);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragment.addScene(scene);
            }
        });
    }

}
