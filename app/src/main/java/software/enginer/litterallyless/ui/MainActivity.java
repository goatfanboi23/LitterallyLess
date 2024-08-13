package software.enginer.litterallyless.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import software.enginer.litterallyless.R;
import software.enginer.litterallyless.databinding.ActivityMainBinding;
import software.enginer.litterallyless.util.perms.LoggablePermissionRequester;

public class MainActivity extends AppCompatActivity {

    LoggablePermissionRequester camPermRequest;
    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        // make fullscreen

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

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
        activityMainBinding.button.setOnClickListener(view -> camPermRequest.request());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}