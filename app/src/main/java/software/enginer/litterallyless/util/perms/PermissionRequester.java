package software.enginer.litterallyless.util.perms;

import androidx.appcompat.app.AppCompatActivity;

public interface PermissionRequester {
    void request();
    void showRational();
    void onSuccess();
    void onFail();
    AppCompatActivity getContext();
}
