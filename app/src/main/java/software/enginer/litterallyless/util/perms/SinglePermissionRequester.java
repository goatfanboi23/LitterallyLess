package software.enginer.litterallyless.util.perms;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public abstract class SinglePermissionRequester implements PermissionRequester {
    private final AppCompatActivity context;
    private final String perm;
    private final ActivityResultLauncher<String> requestPermissionLauncher;

    public SinglePermissionRequester(AppCompatActivity context, String perm) {
        this.context = context;
        this.perm = perm;
        requestPermissionLauncher = context.registerForActivityResult(new ActivityResultContracts.RequestPermission(), r -> {
            boolean result = hasPerm();
            if (result) {
                onSuccess();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, perm)) {
                    showRational();
                }
                onFail();
            }
        });

    }

    @Override
    public void request() {
        requestPermissionLauncher.launch(perm);
    }
    public static boolean hasPerm(Context context, String perm){
        return ActivityCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPerm(){
        return hasPerm(context, perm);
    }

    public AppCompatActivity getContext() {
        return context;
    }

    public String getPerm() {
        return perm;
    }
}
