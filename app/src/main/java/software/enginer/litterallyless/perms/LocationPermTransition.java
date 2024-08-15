package software.enginer.litterallyless.perms;

import android.Manifest;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Map;
import java.util.function.Supplier;

import software.enginer.litterallyless.util.perms.BiLoggablePermissionRequester;

public class LocationPermTransition extends BiLoggablePermissionRequester {
    private static final Map<String, String> permDescriptionMap = Map.of(Manifest.permission.ACCESS_COARSE_LOCATION, "Needed to approximate location on map",
            Manifest.permission.ACCESS_FINE_LOCATION, "Needed to get precise location on map"
    );

    private final int containerViewId;
    private final Supplier<Fragment> fragmentCreator;

    public LocationPermTransition(AppCompatActivity activity, @IdRes int containerViewId, Supplier<Fragment> fragmentCreator) {
        super(activity,  true, permDescriptionMap);
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
