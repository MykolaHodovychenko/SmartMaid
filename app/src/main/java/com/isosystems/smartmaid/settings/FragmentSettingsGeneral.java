package com.isosystems.smartmaid.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.isosystems.smartmaid.Exiter;
import com.isosystems.smartmaid.MainActivity;
import com.isosystems.smartmaid.R;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class FragmentSettingsGeneral extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_general);

        Preference back_button = (Preference) findPreference("button_back");
        back_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().finish();
                return true;
            }
        });

        Preference quit_button = (Preference) findPreference("button_quit");
        quit_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                Activity a = (Activity)getActivity();
//                a.finishAffinity();

                finishAndHide();

                return true;
            }
        });

    }

    private void finishAndHide() {
        final Intent relaunch = new Intent(getActivity(), Exiter.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK // CLEAR_TASK requires this
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK // finish everything else in the task
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); // hide (remove, in this case) task from recents
        startActivity(relaunch);
    }
}
