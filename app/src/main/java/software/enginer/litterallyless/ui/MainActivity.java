package software.enginer.litterallyless.ui;

import static androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.mapbox.common.MapboxOptions;

import software.enginer.litterallyless.BuildConfig;
import software.enginer.litterallyless.MapFragment;
import software.enginer.litterallyless.R;
import software.enginer.litterallyless.databinding.ActivityMainBinding;
import software.enginer.litterallyless.perms.CameraPermTransition;
import software.enginer.litterallyless.perms.LocationPermTransition;
import software.enginer.litterallyless.util.perms.LoggablePermissionRequester;
import software.enginer.litterallyless.util.perms.PermissionRequester;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;

    private PermissionRequester locationPermRequest;
    private CameraPermTransition camPermRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableFullscreen();
        super.onCreate(savedInstanceState);
        // create binding
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        // initialize mapbox
        MapboxOptions.setAccessToken(BuildConfig.MapboxAccessToken);

        // create permission request helpers
        locationPermRequest = new LocationPermTransition(this, R.id.fragment_container, MapFragment::new);
        camPermRequest = new CameraPermTransition(this, R.id.fragment_container, DetectionFragment::new);

        // configure user interactions
        activityMainBinding.navBar.setSelectedItemId(R.id.home_menu_item);

        activityMainBinding.navBar.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.camera_menu_item) {
                camPermRequest.request();
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