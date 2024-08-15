package software.enginer.litterallyless.perms;

import android.Manifest;

import androidx.activity.ComponentActivity;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Map;
import java.util.function.Supplier;

import software.enginer.litterallyless.util.perms.LoggablePermissionRequester;
import software.enginer.litterallyless.util.perms.PermissionRequester;

public class CameraPermTransition extends LoggablePermissionRequester {
    private final int containerViewId;
    private final Supplier<Fragment> fragmentCreator;

    public CameraPermTransition(AppCompatActivity context, @IdRes int containerViewId, Supplier<Fragment> fragmentCreator) {
        super(context, "USED FOR AI TRASH DETECTION", Manifest.permission.CAMERA);
        this.containerViewId = containerViewId;
        this.fragmentCreator = fragmentCreator;
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        getContext().runOnUiThread(() -> {
            getContext().getSupportFragmentManager().beginTransaction()
                    .replace(containerViewId, fragmentCreator.get())
                    .commitNow();
        });
    }
}
