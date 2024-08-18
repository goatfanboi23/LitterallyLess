package software.enginer.litterallyless.util.perms;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import software.enginer.litterallyless.util.FilterResult;
import software.enginer.litterallyless.util.ListUtils;


public abstract class ContractedPermissionRequester<T> implements PermissionRequester {
    private final AppCompatActivity context;
    private final T permHolder;
    private List<String> acceptedPerms = new ArrayList<>();
    private List<String> rejectedPerms = new ArrayList<>();
    private final ActivityResultLauncher<T> requestPermissionLauncher;

    public ContractedPermissionRequester(AppCompatActivity context, T permHolder, ActivityResultContract<T, ?> contract, boolean requireAll) {
        this.context = context;
        this.permHolder = permHolder;
        requestPermissionLauncher = context.registerForActivityResult(contract, r -> {
            List<String> stringPerms = permAsString(permHolder);
            FilterResult<String> result = ListUtils.filter(stringPerms, p -> ActivityCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED);
            acceptedPerms = result.getAccepted();
            rejectedPerms = result.getDenied();
            if ((acceptedPerms.size() == stringPerms.size()) || (!requireAll && !acceptedPerms.isEmpty())) {
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

    public abstract List<String> permAsString(T perm);

    @Override
    public void request() {
        requestPermissionLauncher.launch(permHolder);
    }

    public AppCompatActivity getContext() {
        return context;
    }

    public T getPermHolder() {
        return permHolder;
    }
}
