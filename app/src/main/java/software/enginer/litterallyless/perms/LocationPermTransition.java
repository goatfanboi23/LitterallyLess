package software.enginer.litterallyless.perms;

import android.Manifest;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class LocationPermTransition extends TransitionPermissionRequester<String[]> {
    private static final String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final String[] rationals = {"Needed to approximate location on map", "Needed to get precise location on map"};


    public LocationPermTransition(AppCompatActivity activity, @IdRes int containerViewId, Class<? extends Fragment> fragmentClass) {
        super(activity, perms, new ActivityResultContracts.RequestMultiplePermissions(),true, rationals, containerViewId, fragmentClass);
    }

    @Override
    public List<String> permAsString(String[] perm) {
        return Arrays.asList(perm);
    }
}
