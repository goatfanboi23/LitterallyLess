package software.enginer.litterallyless.util.perms;

import android.util.Log;

import androidx.activity.ComponentActivity;

public class LoggablePermissionRequester extends SinglePermissionRequester {
    private final String rational;
    private final String logName;

    public LoggablePermissionRequester(ComponentActivity context, String rational, String permission) {
        super(context, permission);
        this.rational = rational;
        this.logName = context.getClass().getSimpleName();
    }

    @Override
    public void showRational() {
        Log.i(logName, getPerm() + " Permission Rational: " + rational);
    }

    @Override
    public void onSuccess() {
        Log.i(logName,getPerm() + " Permission Accepted");
    }

    @Override
    public void onFail() {
        Log.i(logName, getPerm() + " Permission Rejected");
    }
}