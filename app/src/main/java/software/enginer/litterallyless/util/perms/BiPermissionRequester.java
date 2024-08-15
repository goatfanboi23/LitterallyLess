package software.enginer.litterallyless.util.perms;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import software.enginer.litterallyless.util.Arrays;
import software.enginer.litterallyless.util.FilterResult;

public abstract class BiPermissionRequester implements PermissionRequester {

    private final AppCompatActivity context;
    private final ActivityResultLauncher<String[]> requestPermissionLauncher;
    private String[] acceptedPerms = new String[0];
    private String[] rejectedPerms = new String[0];
    private final String[] perms;

    public BiPermissionRequester(AppCompatActivity context, boolean requireAll, String... permissions) {
        this.context = context;
        this.perms = permissions;
        requestPermissionLauncher = context.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {
            FilterResult<String> result = Arrays.filter(permissions, p -> ActivityCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED);
            acceptedPerms = result.getAccepted().toArray(new String[0]);
            rejectedPerms = result.getDenied().toArray(new String[0]);
            if ((acceptedPerms.length == permissions.length) || (!requireAll && acceptedPerms.length > 0)) {
                onSuccess();
            } else {
                for (String rejectedPerm : rejectedPerms) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(context, rejectedPerm)) {
                        showRational();
                    }
                }
                onFail();
            }
        });

    }


    public String[] getRejectedPerms() {
        return rejectedPerms;
    }

    public String[] getAcceptedPerms() {
        return acceptedPerms;
    }

    public String[] getPerms(){
        return perms;
    }

    @Override
    public void request() {
        requestPermissionLauncher.launch(getPerms());
    }

    public AppCompatActivity getContext() {
        return context;
    }
}
