package com.isosystems.smartmaid.settings;

import android.content.Intent;
import android.preference.PreferenceActivity;

import com.isosystems.smartmaid.Exiter;
import com.isosystems.smartmaid.R;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);

        loadHeadersFromResource(R.xml.settings_headers, target);
    }



    @Override
    protected boolean isValidFragment (String fragmentName) {
        return true;
    }
}
