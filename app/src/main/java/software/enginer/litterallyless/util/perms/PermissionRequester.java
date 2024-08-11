package software.enginer.litterallyless.util.perms;

import android.app.Activity;

public interface PermissionRequester {
    void request();
    void showRational();
    void onSuccess();
    void onFail();
    Activity getContext();
}
