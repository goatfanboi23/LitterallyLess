package software.enginer.litterallyless;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.Manifest;
import android.os.Bundle;

import software.enginer.litterallyless.ui.main.CameraFeedFragment;
import software.enginer.litterallyless.util.perms.LoggablePermissionRequester;

public class MainActivity extends AppCompatActivity {

    LoggablePermissionRequester camPermRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        this.getSupportActionBar().hide();
        camPermRequest = new LoggablePermissionRequester(MainActivity.this, "USED FOR AI TRASH DETECTION", Manifest.permission.CAMERA){
            @Override
            public void onSuccess() {
                super.onSuccess();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (savedInstanceState == null) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragmentContainerView, new CameraFeedFragment())
                                    .commitNow();
                        }
                    }
                });

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!camPermRequest.hasPerm()){
            camPermRequest.request();
        }

    }
}