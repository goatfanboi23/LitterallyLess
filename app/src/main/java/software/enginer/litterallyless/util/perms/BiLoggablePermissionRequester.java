package software.enginer.litterallyless.util.perms;

import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Map;

public class BiLoggablePermissionRequester extends BiPermissionRequester {
    private final Map<String, String> rationalPermMap;
    private final String logName;

    public BiLoggablePermissionRequester(AppCompatActivity context, boolean requireAll, Map<String, String> rationalPermMap) {
        super(context, requireAll, rationalPermMap.keySet().toArray(new String[0]));
        this.rationalPermMap = rationalPermMap;
        this.logName = context.getClass().getSimpleName();
    }
    @Override
    public void showRational() {
        for (String perm: getRejectedPerms()){
            Log.i(logName, perm + " Permission Rational: " + rationalPermMap.get(perm));
        }
    }

    @Override
    public void onSuccess() {
        Log.i(logName,Arrays.toString(getAcceptedPerms()) + " Permission Accepted");
    }

    @Override
    public void onFail() {
        Log.i(logName,Arrays.toString(getRejectedPerms()) + " Permission Rejected");
    }
}
