package com.isosystems.smartmaid.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.isosystems.smartmaid.R;

public class FragmentNotifications extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_notifications);
    }
}
