package software.enginer.litterallyless.ui;

import static androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.StrictMode;

import com.google.ar.core.ArCoreApk;
import com.mapbox.common.MapboxOptions;

import software.enginer.litterallyless.BuildConfig;
import software.enginer.litterallyless.ui.fragments.MapFragment;
import software.enginer.litterallyless.R;
import software.enginer.litterallyless.databinding.ActivityMainBinding;
import software.enginer.litterallyless.perms.CameraPermTransition;
import software.enginer.litterallyless.perms.LocationPermTransition;
import software.enginer.litterallyless.ui.fragments.ArCoreFragment;
import software.enginer.litterallyless.ui.fragments.DetectionFragment;
import software.enginer.litterallyless.ui.fragments.SettingsFragment;
import software.enginer.litterallyless.util.perms.PermissionRequester;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;

    private PermissionRequester locationPermRequest;
    private CameraPermTransition camPermRequest;
    private CameraPermTransition arPermRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableFullscreen();
        super.onCreate(savedInstanceState);
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());
        // create binding
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        // initialize mapbox
        MapboxOptions.setAccessToken(BuildConfig.MapboxAccessToken);

        ArCoreApk.getInstance().checkAvailabilityAsync(this, availability -> {
            if (!availability.isSupported()) {
                activityMainBinding.navBar.removeView(activityMainBinding.navBar.findViewById(R.id.home_menu_item));
            }
        });


        // create permission request helpers
        locationPermRequest = new LocationPermTransition(this, R.id.fragment_container, MapFragment.class);
        camPermRequest = new CameraPermTransition(this, R.id.fragment_container, DetectionFragment.class);
        arPermRequest = new CameraPermTransition(this, R.id.fragment_container, ArCoreFragment.class);

        // configure user interactions
        activityMainBinding.navBar.setSelectedItemId(R.id.home_menu_item);

        activityMainBinding.navBar.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.settings_menu_item) {
                runOnUiThread(() -> {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragment_container, SettingsFragment.class, null)
                            .commitNow();
                });
            } else if (menuItem.getItemId() == R.id.ar_menu_item) {
                arPermRequest.request();
            } else if (menuItem.getItemId() == R.id.map_menu_item) {
                locationPermRequest.request();
            }else if (menuItem.getItemId() == R.id.home_menu_item){
                runOnUiThread(() -> {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new Fragment())
                            .commitNow();
                });
            }
            return true;
        });
    }

    private void enableFullscreen() {
        try {
            EdgeToEdge.enable(this);
        } catch (Exception e) {
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            // Configure the behavior of the hidden system bars.
            windowInsetsController.setSystemBarsBehavior(BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

}