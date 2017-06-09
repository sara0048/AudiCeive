package com.bignerdranch.android.audiceive;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

// In this case, the fragment displays simple text based on the page
public class PageFragment extends PreferenceFragmentCompat {

    public static PageFragment newInstance() {
        PageFragment fragment = new PageFragment();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
