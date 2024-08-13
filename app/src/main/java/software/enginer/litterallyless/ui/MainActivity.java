package software.enginer.litterallyless.ui;

import static androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

import com.mapbox.common.MapboxOptions;
import com.mapbox.common.SDKVersions;

import java.util.Map;

import software.enginer.litterallyless.BuildConfig;
import software.enginer.litterallyless.MapFragment;
import software.enginer.litterallyless.R;
import software.enginer.litterallyless.databinding.ActivityMainBinding;
import software.enginer.litterallyless.util.perms.BiLoggablePermissionRequester;
import software.enginer.litterallyless.util.perms.LoggablePermissionRequester;
import software.enginer.litterallyless.util.perms.PermissionRequester;

public class MainActivity extends AppCompatActivity {

    PermissionRequester camPermRequest;
    PermissionRequester locationPermRequest;
    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            EdgeToEdge.enable(this);
        }catch (Exception e){
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            // Configure the behavior of the hidden system bars.
            windowInsetsController.setSystemBarsBehavior(BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        MapboxOptions.setAccessToken(BuildConfig.MapboxAccessToken);

        camPermRequest = new LoggablePermissionRequester(MainActivity.this, "USED FOR AI TRASH DETECTION", Manifest.permission.CAMERA){
            @Override
            public void onSuccess() {
                super.onSuccess();
                runOnUiThread(() -> {
                    if (savedInstanceState == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new DetectionFragment())
                                .commitNow();
                    }
                });
            }
        };
        locationPermRequest = new BiLoggablePermissionRequester(MainActivity.this, true,
                Map.of(Manifest.permission.ACCESS_COARSE_LOCATION, "Needed to approximate location on map",
                        Manifest.permission.ACCESS_FINE_LOCATION, "Needed to get precise location on map")
        ){
            @Override
            public void onSuccess() {
                super.onSuccess();
                runOnUiThread(() -> {
                    if (savedInstanceState == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new MapFragment())
                                .commitNow();
                    }
                });
            }
        };
        activityMainBinding.cameraButton.setOnClickListener(view -> camPermRequest.request());
        activityMainBinding.mapButton.setOnClickListener(view -> locationPermRequest.request());
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

}