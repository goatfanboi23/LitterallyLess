package software.enginer.litterallyless.perms;

import android.Manifest;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import software.enginer.litterallyless.MapFragment;
import software.enginer.litterallyless.util.perms.LoggablePermissionRequester;

public abstract class TransitionPermissionRequester<T> extends LoggablePermissionRequester<T> {
    private final int containerViewId;
    private final Class<? extends Fragment> fragmentClass;

    public TransitionPermissionRequester(AppCompatActivity context, T permHolder, ActivityResultContract<T, ?> contract, boolean requireAll, String[] rationals, @IdRes int containerViewId, Class<? extends Fragment> fragmentClass) {
        super(context, permHolder, contract, requireAll, rationals);
        this.containerViewId = containerViewId;
        this.fragmentClass = fragmentClass;
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        getContext().runOnUiThread(() -> {
            getContext().getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(containerViewId, fragmentClass, null)
                    .commitNow();
        });
    }
}
