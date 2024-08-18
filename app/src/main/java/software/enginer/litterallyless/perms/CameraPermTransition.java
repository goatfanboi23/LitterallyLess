package software.enginer.litterallyless.perms;

import android.Manifest;

import androidx.activity.ComponentActivity;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import software.enginer.litterallyless.util.perms.LoggablePermissionRequester;
import software.enginer.litterallyless.util.perms.PermissionRequester;

public class CameraPermTransition extends TransitionPermissionRequester<String> {

    private static final String[] rationals = new String[]{"USED FOR AI TRASH DETECTION"};

    public CameraPermTransition(AppCompatActivity context, @IdRes int containerViewId, Class<? extends Fragment> fragmentClass) {
        super(context, Manifest.permission.CAMERA, new ActivityResultContracts.RequestPermission(),false, rationals, containerViewId, fragmentClass);
    }

    @Override
    public List<String> permAsString(String perm) {
        return Collections.singletonList(perm);
    }
}
