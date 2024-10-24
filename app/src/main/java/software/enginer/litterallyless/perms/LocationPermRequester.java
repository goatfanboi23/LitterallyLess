package software.enginer.litterallyless.perms;

import android.Manifest;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import software.enginer.litterallyless.util.perms.LoggablePermissionRequester;

public class LocationPermRequester extends LoggablePermissionRequester<String[]> {

    private static final String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final String[] rationals = {"Needed to approximate location on map", "Needed to get precise location on map"};


    public LocationPermRequester(AppCompatActivity context) {
        super(context, perms, new ActivityResultContracts.RequestMultiplePermissions(), true, rationals);
    }

    @Override
    public List<String> permAsString(String[] perm) {
        return Arrays.asList(perm);
    }
}
