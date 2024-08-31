package software.enginer.litterallyless.ui.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import software.enginer.litterallyless.R;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}