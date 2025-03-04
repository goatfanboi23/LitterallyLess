package software.enginer.litterallyless.util.perms;

import android.util.Log;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collection;
import java.util.List;

import software.enginer.litterallyless.ui.MainActivity;

public abstract class LoggablePermissionRequester<T> extends ContractedPermissionRequester<T> {
    private final String[] rationals;
    private final String logName;

    public LoggablePermissionRequester(AppCompatActivity context, T permHolder, ActivityResultContract<T, ?> contract, boolean requireAll, String[] rationals) {
        super(context, permHolder, contract, requireAll);
        this.rationals = rationals;
        this.logName = context.getClass().getSimpleName();
    }

    @Override
    public void showRational() {
        List<String> strings = permAsString(getPermHolder());
        for (int i = 0; i < strings.size(); i++) {
            Log.d(logName, strings.get(i) + " Permission Rational: " + rationals[i]);
        }
    }

    @Override
    public void onSuccess() {
        List<String> strings = permAsString(getPermHolder());
        for (String s: strings){
            Log.d(logName, s + " Permission Accepted");

        }
    }

    @Override
    public void onFail() {
        List<String> strings = permAsString(getPermHolder());
        for (String s: strings){
            Log.d(logName, s + " Permission Rejected");
        }
    }
}
