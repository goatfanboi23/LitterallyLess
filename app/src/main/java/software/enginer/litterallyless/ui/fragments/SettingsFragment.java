package software.enginer.litterallyless.ui.fragments;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import software.enginer.litterallyless.LitterallyLess;
import software.enginer.litterallyless.R;
import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.ui.models.ArCoreViewModel;
import software.enginer.litterallyless.ui.models.FirebaseViewModel;
import software.enginer.litterallyless.ui.models.factories.ArCoreModelFactory;
import software.enginer.litterallyless.ui.models.factories.FirebaseModelFactory;
import software.enginer.litterallyless.ui.state.FirebaseState;


public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = FirebaseUIFragment.class.getSimpleName();

    private FirebaseViewModel viewModel;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private String username_key;
    private Preference usernameKey;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);

        LitterallyLess app = (LitterallyLess)requireActivity().getApplication();
        FirebaseUserRepository repo = app.getFirebaseUserRepository();
        viewModel = new ViewModelProvider(requireActivity(), new FirebaseModelFactory(repo)).get(FirebaseViewModel.class);

        viewModel = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        username_key = getResources().getString(R.string.username_key);
        v.setGravity(Gravity.CENTER);
        MaterialButton btn = new MaterialButton(requireContext());
        btn.setText("Log Out");
        float scale = requireContext().getResources().getDisplayMetrics().density;
        int width = (int) (100 * scale + 0.5f);
        int height = (int) (50 * scale + 0.5f);
        int bottomMargin = (int) (10 * scale + 0.5f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.setMargins(0,0,0,bottomMargin);
        btn.setLayoutParams(layoutParams);
        btn.setOnClickListener(v1 -> viewModel.signOut(requireContext()));
        v.addView(btn);
        preferenceChangeListener = (sharedPreferences, key) -> {
            if (key != null && key.equals(username_key)){
                viewModel.setUsername(sharedPreferences.getString(username_key, ""));
            }
        };

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        usernameKey = getPreferenceManager().findPreference(username_key);

        viewModel.getUIState().observe(getViewLifecycleOwner(), state -> {
            if (state.isSignedIn()){
                if (usernameKey != null && sharedPreferences.getString(username_key, "").isEmpty()){
                    FirebaseState value = viewModel.getUIState().getValue();
                    if (value != null){
                        usernameKey.setDefaultValue(value.getUsername());
                    }
                }
            }
        });

        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}